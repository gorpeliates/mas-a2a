
import ai.koog.a2a.client.A2AClient

import ai.koog.agents.a2a.client.feature.A2AAgentClient
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import io.github.cdimascio.dotenv.dotenv


class ClientAgent {
    private val clients = mapOf<String, A2AClient>()
    private val executor: PromptExecutor = simpleOpenAIExecutor(dotenv()["OPENAI_API_KEY"])

    private val agentConfig = AIAgentConfig(
        prompt("ClientAgent") {
            system(
                "You are a client agent that communicates with other agents to complete" +
                        "the tasks provided."
            )
        },
        model = OpenAIModels.Chat.GPT5Mini,
        maxAgentIterations = 100
    )

    private val agent = AIAgent<String, String>(
        promptExecutor = executor,
        agentConfig = agentConfig,
        strategy = clientStrategy(),
        toolRegistry = ToolRegistry.EMPTY
    ) {
        install(A2AAgentClient){
            this.a2aClients = clients
        }
    }
    init {

    }

    suspend fun run(message: String): String {
        return this.agent.run(message)
    }
}


private fun clientStrategy() = strategy<String, String> ("clientStrategy") {



}
