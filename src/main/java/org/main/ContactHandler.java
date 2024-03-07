//version 0.0.6

package org.main;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ContactHandler {


    private static boolean isContacting = false;

    public static void handleContact(Message message) {
        Long chatId = message.getChatId();
        sendContactPrompt(chatId);
        isContacting = true;
    }


    public static boolean isUserContacting(Long userId) {
        return isContacting;
    }

    private static void sendContactPrompt(Long chatId) {
        String prompt = "Введите ваше обращение ниже. Для отмены напишите 'отменить'.";
        SendMessage sendMessage = new SendMessage(chatId.toString(), prompt);

        try {
            Bot.getInstance().execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public static void processContactMessage(Message message) {
        String text = message.getText();
        Long chatId = message.getChatId();

        if ("отменить".equalsIgnoreCase(text)) {
            sendCancelledMessage(chatId);
            return;
        }

        Long adminId = Long.valueOf(Config.getAdminId());
        forwardMessageToAdmin(message, chatId, adminId);
        isContacting = false;
    }

    private static void forwardMessageToAdmin(Message message, Long chatId, Long adminId) {
        SendMessage forwardMessage = new SendMessage();
        forwardMessage.setChatId(adminId);
        forwardMessage.setText("Новое сообщение от пользователя с ID " + chatId + ":\n" + message.getText());

        try {
            Bot.getInstance().execute(forwardMessage);
            sendSuccessMessage(chatId);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static void sendCancelledMessage(Long chatId) {
        String response = "Отправка отменена.";
        SendMessage sendMessage = new SendMessage(chatId.toString(), response);

        try {
            Bot.getInstance().execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static void sendSuccessMessage(Long chatId) {
        String response = "Ваше обращение успешно отправлено.";
        SendMessage sendMessage = new SendMessage(chatId.toString(), response);

        try {
            Bot.getInstance().execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
