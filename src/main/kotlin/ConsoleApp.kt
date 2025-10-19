import kotlinx.coroutines.runBlocking
import utils.*

/**
 * Interactive console application for communicating with the ClientAgent.
 */
fun main() = runBlocking {
    println("=".repeat(60))
    println("Welcome to the Client Agent Console!")
    println("=".repeat(60))
    println()

    val serverProperties = listOf(
        ServerProperties(
            id = "coding-agent",
            agentPath = CODING_AGENT_PATH,
            agentCardPath = CODING_AGENT_CARD_PATH,
            port = CODING_PORT,
            host = CODING_HOST
        ),
        ServerProperties(
            id = "reviewing-agent",
            agentPath = REVIEWING_AGENT_PATH,
            agentCardPath = REVIEWING_AGENT_CARD_PATH,
            port = REVIEWING_PORT,
            host = REVIEWING_HOST
        ),
        ServerProperties(
            id = "testing-agent",
            agentPath = TESTING_AGENT_PATH,
            agentCardPath = TESTING_AGENT_CARD_PATH,
            port = TESTING_PORT,
            host = TESTING_HOST
        )
    )

    println("Initializing Client Agent...")
    println("Connecting to agents:")
    serverProperties.forEach {
        println("  - ${it.id} at ${it.host}")
    }
    println()

    val clientAgent = try {
        ClientAgent(serverProperties)
    } catch (e: Exception) {
        println("ERROR: Failed to initialize Client Agent: ${e.message}")
        return@runBlocking
    }

    println("Client Agent initialized successfully!")
    println()
    println("Available commands:")
    println("  - Type your message to send to the agent")
    println("  - Type 'exit' or 'quit' to exit the application")
    println("  - Type 'help' to see this message again")
    println()

    // Main interaction loop
    while (true) {
        print("You: ")
        val input = readLine()?.trim()

        when {
            input.isNullOrBlank() -> {
                println("Please enter a message.")
                continue
            }
            input.equals("exit", ignoreCase = true) ||
            input.equals("quit", ignoreCase = true) -> {
                println()
                println("Goodbye!")
                break
            }
            input.equals("help", ignoreCase = true) -> {
                println()
                println("Available commands:")
                println("  - Type your message to send to the agent")
                println("  - Type 'exit' or 'quit' to exit the application")
                println("  - Type 'help' to see this message again")
                println()
                println("Example messages:")
                println("  - 'Write a function to calculate fibonacci numbers'")
                println("  - 'Review the code for potential bugs'")
                println("  - 'Create unit tests for the calculator class'")
                println()
                continue
            }
            else -> {
                println()
                println("Agent is processing your request...")
                println("-".repeat(60))

                try {
                    val response = clientAgent.run(input)
                    println("Agent: $response")
                } catch (e: Exception) {
                    println("ERROR: ${e.message}")
                    println("The agent may have encountered an issue. Please try again.")
                }

                println("-".repeat(60))
                println()
            }
        }
    }
}

