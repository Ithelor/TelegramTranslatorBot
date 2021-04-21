package sample;

import com.google.cloud.translate.Language;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResult;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.InputStream;
import java.util.*;
import java.io.IOException;

import static org.telegram.telegrambots.meta.api.methods.ParseMode.HTML;

public class Bot extends TelegramLongPollingBot {

    static String botName;
    static String botToken;

    static String targetLanguageCode = "en";
    static String targetLanguageName = "English";

    static InlineKeyboardMarkup firstInlineMarkup = new InlineKeyboardMarkup();
    static InlineKeyboardMarkup secondInlineMarkup = new InlineKeyboardMarkup();

    static List<List<InlineKeyboardButton>> firstMarkupRowsInline = new ArrayList<>();
    static List<List<InlineKeyboardButton>> secondMarkupRowsInline = new ArrayList<>();

    static Translate translate = TranslateOptions.getDefaultInstance().getService();

    static boolean AUTO_MODE = false;
    static boolean SPECIFIC_MODE = false;

    private static final Integer CACHETIME = 0;

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
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void onUpdateReceived(Update update) {

        Message message = update.getMessage();

        if (update.hasInlineQuery()) {

            handleInlineQuery(update.getInlineQuery());
        }
        else if (message != null && message.hasText()) {

            String text = message.getText();

            switch (message.getText()) {

                case "/stl":

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
                            // TODO: switch languages + notification
                            TranslateText.translateText(targetLanguageCode, text)
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

            switch (call_data)
            {

                case (">"):
                    editMessageText.setReplyMarkup(secondInlineMarkup);
                    editMessageText.setText("Please, select target language - page 2/2");
                    break;

                case ("<"):
                    editMessageText.setReplyMarkup(firstInlineMarkup);
                    editMessageText.setText("Please, select target language - page 1/2");
                    break;

                default:
                    targetLanguageCode = getCodeByName(call_data);
                    targetLanguageName = call_data;
                    editMessageText.setText("Selected " + call_data);
                    break;
            }

            try {
                execute(editMessageText);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleInlineQuery(InlineQuery inlineQuery) {

        String query = inlineQuery.getQuery();

        try {
            if (!query.isEmpty()) {

                AnswerInlineQuery answerInlineQuery = new AnswerInlineQuery();
                answerInlineQuery.setInlineQueryId(inlineQuery.getId());
                answerInlineQuery.setResults(prepareArticle(inlineQuery.getQuery()));
                answerInlineQuery.setCacheTime(CACHETIME);

                execute(answerInlineQuery);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private static List<InlineQueryResult> prepareArticle(String query) {

        List<InlineQueryResult> result = new ArrayList<>();

        InputTextMessageContent messageContent = new InputTextMessageContent();
        messageContent.setMessageText(
                "Translation (" + getNameByCode(targetLanguageCode) + "): " + TranslateText.translateText(targetLanguageCode, query) +
                "\nOriginal (" + getNameByCode(translate.detect(query).getLanguage()) + "): " + query
        );

        InlineQueryResultArticle article = new InlineQueryResultArticle();
        article.setInputMessageContent(messageContent);
        article.setId("inline_query_translation");
        article.setTitle(
                "Translation (" + getNameByCode(targetLanguageCode) + "): " + TranslateText.translateText(targetLanguageCode, query)
        );
        article.setDescription(
                "Original (" + getNameByCode(translate.detect(query).getLanguage()) + "): " + query
        );

        result.add(article);
        return result;
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

    public static String getNameByCode(String code) {

        Translate translate = TranslateOptions.getDefaultInstance().getService();
        List<Language> languages = translate.listSupportedLanguages();

        for (Language language : languages) {

            if (language.getCode().equals(code))
                return language.getName();
        }

        return null;
    }

    public SendMessage prepareInline(Message message) {

        // TODO: gayISH - find an adequate solution
        if (firstInlineMarkup.getKeyboard() != null && secondInlineMarkup.getKeyboard() != null) {

            firstInlineMarkup.getKeyboard().clear();
            secondInlineMarkup.getKeyboard().clear();
        }

        SendMessage tempMessage = new SendMessage();
        tempMessage.setChatId(String.valueOf(message.getChatId()));
        tempMessage.setText("Please, select target language - page 1/2");
        tempMessage.setChatId(String.valueOf(message.getChatId()));
        tempMessage.setReplyToMessageId(message.getMessageId());

        Translate translate = TranslateOptions.getDefaultInstance().getService();
        List<Language> languages = translate.listSupportedLanguages();

        int buttonsPerRow = 4;
        double buttonRows = Math.ceil(Double.parseDouble(String.valueOf(languages.size())) / buttonsPerRow / 2
                + 1); // 1 == number of auxiliary (navigation) rows

        int languageIndex = 0;

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton tempInlineKeyboardButton;

        for (int i = 0; i < buttonRows; i++)
        {

            rowInline = new ArrayList<>();

            for (int j = 0; j < buttonsPerRow; j++) {

                tempInlineKeyboardButton = new InlineKeyboardButton();
                tempInlineKeyboardButton.setText(languages.get(languageIndex).getName());
                tempInlineKeyboardButton.setCallbackData(languages.get(languageIndex).getName());

                rowInline.add(tempInlineKeyboardButton);

                languageIndex++;
            }
            firstMarkupRowsInline.add(rowInline);
        }

        tempInlineKeyboardButton = new InlineKeyboardButton();
        tempInlineKeyboardButton.setText(">");
        tempInlineKeyboardButton.setCallbackData(">");

        rowInline.clear();
        rowInline.add(tempInlineKeyboardButton);

        firstInlineMarkup.setKeyboard(firstMarkupRowsInline);
        tempMessage.setReplyMarkup(firstInlineMarkup);

        for (int i = 0; i < buttonRows; i++)
        {

            rowInline = new ArrayList<>();

            for (int j = 0; j < buttonsPerRow; j++) {

                if (languageIndex < languages.size()) {

                    tempInlineKeyboardButton = new InlineKeyboardButton();
                    tempInlineKeyboardButton.setText(languages.get(languageIndex).getName());
                    tempInlineKeyboardButton.setCallbackData(languages.get(languageIndex).getName());

                    rowInline.add(tempInlineKeyboardButton);

                    languageIndex++;
                }
            }
            secondMarkupRowsInline.add(rowInline);
        }

        tempInlineKeyboardButton = new InlineKeyboardButton();
        tempInlineKeyboardButton.setText("<");
        tempInlineKeyboardButton.setCallbackData("<");

        rowInline.clear();
        rowInline.add(tempInlineKeyboardButton);

        secondInlineMarkup.setKeyboard(secondMarkupRowsInline);

        return tempMessage;
    }

    public String getBotUsername() { return botName; }

    public String getBotToken() { return botToken; }
}