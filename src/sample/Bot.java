package sample;

import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.IOException;

public class Bot extends TelegramLongPollingBot {

    static String botName;
    static String botToken;

    static String targetLanguage = "en";

    public static void initConfig() {

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

    public static void main(String[] args) {

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

    public void sendMsg(Message message, String text) {

        SendMessage sendMessage = new SendMessage();

        sendMessage.setChatId(String.valueOf(message.getChatId()));
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);

        try {
//            setButtons(sendMessage);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void onUpdateReceived(Update update) {

        Message message = update.getMessage();

        if (message != null && message.hasText()) {

            String text = message.getText();

            switch (message.getText()) {

                case "/selectTargetLanguage":

                    // TODO: drop languages list : kind-of-done
                    SendMessage inlineMessage = prepareInline(message);

                    try {
                        execute(inlineMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    break;

                case "/anotherCommand":

                    // TODO: something
                    break;

                default:
                    sendMsg(
                            message,
                            // TODO: language status if switched
                            TranslateText.translateText(targetLanguage, text)
                    );
                    break;
            }
        }
        else if (update.hasCallbackQuery()) {

            String call_data = update.getCallbackQuery().getData();
            long message_id = update.getCallbackQuery().getMessage().getMessageId();
            long chat_id = update.getCallbackQuery().getMessage().getChatId();

            EditMessageText editMessageText = new EditMessageText();
            editMessageText.setChatId(String.valueOf(chat_id));
            editMessageText.setMessageId((int) message_id);
            editMessageText.setText("Selected " + call_data);

            targetLanguage = getCodeByName(call_data);

            try {
                execute(editMessageText);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getCodeByName(String name) {

        Translate translate = TranslateOptions.getDefaultInstance().getService();
        List<Language> languages = translate.listSupportedLanguages();

        for (Language language : languages) {

            if (language.getName().equals(name))
                return language.getCode();
        }

        return null;
    }

    // TODO: adequate solution for entire languages list (e.g. commented below) ?
    public SendMessage prepareInline(Message message) {

        SendMessage tempMessage = new SendMessage();
        tempMessage.setChatId(String.valueOf(message.getChatId()));
        tempMessage.setText("Please, select target language");

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

//        Translate translate = TranslateOptions.getDefaultInstance().getService();
//        List<Language> languages = translate.listSupportedLanguages();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton InlineKeyboardButton1 = new InlineKeyboardButton();
        InlineKeyboardButton1.setText("Russian (" + getCodeByName("Russian") + ")");
        InlineKeyboardButton1.setCallbackData("Russian");

        InlineKeyboardButton InlineKeyboardButton2 = new InlineKeyboardButton();
        InlineKeyboardButton2.setText("English (" + getCodeByName("English") + ")");
        InlineKeyboardButton2.setCallbackData("English");

        InlineKeyboardButton InlineKeyboardButton3 = new InlineKeyboardButton();
        InlineKeyboardButton3.setText("Japanese (" + getCodeByName("Japanese") + ")");
        InlineKeyboardButton3.setCallbackData("Japanese");

        rowInline.add(InlineKeyboardButton1);
        rowInline.add(InlineKeyboardButton2);
        rowInline.add(InlineKeyboardButton3);

        rowsInline.add(rowInline);

////        for (Language language : languages) {
//        for (int i = 0; i < languages.size(); i++) {
//
//            InlineKeyboardButton tempInlineKeyboardButton = new InlineKeyboardButton();
//
//            tempInlineKeyboardButton.setText(i+1 + ": " + languages.get(i).getName() + " (" + languages.get(i).getCode() + ")");
//            tempInlineKeyboardButton.setCallbackData(languages.get(i).getName());
//
//            rowInline.add(tempInlineKeyboardButton);
//
//            System.out.println("Before: " + rowInline);
//
//            // tried splitting into several rows..
//            if (i % 5 == 0 && i != languages.size() - 1)
//            {
//                rowsInline.add(rowInline);
//
//                System.out.println("In: " + rowInline);
//
////                rowInline.clear();
//            }
//
//            System.out.println("After: " + rowInline);
//        }

        markupInline.setKeyboard(rowsInline);
        tempMessage.setReplyMarkup(markupInline);

        return tempMessage;
    }

    public String getBotUsername() { return botName; }

    public String getBotToken() { return botToken; }
}