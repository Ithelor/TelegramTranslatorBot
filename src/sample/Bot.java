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
import java.math.BigInteger;
import java.util.*;
import java.io.IOException;

public class Bot extends TelegramLongPollingBot {

    static String botName;
    static String botToken;

    static String targetLanguageCode = "en";
    static String targetLanguageName = "English";

    static Translate translate = TranslateOptions.getDefaultInstance().getService();
    static List<Language> languages = translate.listSupportedLanguages();

    static InlineKeyboardMarkup firstSTLMarkup = new InlineKeyboardMarkup();
    static InlineKeyboardMarkup secondSTLMarkup = new InlineKeyboardMarkup();

    static List<List<InlineKeyboardButton>> firstSTLMarkupRowsInline = new ArrayList<>();
    static List<List<InlineKeyboardButton>> secondSTLMarkupRowsInline = new ArrayList<>();

    static InlineKeyboardMarkup spcMarkup = new InlineKeyboardMarkup();
    static List<List<InlineKeyboardButton>> spcMarkupRowsInline = new ArrayList<>();

    static String MODE = "MANUAL"; // either MANUAL for translation-by-command or SPECIFIED for automatic translation of specific language
    static String SPECIFIED_LANGUAGE_CODE;
    static Boolean AWAITING_SPECIFICATION = false;

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

