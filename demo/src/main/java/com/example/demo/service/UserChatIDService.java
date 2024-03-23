package com.example.demo.service;

import com.example.demo.models.UserChatID;
import com.example.demo.repository.UserChatIDRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserChatIDService {
    private final UserChatIDRepository userChatIDRepository;

    @Autowired
    public UserChatIDService(UserChatIDRepository userChatIDRepository) {
        this.userChatIDRepository = userChatIDRepository;
    }


    public void addChatId(Long chatId) {
        userChatIDRepository.addChatId(chatId);
    }

    public Set<Long> getChatIds() {
        return userChatIDRepository.getChatIds();
    }

    public void saveUserChoice(Long chatId, Integer choice) {
        // Предполагается, что у вас есть сущность UserChatID с полями chatId и choice
        UserChatID userChatID = userChatIDRepository.findByChatId(chatId);
        if (userChatID == null) {
            userChatID = new UserChatID();
            userChatID.setChatId(chatId);
        }
        userChatID.setChoice(choice);
        userChatIDRepository.save(userChatID);
    }

    public Integer getChoiceByChatId(Long chatId) {
        Integer choice = null;
        UserChatID userChatID = userChatIDRepository.findByChatId(chatId);
        if (userChatID != null) {
            choice = userChatID.getChoice();
        }
        return choice;
    }

    public boolean chatIdExists(Long chatId) {
        return userChatIDRepository.existById(chatId);
    }

    public void stopNotifications(Long chatId) {
        UserChatID userChatID = userChatIDRepository.findByChatId(chatId);
        if (userChatID != null) {
            userChatID.setChoice(0); // Устанавливаем выбор в 0, что означает остановку рассылки
            userChatIDRepository.save(userChatID);
        }
    }


}
