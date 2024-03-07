//version 0.0.6

package org.main;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class Currency {
    private static final String API_KEY = Config.getExchangeRateApiKey();

    public static String getCurrencyRates() {
        try {
            String apiUrl = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/RUB";
            String jsonResponse = makeGetRequest(apiUrl);
            Map<String, Double> rates = parseRates(jsonResponse);
            StringBuilder output = new StringBuilder();
            DecimalFormat df = new DecimalFormat("#.##");
            output.append("1 USD to RUB: ").append(df.format(1 / rates.getOrDefault("USD", 0.0))).append("\n");
            output.append("1 EUR to RUB: ").append(df.format(1 / rates.getOrDefault("EUR", 0.0))).append("\n");
            output.append("1 CNY to RUB: ").append(df.format(1 / rates.getOrDefault("CNY", 0.0))).append("\n");

            return output.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "Ошибка сети: Не удалось получить курсы валют.";
        }
    }

    private static String makeGetRequest(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    private static Map<String, Double> parseRates(String jsonResponse) {
        Map<String, Double> rates = new HashMap<>();
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(jsonResponse);

        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonObject conversionRates = jsonObject.getAsJsonObject("conversion_rates");

            for (Map.Entry<String, JsonElement> entry : conversionRates.entrySet()) {
                String currencyCode = entry.getKey();
                double rate = entry.getValue().getAsDouble();
                rates.put(currencyCode, rate);
            }
        }

        return rates;
    }
}
