package com.example.demo.service;

import com.example.demo.models.CryptoInfo;
import com.example.demo.repository.CryptoPriceRepository;
import com.example.demo.tgBot.CryptoBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Service
public class RedisService {
    private final CryptoPriceRepository cryptoPriceRepository;


    private final CryptoBot cryptoBot;
    private final UserChatIDService userChatIDService;

    @Autowired
    public RedisService(CryptoPriceRepository cryptoPriceRepository, @Lazy CryptoBot cryptoBot, UserChatIDService userChatIDService) {
        this.cryptoPriceRepository = cryptoPriceRepository;
        this.cryptoBot = cryptoBot;
        this.userChatIDService = userChatIDService;
    }

    public void saveCryptoPrices(List<CryptoInfo> data) {
        data.forEach(cryptoInfo -> {
            String currentPriceKey = cryptoInfo.getSymbol();
            String previousPriceKey = "previous_" + cryptoInfo.getSymbol();
            String currentPrice = cryptoInfo.getPrice();
            String previousPrice = cryptoPriceRepository.find(previousPriceKey);

            // Сохраняем текущее значение как предыдущее для следующего сравнения
            cryptoPriceRepository.save(previousPriceKey, currentPrice);

            // Сохраняем текущее значение
            cryptoPriceRepository.save(currentPriceKey, currentPrice);


            if (previousPrice != null) {
                double currentPriceValue = Double.parseDouble(currentPrice);
                double previousPriceValue = Double.parseDouble(previousPrice);
                double changePercentage = ((currentPriceValue - previousPriceValue) / previousPriceValue) * 100;

                Set<Long> chatIds = userChatIDService.getChatIds();
                for (Long chatId : chatIds) {
                    // Получаем выбор пользователя
                    Integer userChoice = userChatIDService.getChoiceByChatId(chatId);
                    if (userChoice != 0 && Math.abs(changePercentage) >= userChoice) {
                        System.out.println(userChoice);
                        sendTelegramNotification(cryptoInfo.getSymbol(), changePercentage, chatId);
                    }
                }

            }
        });
    }

    private void sendTelegramNotification(String symbol, double changePercentage, Long chatId) {
        Integer notificationChoice = userChatIDService.getChoiceByChatId(chatId);
        if (notificationChoice == null || notificationChoice == 0) {
            return;
        }
        String direction = changePercentage >= 0 ? "↑" : "↓";
        String message = String.format("Цена %s изменилась на %.2f%% %s", symbol, Math.abs(changePercentage), direction);
        Set<Long> chatIds = userChatIDService.getChatIds();
        for (Long id : chatIds) {
            Integer userChoice = userChatIDService.getChoiceByChatId(id);
            if (userChoice != null) {
                // Проверяем, что изменение курса больше или равно выбору пользователя
                if (Math.abs(changePercentage) >= userChoice) {
                    try {
                        cryptoBot.sendNotification(String.valueOf(chatId), message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public String getAllDataAsJson(){
        try {
          return cryptoPriceRepository.getAllDataAsJson();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String findCryptoPrice(String symbol) {
        return cryptoPriceRepository.find(symbol);
    }

    public void deleteCryptoPrice(String symbol) {
        cryptoPriceRepository.delete(symbol);
    }

    public Double getCryptoPrice(String symbol) {
        return cryptoPriceRepository.getData(symbol);
    }


}
