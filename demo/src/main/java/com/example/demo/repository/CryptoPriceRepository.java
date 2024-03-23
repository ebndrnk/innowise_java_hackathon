package com.example.demo.repository;

import com.example.demo.models.CryptoInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
public class CryptoPriceRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public CryptoPriceRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void save(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public String find(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void saveData(List<CryptoInfo> data) {
        data.forEach(cryptoInfo -> {
            redisTemplate.opsForValue().set(cryptoInfo.getSymbol(), cryptoInfo.getPrice(), 21, TimeUnit.SECONDS);
        });
    }

    public Double getData(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Double.parseDouble(value) : null;
    }

    public String getAllDataAsJson() throws IOException {
        // Получаем все ключи
        Set<String> keys =  redisTemplate.keys("*");

        // Создаем карту для хранения данных
        Map<String, String> dataMap = new HashMap<>();

        // Заполняем карту данными из Redis
        for (String key : keys) {
            String value = redisTemplate.opsForValue().get(key);
            dataMap.put(key, value);
        }

        // Преобразуем карту в JSON
        String jsonData = objectMapper.writeValueAsString(dataMap);

        return jsonData;
    }
}
