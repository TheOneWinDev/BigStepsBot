package org.main;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Bot extends TelegramLongPollingBot {
    // JDBC URL, username, and password of MySQL server
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/tgWhitelist";
    private static final String JDBC_USER = "USER";
    private static final String JDBC_PASSWORD = "PSWD";

    public Bot() {
        // Initialize database connection and create the whitelist table if not exists
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

            if ("/start".equals(text)) {
                // Пользователь начал диалог, можем отправить приветственное сообщение
                sendStartMessage(message.getChatId());
            } else if ("/weather".equals(text.toLowerCase())) {
                // Пользователь запросил погоду
                sendWeatherMessage(message.getChatId());
            } else if (isUserInWhitelist(userId)) {
                // Пользователь в whitelist, поэтому отправляем ответ
                String response = "Привет, " + message.getFrom().getFirstName() + "! Это ответ бота.";
                SendMessage sendMessage = new SendMessage(message.getChatId().toString(), response);

                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else {
                // Пользователь не в whitelist, поэтому игнорируем сообщение
                System.out.println("Пользователь не в whitelist: " + userId);
            }
        }
    }

    private void sendStartMessage(Long chatId) {
        // Отправляем приветственное сообщение
        String response = "Привет! Я бот. Чтобы узнать погоду, напиши /weather.";
        SendMessage sendMessage = new SendMessage(chatId.toString(), response);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendWeatherMessage(Long chatId) {
        // Получаем данные о погоде
        String weatherData = Weather.formattedWeatherData();

        // Формируем полный ответ о погоде
        String response = "Вот текущая погода:\n" + weatherData;

        // Отправляем сообщение с полной информацией о погоде
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
        // Верните имя вашего бота
        return "YourBotName";
    }

    @Override
    public String getBotToken() {
        // Верните токен вашего бота
        return "TOKEN";
    }
}
