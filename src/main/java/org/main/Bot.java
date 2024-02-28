//version 0.0.3

package org.main;

import org.telegram.telegrambots.bots.TelegramLongPollingBot; // Use TelegramLongPollingBot as base class
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class Bot extends TelegramLongPollingBot {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/tgWhitelist";
    private static final String JDBC_USER = "1111";
    private static final String JDBC_PASSWORD = "1111";

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

    private static final Long ChatId = 1111L;

    // Change from Long to long
    private void forwardPinnedPost(long userId) throws TelegramApiException {
        try {
            // Получить информацию о чате
            GetChat getChat = new GetChat();
            getChat.setChatId(String.valueOf(ChatId)); // Предполагая, что ChatId - это переменная класса
            Chat chat = execute(getChat);

            // Проверить, есть ли в чате закрепленное сообщение
            if (chat != null && chat.getPinnedMessage() != null) {
                Message pinnedMessage = chat.getPinnedMessage();

                if (pinnedMessage != null) {
                    // Обрабатываем фото
                    if (pinnedMessage.hasPhoto()) {
                        PhotoSize bestPhoto = pinnedMessage.getPhoto().stream()
                                .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
                                .findFirst().orElse(null);

                        if (bestPhoto != null) {
                            String fileId = bestPhoto.getFileId();
                            SendPhoto photo = new SendPhoto();
                            photo.setChatId(String.valueOf(userId));

                            // Create an InputFile object from the fileId
                            photo.setPhoto(new InputFile(fileId));

                            photo.setCaption(pinnedMessage.getCaption());
                            execute(photo);
                        } else {
                            // Не удалось получить фото
                            System.out.println("[ID " + userId + "] Не удалось получить фото из закреплённого сообщения.");
                        }
                    } else {
                        // Обрабатываем другие типы сообщений
                        String text = pinnedMessage.getText();
                        execute(new SendMessage(String.valueOf(userId), text));
                    }

                    // Отправляем сообщение об успехе
                    System.out.println("[ID " + userId + "] Закреплённое сообщение отправлено в личные сообщения.");
                } else {
                    // Не удалось получить информацию о закрепленном сообщении
                    System.out.println("[ID " + userId + "] Не удалось получить информацию о закреплённом сообщении.");
                }
            } else {
                // В чате нет закрепленного сообщения
                System.out.println("[ID " + userId + "] Не удалось получить фото из закреплённого сообщения.");
            }
        } catch (TelegramApiException e) {
            // Обработка исключения Telegram API
            e.printStackTrace();
            System.out.println("[ID " + userId + "] Ошибка при пересылке закреплённого сообщения.");
        }
    }


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            Long userId = message.getFrom().getId();
            String text = message.getText();

            if ("/start".equals(text)) {
                sendStartMessage(message.getChatId());
            } else if ("/weather".equals(text.toLowerCase())) {
                sendWeatherMessage(message.getChatId());
            } else if ("/lastpost".equals(text.toLowerCase())) {
                try {
                    forwardPinnedPost(userId);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (isUserInWhitelist(userId)) {
                String response = "Привет, " + message.getFrom().getFirstName() + "! Это ответ бота.";
                SendMessage sendMessage = new SendMessage(message.getChatId().toString(), response);

                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Пользователь не в whitelist: " + userId);
            }
        }
    }
    private void sendStartMessage(Long chatId) {
        String response = "Привет! Я бот. Чтобы узнать погоду, напиши /weather.";
        SendMessage sendMessage = new SendMessage(chatId.toString(), response);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendWeatherMessage(Long chatId) {
        String weatherData = Weather.formattedWeatherData();

        String response = "Вот текущая погода:\n" + weatherData;

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
        return "6749548121:AAHPCWV8iYvMwt3zsdUV2HzjMu2WRiZHmlA";
    }
}