    private void sendMsg(Message message, String text) {

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

        System.out.println("Received an update");

        Message message = update.getMessage();

        if (update.hasInlineQuery()) {

            handleInlineQuery(update.getInlineQuery());
        }
        else if (message != null && message.hasText()) {

            String text = message.getText();
            SendMessage inlineMessage;

            switch (message.getText()) {

                case "/stl":
                case "/stl@NumeriusCloudBot":

                    inlineMessage = prepareSTLKeyboard(message);

                    try {
                        execute(inlineMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    break;

                case "/spc":
                case "/spc@NumeriusCloudBot":

                    // TODO: redo
                    inlineMessage = prepareSTLKeyboard(message);
                    inlineMessage = prepareSPCKeyboard(message);

                    try {
                        execute(inlineMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                    break;

                default:

//                    if (message.getText().startsWith("/specify ") || message.getText().startsWith("/specify@NumeriusCloudBot "))
//                    {
//                        MODE = "SPECIFIED";
//                        SPECIFIED_LANGUAGE_CODE = getCodeByName(text.substring(text.indexOf(' ') + 1));
//
//                        DatabaseHandler.handleSpecify(SPECIFIED_LANGUAGE_CODE, message.getChatId());
//
//                        sendMsg(
//                                message,
//                                "Specified for " + text.substring(text.indexOf(' ') + 1)
//                        );
//                        System.out.println(MODE + " " + SPECIFIED_LANGUAGE_CODE);
//                    }
//                    else

                    System.out.println("Checking 160-if");
                        if (DatabaseHandler.executeQuery("SELECT translation_mode FROM chats WHERE chat_id = " + message.getChatId() + ";", "1", true).equals("SPECIFIED") &&
                        translate.detect(message.getText()).getLanguage().equals(
                                DatabaseHandler.executeQuery(
                                        "SELECT latest_specified_language_code FROM chats WHERE chat_id = " + message.getChatId() + ";",
                                        "1",
                                        true
                                )
                        )) {
                            System.out.println("Checked 160-if");
                            sendMsg(
                                    message,
                                    // TODO: switch languages + notification
                                    TranslateText.translateText(
                                            DatabaseHandler.getCurrentTargetLanguage(message.getChatId()),
                                            text
                                    )
                            );
                        }
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

            System.out.println("Callbacked");

            switch (call_data)
            {

                case (">STL2"):
                    editMessageText.setReplyMarkup(secondSTLMarkup);
                    editMessageText.setText("Please, select language - page 2/2");
                    break;

                case ("<STL1"):
                    editMessageText.setReplyMarkup(firstSTLMarkup);
                    editMessageText.setText("Please, select language - page 1/2");
                    break;

                case ("MANUAL"):

                    MODE = "MANUAL";

                    DatabaseHandler.handleSpecify(MODE, SPECIFIED_LANGUAGE_CODE, update.getCallbackQuery().getMessage().getChatId());
                    editMessageText.setText("Set " + call_data + " mode (@" + update.getCallbackQuery().getFrom().getUserName() + ")");
                    break;

                case ("SPECIFIED"):

                    AWAITING_SPECIFICATION = true;

                    editMessageText.setReplyMarkup(firstSTLMarkup);
                    editMessageText.setText("Please, select a language for specified mode");
                    break;

                default:
                    if (AWAITING_SPECIFICATION) {

                        MODE = "SPECIFIED";
                        SPECIFIED_LANGUAGE_CODE = getCodeByName(call_data);

                        DatabaseHandler.handleSpecify(MODE, SPECIFIED_LANGUAGE_CODE, update.getCallbackQuery().getMessage().getChatId());
                        editMessageText.setText("Specified " + call_data + " (@" + update.getCallbackQuery().getFrom().getUserName() + ")");
                        AWAITING_SPECIFICATION = false;
                    }
                    else {

                        targetLanguageCode = getCodeByName(call_data);
                        targetLanguageName = call_data;
                        DatabaseHandler.handleSTL(call_data, update.getCallbackQuery().getMessage().getChatId());
                        editMessageText.setText("Selected " + call_data + " ( @" + update.getCallbackQuery().getFrom().getUserName() + ")");
                    }
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

        for (Language language : languages) {

            if (language.getName().equals(name))
                return language.getCode();
        }

        return null;
    }

    public static String getNameByCode(String code) {

        for (Language language : languages) {

            if (language.getCode().equals(code))
                return language.getName();
        }

        return null;
    }

    private SendMessage prepareSTLKeyboard(Message message) {

        // TODO: gayISH - find an adequate solution
        if (firstSTLMarkup.getKeyboard() != null && secondSTLMarkup.getKeyboard() != null) {

            firstSTLMarkup.getKeyboard().clear();
            secondSTLMarkup.getKeyboard().clear();
        }

        SendMessage tempMessage = new SendMessage();
        InlineKeyboardButton tempInlineKeyboardButton;
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        int buttonsPerRow = 4;
        double buttonRows = Math.ceil(Double.parseDouble(String.valueOf(languages.size())) / buttonsPerRow / 2
                + 1); // 1 == number of auxiliary (navigation) rows
        int languageIndex = 0;

        tempMessage.setChatId(String.valueOf(message.getChatId()));
        tempMessage.setText("Please, select target language - page 1/2");
        tempMessage.setChatId(String.valueOf(message.getChatId()));
        tempMessage.setReplyToMessageId(message.getMessageId());

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
            firstSTLMarkupRowsInline.add(rowInline);
        }

        tempInlineKeyboardButton = new InlineKeyboardButton();
        tempInlineKeyboardButton.setText(">");
        tempInlineKeyboardButton.setCallbackData(">STL2");

        rowInline.clear();
        rowInline.add(tempInlineKeyboardButton);

        firstSTLMarkup.setKeyboard(firstSTLMarkupRowsInline);
        tempMessage.setReplyMarkup(firstSTLMarkup);

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
            secondSTLMarkupRowsInline.add(rowInline);
        }

        tempInlineKeyboardButton = new InlineKeyboardButton();
        tempInlineKeyboardButton.setText("<");
        tempInlineKeyboardButton.setCallbackData("<STL1");

        rowInline.clear();
        rowInline.add(tempInlineKeyboardButton);

        secondSTLMarkup.setKeyboard(secondSTLMarkupRowsInline);

        return tempMessage;
    }

    private SendMessage prepareSPCKeyboard(Message message) {

        // TODO: gayISH - find an adequate solution
        if (spcMarkup.getKeyboard() != null) {

            spcMarkup.getKeyboard().clear();
        }

        SendMessage tempMessage = new SendMessage();
        InlineKeyboardButton tempInlineKeyboardButton;
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        tempMessage.setChatId(String.valueOf(message.getChatId()));
        tempMessage.setText("Please, select translation mode");
        tempMessage.setChatId(String.valueOf(message.getChatId()));
        tempMessage.setReplyToMessageId(message.getMessageId());

        tempInlineKeyboardButton = new InlineKeyboardButton();
        tempInlineKeyboardButton.setText("MANUAL");
        tempInlineKeyboardButton.setCallbackData("MANUAL");

        rowInline.add(tempInlineKeyboardButton);

        tempInlineKeyboardButton = new InlineKeyboardButton();
        tempInlineKeyboardButton.setText("SPECIFIED");
        tempInlineKeyboardButton.setCallbackData("SPECIFIED");

        rowInline.add(tempInlineKeyboardButton);

        spcMarkupRowsInline.add(rowInline);

        spcMarkup.setKeyboard(spcMarkupRowsInline);
        tempMessage.setReplyMarkup(spcMarkup);

        return tempMessage;
    }

    public String getBotUsername() { return botName; }

    public String getBotToken() { return botToken; }
}