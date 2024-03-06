package org.main;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Weather {
    private static final String API_KEY = Config.getYandexWeatherApiKey();
    private static final String API_URL = "https://api.weather.yandex.ru/v2/informers?lat=51.670833&lon=39.180556";

    private static final HttpClient client = HttpClient.newHttpClient();

    private static String getWeatherData() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("X-Yandex-API-Key", API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new IOException("API request failed with status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Произошла ошибка при получении данных.";
        }
    }

    public static String formattedWeatherData() {
        String json = getWeatherData();
        Gson gson = new Gson();

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        JsonObject factObject = jsonObject.getAsJsonObject("fact");

        int temperature = factObject.get("temp").getAsInt();
        int pressure_mm = factObject.get("pressure_mm").getAsInt();

        String toReturn = "";
        toReturn = "Температура: " + temperature + "°C" + "\n";
        toReturn += "Давление: " + pressure_mm + " мм рт. ст.";

        return toReturn;
    }
}
