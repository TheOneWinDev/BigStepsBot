package org.main;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class Bot extends TelegramLongPollingBot {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/tgWhitelist";
    private static final String JDBC_USER = "windev";
    private static final String JDBC_PASSWORD = "1234";

    public Bot() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS whitelist (user_id BIGINT PRIMARY KEY)";
            connection.prepareStatement(createTableSQL).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            Long userId = message.getFrom().getId();
            String text = message.getText();

            if (!isUserInWhitelist(userId)) {
                System.out.println("Пользователь не в whitelist: " + userId);
                return;
            }

            if ("/start".equals(text)) {
                sendStartMessage(message.getChatId());
            } else if ("/weather".equals(text.toLowerCase())) {
                sendWeatherMessage(message.getChatId());
            } else if ("/lastpost".equals(text.toLowerCase())) {
                Forwarder forwarder = new Forwarder();
                try {
                    forwarder.forwardPinnedPost(userId);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else {
                String response = "Привет, " + message.getFrom().getFirstName() + "! Это ответ бота.";
                SendMessage sendMessage = new SendMessage(message.getChatId().toString(), response);

                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendStartMessage(Long chatId) {
        String response = "Привет! Я бот BigSteps, изучи мои команды";
        SendMessage sendMessage = new SendMessage(chatId.toString(), response);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendWeatherMessage(Long chatId) {
        String weatherData = Weather.formattedWeatherData();

        String response = "Текущая погода в Воронеже:\n" + weatherData;

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
        return "6749qDzTvtE";
    }
}