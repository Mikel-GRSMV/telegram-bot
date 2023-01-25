package ru.folder.service;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.folder.config.TelegramBotConfiguration;
import ru.folder.entities.User;
import ru.folder.repository.UserRepository;

import java.sql.Timestamp;
import java.util.*;

//TODO: ПРОВЕСТИ РЕФАКТОРИНГ
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
        botCommandList.add(new BotCommand("/register", "You can check in"));
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

    //TODO: разбить на методы(Провести рефакторинг)
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":
                    registerUser(update.getMessage(), chatId);
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                case "/emoji":
                    sendEmoji(chatId);
                    break;
                case "/register":
                    register(chatId);
                    break;


                default:
                    sendMessage(chatId, "This command was not recognized!");

            }
        }
        else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();

            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callBackData.equals("YES_BUTTON")) {
                String text = "You pressed YES button";
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(String.valueOf(chatId));
                editMessageText.setText(text);
                editMessageText.setMessageId((int) messageId);

                try {
                    execute(editMessageText);
                } catch (TelegramApiException exception) {
                    exception.printStackTrace();
                }

            } else if (callBackData.equals("NO_BUTTON")) {
                String text = "You pressed NO button";
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(String.valueOf(chatId));
                editMessageText.setText(text);
                editMessageText.setMessageId((int) messageId);

                try {
                    execute(editMessageText);
                } catch (TelegramApiException exception) {
                    exception.printStackTrace();
                }
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

    //TODO: разбить на методы(Провести рефакторинг)
    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("weather");
        row.add("get random joke");
        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add("register");
        row.add("check my data");
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);
        try {
            execute(message);
        } catch (TelegramApiException exception) {
            exception.printStackTrace();
        }
    }

    //TODO: разбить на методы(Провести рефакторинг)
    private void registerUser(Message message, Long chatId) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            chatId = message.getChatId();
            Chat chat = message.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
        } else {
            System.out.println("Пользак существует!");
            String userName = message.getChat().getUserName();
            sendMessage(chatId, "Hello, " + userName + "!");
        }
    }

    //TODO: разбить на методы(Провести рефакторинг)
    private void register(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Do you want to register?");
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        yesButton.setText("Yes");
        yesButton.setCallbackData("YES_BUTTON");

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        noButton.setText("No");
        noButton.setCallbackData("NO_BUTTON");

        rowInline.add(yesButton);
        rowInline.add(noButton);

        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);

        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException exception) {
            exception.printStackTrace();
        }
    }

}
