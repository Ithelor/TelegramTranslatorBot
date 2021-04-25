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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class Bot extends TelegramLongPollingBot {

    static String botName;
    static String botToken;

    static String targetLanguageName;
    static String targetLanguageCode;

    static Translate translate = TranslateOptions.getDefaultInstance().getService();
    static List<Language> languages = translate.listSupportedLanguages();

    static List<InlineKeyboardMarkup> stlMarkup = new ArrayList<>();

    static List<List<InlineKeyboardButton>> stlMarkupRowsInline = new ArrayList<>();

    static InlineKeyboardMarkup spcMarkup = new InlineKeyboardMarkup();
    static List<List<InlineKeyboardButton>> spcMarkupRowsInline = new ArrayList<>();

    static String MODE; // either MANUAL for translation-by-command
                        // or SPECIFIED for automatic translation of specific language
    static String SPECIFIED_LANGUAGE_CODE;
    static Boolean AWAITING_SPECIFICATION = false;

    static int buttonsPerRow = 4;
    static int buttonRows = 7;
    static int STL_PAGES_NUM = 0;

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

                case "/status":
                case "/status@NumeriusCloudBot":

                    if (
                        DatabaseHandler.getValueByColumn
                        (
                            "SELECT translation_mode FROM chats WHERE chat_id = " + message.getChatId() + ";",
                            "1",
                            message.getChatId()
                        ).equals("MANUAL")
                    ) {
                        sendMsg(
                            message,
                            "Status: " +
                                DatabaseHandler.getValueByColumn(
                                    "SELECT translation_mode FROM chats WHERE chat_id = " + message.getChatId() + ";",
                                    "1",
                                    message.getChatId()
                                ).toLowerCase() +
                                " (" +
                                Objects.requireNonNull(getNameByCode(
                                    DatabaseHandler.getValueByColumn(
                                        "SELECT latest_specified_language_code FROM chats WHERE chat_id = " + message.getChatId() + ";",
                                        "1",
                                        message.getChatId()
                                    )
                                )).toLowerCase() +
                                " to " +
                                Objects.requireNonNull(getNameByCode(
                                    DatabaseHandler.getValueByColumn(
                                        "SELECT last_target_language_code FROM chats WHERE chat_id = " + message.getChatId() + ";",
                                        "1",
                                        message.getChatId()
                                    )
                                )).toLowerCase() +
                                " - not used)"
                        );
                    } else {
                        sendMsg(
                            message,
                            "Status: " +
                                DatabaseHandler.getValueByColumn(
                                    "SELECT translation_mode FROM chats WHERE chat_id = " + message.getChatId() + ";",
                                    "1",
                                    message.getChatId()
                                ).toLowerCase() +
                                " (" +
                                Objects.requireNonNull(getNameByCode(
                                    DatabaseHandler.getValueByColumn(
                                        "SELECT latest_specified_language_code FROM chats WHERE chat_id = " + message.getChatId() + ";",
                                        "1",
                                        message.getChatId()
                                    )
                                )).toLowerCase() +
                                " to " +
                                Objects.requireNonNull(getNameByCode(
                                    DatabaseHandler.getValueByColumn(
                                        "SELECT last_target_language_code FROM chats WHERE chat_id = " + message.getChatId() + ";",
                                        "1",
                                        message.getChatId()
                                    )
                                )).toLowerCase() +
                                ")"
                        );
                    }

                    break;

                case "/translate":
                case "/translate@NumeriusCloudBot":

                    if (!message.isReply()) {

                        sendMsg(
                            message,
                            "Please reply to the message you want to translate and specify target language"
                        );
                    }
                    else {

                        sendMsg(
                            message,
                            "Please specify target language"
                        );
                    }
                    break;

                default:

                    if (
                        message.getText().startsWith("/translate ")
                            ||
                        message.getText().startsWith("/translate@NumeriusCloudBot ")
                    )
                    {
                        if (message.isReply())
                        {
                            if (!message.getText().startsWith("@", 10)) {

                                sendMsg(
                                    message.getReplyToMessage(),
                                    TranslateText.translateText(
                                        getCodeByName(
                                            message.getText().substring(11, 12).toUpperCase() +
                                            message.getText().substring(12)
                                        ),
                                        message.getReplyToMessage().getText()
                                    )
                                );
                            }
                            else {

                                sendMsg(
                                    message.getReplyToMessage(),
                                    TranslateText.translateText(
                                        getCodeByName(
                                            message.getText().substring(28, 29).toUpperCase() +
                                            message.getText().substring(29)
                                        ),
                                        message.getReplyToMessage().getText()
                                    )
                                );
                            }
                        }
                        else {
                            sendMsg(
                                    message,
                                    "Please reply to the message you want to translate"
                            );
                        }
                    }
                    else if (
                        DatabaseHandler.getValueByColumn
                        (
                            "SELECT translation_mode FROM chats WHERE chat_id = " + message.getChatId() + ";",
                            "1",
                            message.getChatId()
                        )
                        .equals("SPECIFIED")
                    ) {

                        SPECIFIED_LANGUAGE_CODE =
                            DatabaseHandler.getValueByColumn
                            (
                                "SELECT latest_specified_language_code FROM chats WHERE chat_id = " + message.getChatId() + ";",
                                "1",
                                message.getChatId()
                            );

                        if (SPECIFIED_LANGUAGE_CODE.equals(translate.detect(message.getText()).getLanguage())) {

                            sendMsg(
                                message,
                                // TODO: switch languages + switch notification
                                TranslateText.translateText(
                                    DatabaseHandler.getCurrentTargetLanguage(message.getChatId()),
                                    text
                                )
                            );
                        }
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

            switch (call_data)
            {

                case ("MANUAL"):

                    MODE = "MANUAL";
                    DatabaseHandler.handleSpecify(MODE, SPECIFIED_LANGUAGE_CODE, update.getCallbackQuery().getMessage().getChatId());
                    editMessageText.setText("Set " + call_data + " mode (@" + update.getCallbackQuery().getFrom().getUserName() + ")");

                    break;

                case ("SPECIFIED"):

                    AWAITING_SPECIFICATION = true;
                    editMessageText.setReplyMarkup(stlMarkup.get(0));
                    editMessageText.setText("Please, select a language for specified mode");

                    break;

                default:

                    if (AWAITING_SPECIFICATION) {

                        // TODO: change stl navigation button label on click
                        if (call_data.startsWith("stl")) {

                            int pageNum = Integer.parseInt(call_data.substring(3)) - 1;
                            editMessageText.setReplyMarkup(stlMarkup.get(pageNum));
                            editMessageText.setText("Please, select language - page " + (pageNum + 1) + "/" + STL_PAGES_NUM);
                        }
                        else {

                            MODE = "SPECIFIED";
                            SPECIFIED_LANGUAGE_CODE = getCodeByName(call_data);

                            targetLanguageCode = getCodeByName(call_data);
                            targetLanguageName = call_data;
                            DatabaseHandler.handleSpecify(MODE, SPECIFIED_LANGUAGE_CODE, update.getCallbackQuery().getMessage().getChatId());
                            editMessageText.setText("Selected " + call_data + " (@" + update.getCallbackQuery().getFrom().getUserName() + ")");

                            AWAITING_SPECIFICATION = false;
                        }
                    }
                    else {

                        if (call_data.startsWith("stl")) {

                            int pageNum = Integer.parseInt(call_data.substring(3)) - 1;
                            editMessageText.setReplyMarkup(stlMarkup.get(pageNum));
                            editMessageText.setText("Please, select language - page " + (pageNum + 1) + "/" + STL_PAGES_NUM);
                        }
                        else {
                            targetLanguageCode = getCodeByName(call_data);
                            targetLanguageName = call_data;
                            DatabaseHandler.handleSTL(call_data, update.getCallbackQuery().getMessage().getChatId());
                            editMessageText.setText("Selected " + call_data + " (@" + update.getCallbackQuery().getFrom().getUserName() + ")");
                        }
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
            "Translation (" +
            getNameByCode(targetLanguageCode) + "): " +
            TranslateText.translateText(targetLanguageCode, query) +
            "\nOriginal (" +
            getNameByCode(translate.detect(query).getLanguage()) +
            "): " + query
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

        STL_PAGES_NUM = (int) Math.ceil(Double.parseDouble(String.valueOf(languages.size())) / (buttonsPerRow * buttonRows));

        SendMessage tempMessage = new SendMessage();
        tempMessage.setChatId(String.valueOf(message.getChatId()));
        tempMessage.setChatId(String.valueOf(message.getChatId()));
        tempMessage.setText("Please, select language - page " + 1 + "/" + STL_PAGES_NUM);
        tempMessage.setReplyToMessageId(message.getMessageId());

        InlineKeyboardButton tempInlineKeyboardButton;
        List<InlineKeyboardButton> rowInline;

        int languageIndex = 0;

        for (int k = 0; k < STL_PAGES_NUM; k++) {

            stlMarkupRowsInline = new ArrayList<>();

            int currentPage = 0;

            for (int m = 0; m < Math.ceil(STL_PAGES_NUM / 8.0f); m++) {

                rowInline = new ArrayList<>();

                for (int l = 0; l < 8 * (m + 1); l++) {

                    if (currentPage < STL_PAGES_NUM) {

                        currentPage++;

                        tempInlineKeyboardButton = new InlineKeyboardButton();
                        tempInlineKeyboardButton.setText(String.valueOf(currentPage));
                        tempInlineKeyboardButton.setCallbackData("stl" + currentPage);

                        rowInline.add(tempInlineKeyboardButton);
                    }
                }

                stlMarkupRowsInline.add(rowInline);
            }

            for (int i = 0; i < buttonRows + Math.ceil(STL_PAGES_NUM / 8.0f) - 1; i++) {

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

                stlMarkupRowsInline.add(rowInline);
            }

            rowInline = new ArrayList<>();
            tempInlineKeyboardButton = new InlineKeyboardButton();

            if (k == 0) {

                tempInlineKeyboardButton.setText(EmojiHandler.Icons.ARROW_RIGHT.get());
                tempInlineKeyboardButton.setCallbackData("stl" + (k + 1 + 1));
            }
            else if (k == STL_PAGES_NUM - 1)
            {

                tempInlineKeyboardButton.setText(EmojiHandler.Icons.ARROW_LEFT.get());
                tempInlineKeyboardButton.setCallbackData("stl" + (k + 1 - 1));
            }
            else
            {

                tempInlineKeyboardButton.setText(EmojiHandler.Icons.ARROW_LEFT.get());
                tempInlineKeyboardButton.setCallbackData("stl" + (k + 1 - 1));
                rowInline.add(tempInlineKeyboardButton);

                tempInlineKeyboardButton = new InlineKeyboardButton();
                tempInlineKeyboardButton.setText(EmojiHandler.Icons.ARROW_RIGHT.get());
                tempInlineKeyboardButton.setCallbackData("stl" + (k + 1 + 1));
            }

            rowInline.add(tempInlineKeyboardButton);
            stlMarkupRowsInline.add(rowInline);

            stlMarkup.add(new InlineKeyboardMarkup(stlMarkupRowsInline));
        }

        tempMessage.setReplyMarkup(stlMarkup.get(0));

        for (InlineKeyboardMarkup inlineKeyboardMarkup : stlMarkup) {

            System.out.println(inlineKeyboardMarkup);
        }

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