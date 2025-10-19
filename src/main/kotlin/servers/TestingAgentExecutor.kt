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
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import io.github.cdimascio.dotenv.dotenv
import tools.AgentServerFactory

class TestingAgentExecutor : AgentExecutor {

    val promptExecutor: PromptExecutor = simpleOpenAIExecutor(dotenv()["OPENAI_API_KEY"])
    override suspend fun execute(
        context: RequestContext<MessageSendParams>,
        eventProcessor: SessionEventProcessor
    ) {
        val agent = AgentServerFactory.createA2AServerAgent(
            agentName = "TestingAgent",
            systemPrompt = "You are a testing agent that creates comprehensive unit tests for code. " +
                    "You write clear, well-structured unit tests that cover edge cases, normal cases, " +
                    "and error conditions. You follow testing best practices and use appropriate testing frameworks.",
            strategy = testingAgentStrategy(),
            promptExecutor = promptExecutor,
            context = context,
            eventProcessor = eventProcessor
        )
        val message = agent.run(context.params.message)
        eventProcessor.sendMessage(message = message)
    }
}

private fun testingAgentStrategy() = strategy<A2AMessage, A2AMessage>("testing") {

    val nodeAnalyzeCode by nodeLLMRequest()
    val nodeWriteTests by nodeLLMRequest()
    edge(nodeStart forwardTo nodeAnalyzeCode transformed { it.toString()})
    edge(nodeAnalyzeCode forwardTo nodeWriteTests transformed { it.toString() })
    edge(nodeWriteTests forwardTo nodeFinish transformed { it.toA2AMessage() })
}
