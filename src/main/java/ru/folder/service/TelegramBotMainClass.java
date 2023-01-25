package ru.folder.service;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.folder.config.TelegramBotConfiguration;
import ru.folder.entities.User;
import ru.folder.repository.UserRepository;

import java.sql.Timestamp;
import java.util.*;

@Service
public class TelegramBotMainClass extends TelegramLongPollingBot {

    private static final String HELP_TEXT = "This bot is created for test telegram-bot!\n\n" +
            "You can execute commands from the main menu on the left or by typing a command:\n\n" +
            "/start - \n\n" +
            "/help - \n\n" +
            "/emoji - ";
    private final TelegramBotConfiguration configuration;

    private UserRepository userRepository;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public TelegramBotMainClass(TelegramBotConfiguration configuration) {
        this.configuration = configuration;

        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/start", "Get welcome message"));
        botCommandList.add(new BotCommand("/help", "Info how to use this bot"));
        botCommandList.add(new BotCommand("/emoji", "Send you emoji"));
        try {
            this.execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return configuration.getBotName();
    }

    @Override
    public String getBotToken() {
        return configuration.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                case "/emoji":
                    sendEmoji(chatId);
                    break;

                default:
                    sendMessage(chatId, "This command was not recognized!");

            }
        }
    }

//----------------------------------------------- Служебные методы ---------------------------------------------------//

    private void startCommandReceived(long chatId, String firstNameUser) {
        String answer = "Hello, " + firstNameUser + ", let's go use this bot!";
        sendMessage(chatId, answer);
    }

    private void sendEmoji(long chatId) {
        ArrayList<String> emojiList = new ArrayList<>();
        emojiList.add(EmojiParser.parseToUnicode("😀"));
        emojiList.add(EmojiParser.parseToUnicode("😃"));
        emojiList.add(EmojiParser.parseToUnicode("😅"));
        emojiList.add(EmojiParser.parseToUnicode("😉"));
        emojiList.add(EmojiParser.parseToUnicode("🥰"));
        emojiList.add(EmojiParser.parseToUnicode("😊"));
        emojiList.add(EmojiParser.parseToUnicode("☺️"));
        emojiList.add(EmojiParser.parseToUnicode("🤩"));
        emojiList.add(EmojiParser.parseToUnicode("😏"));

        Random random = new Random();
        int randomIndex = random.nextInt(emojiList.size());

        String answer = emojiList.get(randomIndex);

        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException exception) {
            exception.printStackTrace();
        }
    }

    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            Long chatId = message.getChatId();
            Chat chat = message.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
        } else {
            //TODO: допилить логику
            System.out.println("Пользак существует!");
        }
    }

}
