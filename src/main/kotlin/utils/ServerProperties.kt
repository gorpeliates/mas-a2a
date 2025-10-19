package utils

data class ServerProperties(
    val id : String = "unknown-agent",
    val agentPath: String,
    val agentCardPath: String,
    val port : Int,
    val host : String
)
