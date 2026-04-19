package drone;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SimulationInsightService {

    @Autowired
    private OpenAIClient client;

    @Value("${openai.model}")
    private String model;

    public String generateInsights(SimulationResult result) {
        // System prompt sets the analyst persona and constrains output length.
        // Keeping it under 150 words ensures the response fits neatly in a REST response.
        String systemPrompt = """
                You are a logistics operations analyst specialising in autonomous drone delivery.
                Analyse simulation results and provide concise, actionable business insights.
                Focus on: delivery performance, scheduling efficiency, and fleet sizing.
                Keep the response under 150 words.
                """;

        // User prompt injects the actual simulation metrics as structured text.
        String userPrompt = String.format("""
                Analyse this drone delivery simulation result:
                - Scenario: %s
                - Fleet size: %d drones
                - Total parcels: %d
                - Parcels delivered: %d
                - Average delivery time: %.1f ticks
                - Fastest delivery: %d ticks
                - Slowest delivery: %d ticks
                - Total simulation duration: %d ticks
                Provide business insights and optimisation recommendations.
                """,
                result.getScenario(),
                result.getDroneCount(),
                result.getTotalParcels(),
                result.getDeliveredCount(),
                result.getAverageDeliveryTime(),
                result.getMinDeliveryTime(),
                result.getMaxDeliveryTime(),
                result.getTotalTicks());

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.of(model))
                .addSystemMessage(systemPrompt)
                .addUserMessage(userPrompt)
                .build();

        // choices().get(0) is the first (and only) completion candidate.
        // content() returns Optional<String> — orElse guards against empty responses.
        return client.chat().completions().create(params)
                .choices().get(0)
                .message().content()
                .orElse("No insights available.");

    }
}
