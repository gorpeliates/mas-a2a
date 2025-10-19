import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import servers.CodingAgentServer
import servers.ReviewingAgentServer
import servers.TestingAgentServer

suspend fun main(): Unit = supervisorScope {
    launch { CodingAgentServer().start() }
    
    launch { ReviewingAgentServer().start() }
    
    launch { TestingAgentServer().start() }

    awaitCancellation()
}