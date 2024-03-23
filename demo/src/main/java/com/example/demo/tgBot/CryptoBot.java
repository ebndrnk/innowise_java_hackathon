package com.example.demo.tgBot;

import com.example.demo.service.CryptoPriceService;
import com.example.demo.service.UserChatIDService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class CryptoBot extends TelegramLongPollingBot {

    private final UserChatIDService userChatIDService;





    @Autowired
    public CryptoBot(UserChatIDService userChatIDService, CryptoPriceService cryptoPriceService) {
        this.userChatIDService = userChatIDService;
    }

    @Override
    public String getBotUsername() {
        return "cryptoBusinessmenBot";
    }

    @Override
    public String getBotToken() {
        return "7113366080:AAFyNjrxT6F_doXIE0JhUtIeWR2RZnUfEk0";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

//            не успел это проверить
            if(userChatIDService.getChatIds().size() > 20) {
                messageText = "Not avaible";
            }
            /// <---_-----------------------------------
            if ("/start".equals(messageText)) {

                userChatIDService.addChatId(chatId); // Добавляем ID чата
                sendMessageWithMenu(String.valueOf(chatId));
            } else if ("3".equals(messageText) || "5".equals(messageText) || "10".equals(messageText) || "15".equals(messageText)) {
                // Сохраняем выбор пользователя
                userChatIDService.saveUserChoice(chatId, Integer.parseInt(messageText));
                // Отправляем сообщение о сохранении выбора
                try {
                    sendNotification(String.valueOf(chatId), "Выбор сохранен! теперь вам будут приходить лишь изменения более чем на " + messageText + "%, если желаете остановить рассылку напишите /start повторно и нажмите на Х");
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if ("ALL DATA".equals(messageText)) {
                // Обработка нажатия на кнопку "D" для получения файла JSON
                try {
                    sendJsonFile(String.valueOf(chatId));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // Проверяем, есть ли ID чата в базе данных
                if (!userChatIDService.chatIdExists(chatId)) {
                    sendStartMessage(String.valueOf(chatId));
                }
            }
        }
    }



    public void sendNotification(String chatId, String msg) throws TelegramApiException {
        var response = new SendMessage(chatId, msg);
        execute(response);
    }

    private void sendMessageWithMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите изменение при котором мы вас оповестим: 3%, 5%, 10%, 15%");

        // Создаем клавиатуру
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("3");
        row.add("5");
        row.add("10");
        row.add("15");
        row.add("ALL DATA");
        if(userChatIDService.getChoiceByChatId(Long.parseLong(chatId)) != 0)
            row.add("X");
        keyboard.add(row);



        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true); // Изменяем размер клавиатуры для лучшего вида
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }



    private void sendStartMessage(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Напишите любой текст, чтобы начать, или нажмите  /start"); // Изменено сообщение
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendJsonFile(String chatId) throws TelegramApiException {
        // Создаем сообщение с ссылкой на сайт
        String messageText = "Вот все данные о криптовалюте на данный момент: https://api.mexc.com/api/v3/ticker/price\n" +
                "Для более удобного поиска нажмите cntrl+f и введите название интересующей вас криптовалюты";

        // Отправляем сообщение пользователю
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId); // Преобразование Long в String
        sendMessage.setText(messageText);
        execute(sendMessage);
    }

}
