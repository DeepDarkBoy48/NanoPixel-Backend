package robin.discordbot.tutorial;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;

public class tool {


    public static void main(String[] args) {

        record WeatherForecast(
                String location,
                String forecast,
                int temperature) {}

        class WeatherForecastService {
            @Tool("Get the weather forecast for a location")
            WeatherForecast getForecast(@P("Location to get the forecast for") String location) {
                if (location.equals("Paris")) {
                    return new WeatherForecast("Paris", "sunny", 20);
                } else if (location.equals("London")) {
                    return new WeatherForecast("London", "rainy", 15);
                } else if (location.equals("Tokyo")) {
                    return new WeatherForecast("Tokyo", "warm", 32);
                } else {
                    return new WeatherForecast("Unknown", "unknown", 0);
                }
            }
        }

        interface WeatherAssistant {
            String chat(String userMessage);
        }

        WeatherForecastService weatherForecastService =
                new WeatherForecastService();

        ChatModel gemini = GoogleAiGeminiChatModel.builder()
                .apiKey("YOUR_GEMINI_API_KEY")
                .modelName("gemini-1.5-flash")
                .temperature(0.0)
                .build();

        WeatherAssistant weatherAssistant =
                AiServices.builder(WeatherAssistant.class)
                        .chatModel(gemini)
                        .tools(weatherForecastService)
                        .build();

        String tokyoWeather = weatherAssistant.chat(
                "What is the weather forecast for Tokyo?");

        System.out.println("Gemini> " + tokyoWeather);
// Gemini> The weather forecast for Tokyo is warm
//         with a temperature of 32 degrees.
    }

}
