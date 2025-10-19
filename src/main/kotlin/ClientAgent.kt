
import ai.koog.a2a.client.A2AClient
import ai.koog.a2a.client.UrlAgentCardResolver
import ai.koog.a2a.model.MessageSendParams
import ai.koog.a2a.model.Role
import ai.koog.a2a.model.TextPart
import ai.koog.a2a.transport.ClientCallContext
import ai.koog.a2a.transport.client.jsonrpc.http.HttpJSONRPCClientTransport

import ai.koog.agents.a2a.client.feature.A2AAgentClient
import ai.koog.agents.a2a.client.feature.A2AClientRequest
import ai.koog.agents.a2a.client.feature.nodeA2AClientGetAgentCard
import ai.koog.agents.a2a.client.feature.nodeA2AClientGetAllAgents
import ai.koog.agents.a2a.client.feature.nodeA2AClientSendMessage
import ai.koog.agents.a2a.core.toA2AMessage
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.context.AIAgentGraphContextBase
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.agent.reActStrategy
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import utils.ServerProperties
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


class ClientAgent(serverProperties : List<ServerProperties>) {

    private val clients : Map<String, A2AClient> = serverProperties.associate {
        properties ->
            val transport = HttpJSONRPCClientTransport(url = properties.host + properties.agentPath)
            val agentCardResolver =
                UrlAgentCardResolver(baseUrl = properties.host, path =properties.agentCardPath)

        val client = A2AClient(transport = transport, agentCardResolver = agentCardResolver)
            val agentId = properties.id
            try {
                runBlocking { client.connect() }
            } catch (e: Exception) {
                error("Failed to connect to $agentId"+e.stackTraceToString())

            }

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
        maxAgentIterations = 5
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
    val nodeReason by nodeLLMRequest("reason")
    val nodeSendA2AMessage by nodeA2AClientSendMessage("senda2aMessage")
    val nodeGetAgents by nodeA2AClientGetAllAgents()
    val nodeGetAgentCard by nodeA2AClientGetAgentCard()

    edge(nodeStart forwardTo nodeGetAgents transformed {})
    edge(nodeGetAgents forwardTo nodeReason transformed { agents ->
        "The available agents are: ${agents.joinToString { it.agentId }}. " +
                "Decide which agent to send the message to based on the input: '$agentInput'"
    })
    edge(nodeReason forwardTo nodeSendA2AMessage transformed { buildA2ARequest(it.toString())})
    edge(nodeReason forwardTo nodeFinish transformed { it.toString() })
    edge(nodeSendA2AMessage forwardTo nodeReason transformed {it.toString()})
}

@OptIn(ExperimentalUuidApi::class)
private fun AIAgentGraphContextBase.buildA2ARequest(agentId: String): A2AClientRequest<MessageSendParams> =
    A2AClientRequest(
        agentId = agentId,
        callContext = ClientCallContext.Default,
        params = MessageSendParams(
            message = ai.koog.agents.a2a.core.A2AMessage(
                messageId = Uuid.random().toString(),
                role = Role.User,
                parts = listOf(
                    TextPart(agentInput as String)
                )
            )
        )
    )
