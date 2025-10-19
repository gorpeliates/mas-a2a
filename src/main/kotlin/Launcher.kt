import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

suspend fun main(): Unit = supervisorScope {
    launch { servers.main() }
    
    launch { servers.testingMain() }
    
    launch { servers.reviewingMain() }

    awaitCancellation()
}