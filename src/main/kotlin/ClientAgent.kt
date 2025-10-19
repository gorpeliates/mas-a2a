
import ai.koog.a2a.client.A2AClient
import ai.koog.a2a.client.UrlAgentCardResolver
import ai.koog.a2a.transport.client.jsonrpc.http.HttpJSONRPCClientTransport

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
import kotlinx.coroutines.runBlocking
import utils.ServerProperties


class ClientAgent(serverProperties : List<ServerProperties>) {

    private val clients : Map<String, A2AClient> = serverProperties.associate {
        properties ->
            val transport = HttpJSONRPCClientTransport(url = properties.host)
            val agentCardResolver =
                UrlAgentCardResolver(baseUrl = properties.host, path =properties.agentPath)
            val client = A2AClient(transport = transport, agentCardResolver = agentCardResolver)

            val agentId = properties.id
            runBlocking { client.connect() }
            agentId to client
    }

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

    suspend fun run(message: String): String {
        return this.agent.run(message)
    }
}


private fun clientStrategy() = strategy<String, String> ("clientStrategy") {

}
