package com.example.s_shoes_bot.service;
import com.example.s_shoes_bot.config.BotConfig;
import com.example.s_shoes_bot.model.User;
import com.example.s_shoes_bot.repository.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.ArrayList;
import java.util.List;
import static com.example.s_shoes_bot.service.Constants.*;


@Slf4j //из библиотеки lombok реализует логирование через переменную log.
@Component //аннотация позволяет автоматически создать экземпляр
public class TelegramBot extends TelegramLongPollingBot {  //есть еще класс WebHookBot (разница в том что WebHook уведомляет нас каждый раз при написании сообщения пользователе, LongPolling сам проверяет не написали ли ему (он более простой)

    @Autowired
    private UserRepository userRepository;
    @Autowired
    final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/service", "услуги"));
        listofCommands.add(new BotCommand("/contacts", "контакты"));
        listofCommands.add(new BotCommand("/makeAnAppointment", "запись на услугу"));
        listofCommands.add(new BotCommand("/portfolio", "фото работ"));
        listofCommands.add(new BotCommand("/info", "информация о командах"));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    //реализация метода LongPooling
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    //реализация метода LongPooling
    @Override
    public String getBotToken() {
        return config.getToken();
    }

    //реализация основного метода общения с пользователем (главный метод приложения)
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.contains("/send") && config.getOwnerId() == chatId) {  //проверили что написал именно админ с его уникальным chatID
                var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" "))); //взяли все что написал админ после пробела вместе со смайликами
                var users = userRepository.findAll();  //по базе взяли всех наших пользователей
                for (User user : users) {
                    prepareAndSendMessage(user.getChatId(), textToSend);
                }
            } else {

                switch (messageText) {
                    case "/start":
                        startCommand(chatId, update.getMessage().getChat().getFirstName());
                        break;
                    case "/service":
                        prepareAndSendMessage(chatId, SERVICE);
                        break;
                    case "/contacts":
                        prepareAndSendMessage(chatId, CONTACTS);
                        break;
                    case "/makeAnAppointment":
                        prepareAndSendMessage(chatId, "здесь будет запись на услуги");
                        registerUser(update.getMessage());
                        break;
                    case "/portfolio":
                        prepareAndSendMessage(chatId, INSTAGRAM);
                        break;
                    case "/info":
                        prepareAndSendMessage(chatId, INFO_TEXT);
                        break;
                    case "/gis":
                        prepareAndSendMessage(chatId, TWO_GIS);
                        break;
                    case "/yandex":
                        prepareAndSendMessage(chatId, YANDEX);
                        break;
                    default:
                        prepareAndSendMessage(chatId, "Я пока не знаю как на это ответить! Мои команды: /info ");
                }
            }
        }
    }

    //метод регистрации пользователя
    private void registerUser(Message msg) {

        var chatId = msg.getChatId();
        var chat = msg.getChat();

        User user = new User();
        user.setChatId(chatId);
        user.setFirstName(chat.getFirstName());
        user.setLastName(chat.getLastName());
        user.setUserName(chat.getUserName());
        /*user.setDate(new Timestamp(System.currentTimeMillis()));*/

        userRepository.save(user);
        log.info("user saved: " + user);
    }

    //метод для приветственного сообщения
    private void startCommand(long chatId, String name) {
        // добавление смайликов в строку (на сайте эмоджипедиа)
        String answer = EmojiParser.parseToUnicode("Привет, " + name + ", мы вернем твою обувь с небес на землю!" + " :blush:");
        prepareAndSendMessage(chatId, answer);
        log.info("Replied to user " + name);                     //лог о том что мы ответили пользователю
    }


    //метод подготовки сообщения и его отправки
    private void prepareAndSendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    //метод только для отправки готового сообщения
    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }
}