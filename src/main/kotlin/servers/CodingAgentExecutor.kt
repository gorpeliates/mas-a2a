package servers

import ai.koog.a2a.model.MessageSendParams
import ai.koog.a2a.server.agent.AgentExecutor
import ai.koog.a2a.server.session.RequestContext
import ai.koog.a2a.server.session.SessionEventProcessor
import ai.koog.agents.a2a.core.A2AMessage
import ai.koog.agents.a2a.core.toA2AMessage
import ai.koog.agents.a2a.core.toKoogMessage
import ai.koog.agents.a2a.server.feature.A2AAgentServer
import ai.koog.agents.a2a.server.feature.nodeA2ATaskGet
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.agent.reActStrategy
import ai.koog.agents.ext.tool.AskUser
import ai.koog.agents.ext.tool.ExitTool
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import io.github.cdimascio.dotenv.dotenv
import kotlin.uuid.ExperimentalUuidApi

class CodingAgentExecutor : AgentExecutor {

    val promptExecutor: PromptExecutor = simpleOpenAIExecutor(dotenv()["OPENAI_API_KEY"])
    override suspend fun execute(
        context: RequestContext<MessageSendParams>,
        eventProcessor: SessionEventProcessor
    ) {
        val agent = codingAgent(promptExecutor, context, eventProcessor)
        agent.run(context.params.message)
    }
}

private fun codingAgent(
    promptExecutor: PromptExecutor,
    context: RequestContext<MessageSendParams>,
    eventProcessor: SessionEventProcessor
) : AIAgent<A2AMessage, A2AMessage> {

    val config = AIAgentConfig(
        prompt("CodingAgent") {
            system("You are a coding agent that writes clean and understandable code in various" +
                    "programming languages based on your task.")
        },
        model = OpenAIModels.Chat.GPT5Mini,
        maxAgentIterations = 100
    )

    val strategy = codingAgentStrategy()
    val toolRegistry = ToolRegistry {
        tool(AskUser)
        tool(ExitTool)
        tool(SayToUser)
    }

    return AIAgent<A2AMessage, A2AMessage>(
        promptExecutor = promptExecutor,
        agentConfig = config,
        strategy = strategy,
        toolRegistry = toolRegistry
    ) {
        install(A2AAgentServer){
            this.context = context
            this.eventProcessor = eventProcessor
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
private fun codingAgentStrategy() = strategy<A2AMessage, A2AMessage>("coding") {

    val nodeCreateRequirements by nodeLLMRequest()
    val nodeWriteCode by nodeLLMRequest()
    edge(nodeStart forwardTo nodeCreateRequirements transformed { it.toString()})
    edge(nodeCreateRequirements forwardTo nodeWriteCode transformed { it.toString() })
    edge(nodeWriteCode forwardTo nodeFinish transformed { it.toA2AMessage() })
}

