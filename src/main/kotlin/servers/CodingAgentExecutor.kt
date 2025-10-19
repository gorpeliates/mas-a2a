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

class CodingAgentExecutor : AgentExecutor {

    val promptExecutor: PromptExecutor = simpleOpenAIExecutor(dotenv()["OPENAI_API_KEY"])
    override suspend fun execute(
        context: RequestContext<MessageSendParams>,
        eventProcessor: SessionEventProcessor
    ) {
        val agent = AgentServerFactory.createA2AServerAgent(
            agentName = "CodingAgent",
            systemPrompt = "You are a coding agent that writes clean and understandable code in various" +
                    "programming languages based on your task.",
            strategy = codingAgentStrategy(),
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

private fun codingAgentStrategy() = strategy<A2AMessage, A2AMessage>("coding") {

    val nodeCreateRequirements by nodeLLMRequest()
    val nodeWriteCode by nodeLLMRequest()
    edge(nodeStart forwardTo nodeCreateRequirements transformed { it.toString()})
    edge(nodeCreateRequirements forwardTo nodeWriteCode transformed { it.toString() })
    edge(nodeWriteCode forwardTo nodeFinish transformed { it.toA2AMessage() })
}
