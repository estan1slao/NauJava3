package ru.stanislav.NauJava3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import ru.stanislav.NauJava3.entity.Transaction;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataConfig {

    @Bean
    @Scope(BeanDefinition.SCOPE_SINGLETON)
    public List<Transaction> transactionContainer() {
        return new ArrayList<>();
    }
}