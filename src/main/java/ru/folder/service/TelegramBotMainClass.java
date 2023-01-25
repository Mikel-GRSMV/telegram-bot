package ru.folder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.folder.config.TelegramBotConfiguration;

@Service
public class TelegramBotMainClass extends TelegramLongPollingBot {

    private TelegramBotConfiguration configuration;

    @Autowired
    public void setConfiguration(TelegramBotConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getBotToken() {
        return configuration.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        //Для того чтобы понять, что написал пользак, нужно убедиться в том, что там
        //есть, что проверять(избежать NullPointerException):
        //update.hasMessage() - проверил, есть ли сообщение.
        //update.getMessage().hasText() - проверил, есть ли в сообщении текст.
        if (update.hasMessage() && update.getMessage().hasText()) {
            //Получил текст из сообщения и положил в messageText:
            String messageText = update.getMessage().getText();
            //Чтобы бот мог мне ответить, ему нужно знать chatId
            //chatId - это идентификатор пользователя, так как с ботом может общаться много пользаков.
            //Этот параметр содержится в каждом Update и при посылке этот параметр должен использоваться.
            long chatId = update.getMessage().getChatId();

            //В зависимости от того, что мне прилетает в messageText, я буду выполнять действие:
            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;

                //Если пользак ввел неподдерживаемую команду, то выполнится default:
                default:
                    sendMessage(chatId, "This command was not recognized!");

            }
        }
    }

    @Override
    public String getBotUsername() {
        return configuration.getBotName();
    }

    private void startCommandReceived(long chatId, String firstNameUser) {
        String answer = "Hello, " + firstNameUser + ", let's go use this bot!";
        sendMessage(chatId, answer);
    }

    //Метод для отправки сообщений:
    private void sendMessage(long chatId, String textToSend) {
        //SendMessage - класс, который отвечает за отправку сообщений.
        SendMessage message = new SendMessage();
        //Устонавливаю идентификатор пользователя:
        message.setChatId(String.valueOf(chatId));
        //Устонавливаю текст сообщения:
        message.setText(textToSend);

        //Теперь попробую отправить сообщение(использую try/catch, так как может быть ошибка):
        try {
            execute(message);
        } catch (TelegramApiException exception) {
            exception.printStackTrace();
        }
    }

}
