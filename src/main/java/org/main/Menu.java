//version 0.0.4

package org.main;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    public static void sendMenuMessage(Long chatId) {
        ReplyKeyboardMarkup replyMarkup = new ReplyKeyboardMarkup();
        replyMarkup.setResizeKeyboard(true);
        replyMarkup.setSelective(true);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("/weather"));
        row1.add(new KeyboardButton("/lastpost"));
        keyboard.add(row1);

        replyMarkup.setKeyboard(keyboard);

        SendMessage sendMessage = new SendMessage(chatId.toString(), "Выберите действие:");
        sendMessage.setReplyMarkup(replyMarkup);

        try {
            Bot.getInstance().execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
