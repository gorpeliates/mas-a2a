import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import io.github.cdimascio.dotenv.dotenv

class ClientAgent {

    val executor: PromptExecutor = simpleOpenAIExecutor(dotenv()["OPENAI_API_KEY"])

    val AIAgentConfig = AIAgentConfig(
        prompt("ClientAgent") {
            system(
                "You are a client agent that communicates with other agents to complete" +
                        "the tasks provided."
            )
        },
        model = OpenAIModels.Chat.GPT5Mini,
        maxAgentIterations = 100
    )

}