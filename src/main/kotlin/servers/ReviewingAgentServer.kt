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


const val REVIEWING_AGENT_PATH = "/reviewing-agent"
const val REVIEWING_AGENT_CARD_PATH = "$REVIEWING_AGENT_PATH/.well-known/agent-card.json"
private val reviewingLogger = KotlinLogging.logger {}
const val REVIEWING_PORT = 9997
const val reviewingHost  = "http://localhost:$REVIEWING_PORT"

suspend fun reviewingMain(){
    reviewingLogger.info { "Starting reviewing agent server on $reviewingHost" }

    val agentCard = AgentCard(
        protocolVersion = "0.3.0",
        name = "Reviewing Agent",
        description = "An AI agent that reviews code and provides GitHub PR-like comments",
        url = reviewingHost + REVIEWING_AGENT_PATH,
        version = "1.0.0",
        preferredTransport = TransportProtocol.JSONRPC,
        additionalInterfaces = listOf(
            AgentInterface(
                url = reviewingHost + REVIEWING_AGENT_PATH,
                transport = TransportProtocol.JSONRPC
            )
        ),
        capabilities = AgentCapabilities(streaming = false),
        defaultInputModes = listOf("text"),
        defaultOutputModes = listOf("text"),
        skills = listOf(
            AgentSkill(
                id = "reviewing",
                name = "Reviewing Agent",
                description = "An AI agent that reviews code and provides GitHub PR-like comments",
                tags = listOf("code-review", "quality-assurance", "best-practices")
            )
        )
    )

    val agentExecutor = ReviewingAgentExecutor()
    val a2aServer = A2AServer(
        agentExecutor = agentExecutor,
        agentCard = agentCard
    )

    val serverTransport = HttpJSONRPCServerTransport(a2aServer)

    reviewingLogger.info { "Reviewing agent ready on $reviewingHost" }

    serverTransport.start(
        engineFactory = CIO,
        port = REVIEWING_PORT,
        path = REVIEWING_AGENT_PATH,
        wait = true,
        agentCard = agentCard,
        agentCardPath = REVIEWING_AGENT_CARD_PATH,
    )

}
