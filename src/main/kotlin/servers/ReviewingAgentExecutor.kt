package servers

import ai.koog.a2a.model.MessageSendParams
import ai.koog.a2a.server.agent.AgentExecutor
import ai.koog.a2a.server.session.RequestContext
import ai.koog.a2a.server.session.SessionEventProcessor
import ai.koog.agents.a2a.core.A2AMessage
import ai.koog.agents.a2a.core.toA2AMessage
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.tool.AskUser
import ai.koog.agents.ext.tool.ExitTool
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import io.github.cdimascio.dotenv.dotenv
import utils.AgentServerFactory

class ReviewingAgentExecutor : AgentExecutor {

    val promptExecutor: PromptExecutor = simpleOpenAIExecutor(dotenv()["OPENAI_API_KEY"])
    override suspend fun execute(
        context: RequestContext<MessageSendParams>,
        eventProcessor: SessionEventProcessor
    ) {
        val agent = AgentServerFactory.createA2AServerAgent(
            agentName = "ReviewingAgent",
            systemPrompt = "You are a code reviewing agent that provides thorough, constructive code reviews. " +
                    "You analyze code for best practices, potential bugs, performance issues, security concerns, " +
                    "readability, and maintainability. You provide feedback in GitHub PR comment style with specific " +
                    "line references, severity levels, and actionable suggestions for improvement.",
            strategy = reviewingAgentStrategy(),
            promptExecutor = promptExecutor,
            context = context,
            eventProcessor = eventProcessor,
            toolRegistry = ToolRegistry {
                tool(AskUser)
                tool(ExitTool)
                tool(SayToUser)
            }
        )
        val message = agent.run(context.params.message)

        eventProcessor.sendMessage(message = message)
    }
}

private fun reviewingAgentStrategy() = strategy<A2AMessage, A2AMessage>("reviewing") {

    val nodeAnalyzeStructure by nodeLLMRequest()
    val nodeIdentifyIssues by nodeLLMRequest()
    val nodeGenerateComments by nodeLLMRequest()
    edge(nodeStart forwardTo nodeAnalyzeStructure transformed { it.toString()})
    edge(nodeAnalyzeStructure forwardTo nodeIdentifyIssues transformed { it.toString() })
    edge(nodeIdentifyIssues forwardTo nodeGenerateComments transformed { it.toString() })
    edge(nodeGenerateComments forwardTo nodeFinish transformed { it.toA2AMessage() })
}
