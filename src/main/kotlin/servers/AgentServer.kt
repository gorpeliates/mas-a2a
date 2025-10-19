package servers

import ai.koog.a2a.model.AgentCard

interface AgentServer {

    val agentCard: AgentCard

    suspend fun start()
}