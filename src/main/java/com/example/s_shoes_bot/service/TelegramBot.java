package com.example.s_shoes_bot.service;

import com.example.s_shoes_bot.config.BotConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;


@Slf4j //из библиотеки lombok реализует логирование через переменную log.
@Component //аннотация позволяет автоматически создать экземпляр
public class TelegramBot extends TelegramLongPollingBot {  //есть еще класс WebHookBot (разница в том что WebHook уведомляет нас каждый раз при написании сообщения пользователе, LongPolling сам проверяет не написали ли ему (он более простой)

    @Autowired
    final BotConfig config;

    static final String ADMIN_CHAT_ID = "123";

    static final String INFO_TEXT = "Привет! Я бот s.shoes! Пока что я нахожусь в стадии разработки, но уже умею: \n"+ // \n - переносит текст на новую строчку
            "/start - здесь я тебя обниму-приподниму, потому что для меня ты лучший пользователь \n"+
            "/service - здесь я расскажу о том какие услуги есть в мастерской, а также сориентирую по стоимости \n" +
            "/makeAnAppointment - здесь я помогу записаться на наши услуги  \n" +
            "/contacts - здесь я покажу наши контакты и помогу тебе до нас добраться \n" +
            "/portfolio - здесь я предоставлю тебе ссылку на страницу в Instagram, там происходит магия!";

    public TelegramBot(BotConfig config){
        this.config = config;
        //создание кнопки "меню" с командами и их кратким описанием
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "приветственное сообщение"));
        listOfCommands.add(new BotCommand("/service", "услуги мастерской"));
        listOfCommands.add(new BotCommand("/contacts", "наши контакты и где мы находимся"));
        listOfCommands.add(new BotCommand("/makeAnAppointment", "записаться на сервис"));
        listOfCommands.add(new BotCommand("/portfolio", "ваша спасённая обувь"));
        listOfCommands.add(new BotCommand("/info", "подробная информация"));
        try {
            this.execute(new SetMyCommands(listOfCommands,new BotCommandScopeDefault(),null));
        }catch (TelegramApiException e){
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

            switch (messageText){
                case "/start":
                    startCommand(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/service":
                    serviceCommand(chatId);
                    break;
                case "/contacts":
                    contactsCommand(chatId);
                    break;
                case "/makeAnAppointment":
                    sendMessage(chatId,"здесь будет запись на услуги");
                    break;
                case "/portfolio":
                    sendMessage(chatId,"https://www.instagram.com/s.shoes.vrn/");
                    break;
                case "/info":
                    sendMessage(chatId, INFO_TEXT);
                    break;
                case "/gis":
                    sendMessage(chatId, "https://2gis.ru/voronezh/firm/70000001075216563");
                    break;
                case "/yandex":
                    sendMessage(chatId, "https://yandex.ru/maps/org/s_shoes/61886721382/?ll=39.189714%2C51.657149&z=16");
                    break;
                default:
                    sendMessage(chatId,"Я пока не умею отвечать на такие сообщения! Мои команды: /info ");
            }
        }

    }

    //метод для приветственного сообщения
    private void startCommand(long chatId, String name){
        String answer = "Привет, " + name + ", мы вернем твою обувь с небес на землю!";
        sendMessage(chatId, answer);
        log.info("Replied to user "+ name);                     //лог о том что мы ответили пользователю
    }

    private void serviceCommand(long chatId){
        String answer = "Наши услуги:\n"+
                "Растяжка:                      от_\n"+
                "Реставрация:                   от_\n"+
                "Покраска:                      от_\n"+
                "Замена задников:               от_\n"+
                "Профилактика:                  от_\n"+
                "Набойки:                       от_\n"+
                "Замена молний/застежек:        от_\n"+
                "Механическая чистка            от_\n" +
                "обуви из замши:                от_\n"+
                "Уход за классической обувью:   от_\n";
        sendMessage(chatId, answer);
    }

    private void contactsCommand(long chatId){
        String answer = "Мы находимся по адресу:\n"+
                "г. Воронеж ул.Кирова 24 этаж 1 \n"+
                "Вход сто стороны Красноармейского бульвара \n"+
                "\n"+
                "Как добраться:\n"+
                "/gis - на картах 2Gis\n"+
                "/yandex - на Яндекс картах \n"+
                "\n"+
                "Контактные номера: +79042110727 \n"+
                "                   +79525522646 \n"+
                "Часы работы:                   \n"+
                "ПН: 10:00–19:00            \n"+
                "ВТ: 10:00–19:00            \n"+
                "СР: 10:00–19:00            \n" +
                "ПТ: 10:00–19:00            \n"+
                "СБ: Выходной               \n"+
                "ВС: Выходной               \n";
        sendMessage(chatId, answer);
    }

    //метод для отправки сообщений
    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e){
            log.error("Error occurred "+ e.getMessage());
        }
    }
}