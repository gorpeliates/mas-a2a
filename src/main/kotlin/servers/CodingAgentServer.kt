package servers

import ai.koog.a2a.model.AgentCapabilities
import ai.koog.a2a.model.AgentCard
import ai.koog.a2a.model.AgentInterface
import ai.koog.a2a.model.AgentSkill
import ai.koog.a2a.model.TransportProtocol
import ai.koog.a2a.server.A2AServer
import ai.koog.a2a.transport.server.jsonrpc.http.HttpJSONRPCServerTransport
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.cio.CIO


const val CODING_AGENT_PATH = "/coding-agent"
const val CODING_AGENT_CARD_PATH = "$CODING_AGENT_PATH/.well-known/agent-card.json"
private val codinglogger = KotlinLogging.logger {}
const val PORT = 9999
const val CODING_HOST  = "http://localhost:$PORT"

suspend fun main(){
    codinglogger.info { "Starting coding agent server on $CODING_HOST" }

    val agentCard = AgentCard(
        protocolVersion = "0.3.0",
        name = "Coding Agent",
        description = "An AI agent that writes quality code",
        url = CODING_HOST + CODING_AGENT_PATH,
        version = "1.0.0",
        preferredTransport = TransportProtocol.JSONRPC,
        additionalInterfaces = listOf(
            AgentInterface(
                url = CODING_HOST + CODING_AGENT_PATH,
                transport = TransportProtocol.JSONRPC
            )
        ),
        capabilities = AgentCapabilities(streaming = false),
        defaultInputModes = listOf("text"),
        defaultOutputModes = listOf("text"),
        skills = listOf(
            AgentSkill(
                id = "coding",
                name = "Coding Agent",
                description = "An AI agent that writes quality code",
                tags = listOf("coding","programming")
            )
        )
    )

    val agentExecutor = CodingAgentExecutor()
    val a2aServer = A2AServer(
        agentExecutor = agentExecutor,
        agentCard = agentCard
    )

    val serverTransport = HttpJSONRPCServerTransport(a2aServer)

    codinglogger.info { "Coding agent ready  on $CODING_HOST" }

    serverTransport.start(
        engineFactory = CIO,
        port = PORT,
        path = CODING_AGENT_PATH,
        wait = true,
        agentCard = agentCard,
        agentCardPath = CODING_AGENT_CARD_PATH,
    )

}