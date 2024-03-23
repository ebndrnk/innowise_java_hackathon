package com.example.demo.service;

import com.example.demo.models.CryptoInfo;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@EnableScheduling
@Service
public class CryptoPriceService {
    private final RedisService redisService;

    public CryptoPriceService(RedisService redisService) {
        this.redisService = redisService;
    }

    public List<CryptoInfo> getCryptoPrice() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.mexc.com/api/v3/ticker/price";
        List<CryptoInfo> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, null, new ParameterizedTypeReference<List<CryptoInfo>>() {}).getBody();
        return response;
    }

    @Scheduled(fixedRate = 5000)
    public String autoSaveData() {
        List<CryptoInfo> cryptoInfoList = getCryptoPrice();
        redisService.saveCryptoPrices(cryptoInfoList);
        return "Data saved successfully";
    }

    public String getAllDataAsJson(){
        return redisService.getAllDataAsJson();

    }
}
