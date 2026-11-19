package ru.stanislav.NauJava3.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    @PostConstruct
    public void init() {
        System.out.println("Конфигурация приложения загружена.");
    }
}