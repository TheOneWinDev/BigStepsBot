package org.main;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Comparator;

public class Forwarder extends TelegramLongPollingBot {

    private final Long chatId;

    public Forwarder() {
        chatId = Config.getForwardChatId();
    }

    public void forwardPinnedPost(long userId) throws TelegramApiException {
        try {
            GetChat getChat = new GetChat();
            getChat.setChatId(String.valueOf(chatId));
            Chat chat = execute(getChat);

            if (chat != null && chat.getPinnedMessage() != null) {
                Message pinnedMessage = chat.getPinnedMessage();

                if (pinnedMessage != null) {
                    if (pinnedMessage.hasPhoto()) {
                        PhotoSize bestPhoto = pinnedMessage.getPhoto().stream()
                                .sorted(Comparator.comparing(PhotoSize::getFileSize).reversed())
                                .findFirst().orElse(null);

                        if (bestPhoto != null) {
                            String fileId = bestPhoto.getFileId();
                            SendPhoto photo = new SendPhoto();
                            photo.setChatId(String.valueOf(userId));
                            photo.setPhoto(new InputFile(fileId));
                            photo.setCaption(pinnedMessage.getCaption());
                            execute(photo);
                        } else {
                            System.out.println("[ID " + userId + "] Failed to get a photo from the pinned message.");
                        }
                    } else {
                        String text = pinnedMessage.getText();
                        execute(new SendMessage(String.valueOf(userId), text));
                    }
                    System.out.println("[ID " + userId + "] Pinned message forwarded to private messages.");
                } else {
                    System.out.println("[ID " + userId + "] Failed to get information about the pinned message.");
                }
            } else {
                System.out.println("[ID " + userId + "] Failed to get a photo from the pinned message.");
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
            System.out.println("[ID " + userId + "] Error forwarding pinned message.");
        }
    }

    @Override
    public String getBotUsername() {
        return "YourBotName";
    }

    @Override
    public void onUpdateReceived(Update update) {
    }

    @Override
    public String getBotToken() {
        return Config.getBotToken();
    }
}
