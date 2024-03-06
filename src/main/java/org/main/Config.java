//version 0.0.5

package org.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE_PATH = "config.properties";

    private static Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(CONFIG_FILE_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getJdbcUrl() {
        return properties.getProperty("JDBC_URL");
    }

    public static String getJdbcUser() {
        return properties.getProperty("JDBC_USER");
    }

    public static String getJdbcPassword() {
        return properties.getProperty("JDBC_PASSWORD");
    }

    public static String getYandexWeatherApiKey() {
        return properties.getProperty("YANDEX_WEATHER_API_KEY");
    }

    public static String getExchangeRateApiKey() {
        return properties.getProperty("EXCHANGE_RATE_API_KEY");
    }

    public static String getBotToken() {
        return properties.getProperty("BOT_TOKEN");
    }

    public static Long getForwardChatId() {
        return Long.parseLong(properties.getProperty("FORWARD_CHAT_ID"));
    }
}
