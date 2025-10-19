package utils

import ai.koog.a2a.model.MessageSendParams
import ai.koog.a2a.server.session.RequestContext
import ai.koog.a2a.server.session.SessionEventProcessor
import ai.koog.agents.a2a.core.A2AMessage
import ai.koog.agents.a2a.server.feature.A2AAgentServer
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.model.PromptExecutor

object AgentServerFactory {

    fun createA2AServerAgent(
        agentName: String,
        systemPrompt: String,
        strategy: AIAgentGraphStrategy<A2AMessage, A2AMessage>,
        promptExecutor: PromptExecutor,
        context: RequestContext<MessageSendParams>?,
        eventProcessor: SessionEventProcessor?,
        toolRegistry: ToolRegistry = ToolRegistry.EMPTY
    ): AIAgent<A2AMessage, A2AMessage> {

        val config = AIAgentConfig(
            prompt(agentName) {
                system(systemPrompt)
            },
            model = OpenAIModels.Chat.GPT5Mini,
            maxAgentIterations = 100
        )

        return AIAgent<A2AMessage, A2AMessage>(
            promptExecutor = promptExecutor,
            agentConfig = config,
            strategy = strategy,
            toolRegistry = toolRegistry
        ) {
            if(context != null && eventProcessor != null) {
                install(A2AAgentServer) {
                    this.context = context
                    this.eventProcessor = eventProcessor
                }
            } else {
                error("Could not create A2AAgent config for $agentName")
            }
        }
    }
}

