package sample;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.*;
import java.util.Properties;

public class Bot extends TelegramLongPollingBot {

    static String botName;
    static String botToken;

    public static void initConfig()
    {

        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream("resources/config.properties");

        try {
            prop.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        botName = prop.getProperty("bot.name");
        botToken = prop.getProperty("bot.token");
    }

    public static void main(String[] args)
    {

        initConfig();

        try {

            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            try {
                telegramBotsApi.registerBot(new Bot());
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(Message message, String text)
    {

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        sendMessage.getReplyToMessageId();
        sendMessage.setText(text);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void onUpdateReceived(Update update) {

        Message message = update.getMessage();

        if (message != null && message.hasText())
        {

            String text = message.getText();

            try {
                sendMsg(message, Translator.translate(text));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getBotUsername() { return botName; }

    public String getBotToken() { return botToken; }
}