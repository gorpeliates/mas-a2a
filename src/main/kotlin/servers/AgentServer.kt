package servers

import ai.koog.a2a.model.AgentCard
import utils.ServerProperties

interface AgentServer {

    val agentCard: AgentCard
    val serverProperties : ServerProperties
    suspend fun start()
}