package ru.folder.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
//Указывается для того чтобы считывать файл конфигурации(но пока работает и без этой аннотации):
//@PropertySource("application.properties")
public class TelegramBotConfiguration {
    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String token;

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
