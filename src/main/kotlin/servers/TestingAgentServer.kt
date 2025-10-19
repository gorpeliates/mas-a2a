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
import utils.ServerProperties


private const val TESTING_AGENT_PATH = "/testing-agent"
private const val TESTING_AGENT_CARD_PATH = "$TESTING_AGENT_PATH/.well-known/agent-card.json"
private const val TESTING_PORT = 9998
private const val TESTING_HOST  = "http://localhost:$TESTING_PORT"

class TestingAgentServer : AgentServer {
    private val logger = KotlinLogging.logger {}

    override val agentCard = AgentCard(
        protocolVersion = "0.3.0",
        name = "Testing Agent",
        description = "An AI agent that creates comprehensive unit tests for code",
        url = TESTING_HOST + TESTING_AGENT_PATH,
        version = "1.0.0",
        preferredTransport = TransportProtocol.JSONRPC,
        additionalInterfaces = listOf(
            AgentInterface(
                url = TESTING_HOST + TESTING_AGENT_PATH,
                transport = TransportProtocol.JSONRPC
            )
        ),
        capabilities = AgentCapabilities(streaming = false),
        defaultInputModes = listOf("text"),
        defaultOutputModes = listOf("text"),
        skills = listOf(
            AgentSkill(
                id = "testing",
                name = "Testing Agent",
                description = "An AI agent that creates comprehensive unit tests for code",
                tags = listOf("testing", "unit-tests", "quality-assurance")
            )
        )
    )
    override val serverProperties: ServerProperties = ServerProperties(
        id = "testing-agent",
        agentPath = TESTING_AGENT_PATH,
        agentCardPath = TESTING_AGENT_CARD_PATH,
        port = TESTING_PORT,
        host = TESTING_HOST
    )

    override suspend fun start(){
        logger.info { "Starting testing agent server on $TESTING_HOST" }



        val agentExecutor = TestingAgentExecutor()
        val a2aServer = A2AServer(
            agentExecutor = agentExecutor,
            agentCard = agentCard
        )

        val serverTransport = HttpJSONRPCServerTransport(a2aServer)

        logger.info { "Testing agent ready on $TESTING_HOST" }

        serverTransport.start(
            engineFactory = CIO,
            port = TESTING_PORT,
            path = TESTING_AGENT_PATH,
            wait = true,
            agentCard = agentCard,
            agentCardPath = TESTING_AGENT_CARD_PATH,
        )

    }

}
