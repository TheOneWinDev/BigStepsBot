//version 0.0.6.5

package org.main;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.*;

public class Bot extends TelegramLongPollingBot {
    private static final String JDBC_URL = Config.getJdbcUrl();
    private static final String JDBC_USER = Config.getJdbcUser();
    private static final String JDBC_PASSWORD = Config.getJdbcPassword();

    private static Bot instance;

    public Bot() {
        instance = this;
        initializeDatabase();
    }

    public static Bot getInstance() {
        return instance;
    }

    private void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS whitelist (user_id BIGINT PRIMARY KEY)";
            connection.prepareStatement(createTableSQL).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return; // Handle only text messages
        }

        Message message = update.getMessage();
        Long userId = message.getFrom().getId();
        String text = message.getText().toLowerCase(); // Handle commands case-insensitively

        if (!isUserInWhitelist(userId)) {
            System.out.println("User not in whitelist: " + userId);
            return;
        }

        switch (text) {
            case "/start":
                sendStartMessage(message.getChatId());
                break;
            case "/weather":
                sendWeatherMessage(message.getChatId());
                break;
            case "/lastpost":
                try {
                    new Forwarder().forwardPinnedPost(userId);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                break;
            case "/menu":
                Menu.sendMenuMessage(message.getChatId());
                break;
            case "/currency":
                SendMessage sendMessage = new SendMessage(message.getChatId().toString(), Currency.getCurrencyRates());
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                break;
            case "/contact":
                ContactHandler.handleContact(message);
                break;
            default:
                sendUnrecognizedCommandMessage(message.getChatId());
        }
    }

    private void sendUnrecognizedCommandMessage(Long chatId) {
        String response = "Я вас не понимаю. Посмотрите список доступных команд с помощью /menu";
        SendMessage sendMessage = new SendMessage(chatId.toString(), response);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void sendStartMessage(Long chatId) {
        String response = "Hello! I'm the BigSteps bot, explore my commands.";
        SendMessage sendMessage = new SendMessage(chatId.toString(), response);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendWeatherMessage(Long chatId) {
        String weatherData = Weather.formattedWeatherData();

        String response = "Current weather in Voronezh:\n" + weatherData;

        SendMessage sendMessage = new SendMessage(chatId.toString(), response);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private boolean isUserInWhitelist(Long userId) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            String query = "SELECT * FROM whitelist WHERE user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getBotUsername() {
        return "YourBotName";
    }

    @Override
    public String getBotToken() {
        return Config.getBotToken();
    }
}
