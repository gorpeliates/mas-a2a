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
import utils.*


class ReviewingAgentServer() : AgentServer{
    private val logger = KotlinLogging.logger {}

    override val agentCard = AgentCard(
        protocolVersion = "0.3.0",
        name = "Reviewing Agent",
        description = "An AI agent that reviews code and provides GitHub PR-like comments",
        url = REVIEWING_HOST + REVIEWING_AGENT_PATH,
        version = "1.0.0",
        preferredTransport = TransportProtocol.JSONRPC,
        additionalInterfaces = listOf(
            AgentInterface(
                url = REVIEWING_HOST + REVIEWING_AGENT_PATH,
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
    override val serverProperties: ServerProperties = ServerProperties(
        id = "reviewing-agent",
        agentPath = REVIEWING_AGENT_PATH,
        agentCardPath = REVIEWING_AGENT_CARD_PATH,
        port = REVIEWING_PORT,
        host = REVIEWING_HOST
    )

    override suspend fun start(){
        logger.info { "Starting reviewing agent server on $REVIEWING_HOST" }

        val agentExecutor = ReviewingAgentExecutor()
        val a2aServer = A2AServer(
            agentExecutor = agentExecutor,
            agentCard = agentCard
        )

        val serverTransport = HttpJSONRPCServerTransport(a2aServer)

        logger.info { "Reviewing agent ready on $REVIEWING_HOST" }

        serverTransport.start(
            engineFactory = CIO,
            port = REVIEWING_PORT,
            path = REVIEWING_AGENT_PATH,
            wait = true,
            agentCard = agentCard,
            agentCardPath = REVIEWING_AGENT_CARD_PATH,
        )
    }
}

