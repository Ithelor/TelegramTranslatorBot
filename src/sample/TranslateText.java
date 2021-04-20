package sample;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

public class TranslateText {

    public static String translateText(String targetLanguage, String text) {

        Translate translate = TranslateOptions.getDefaultInstance().getService();

        Translation translation = translate.translate(
                text,
                Translate.TranslateOption.targetLanguage(targetLanguage)
        );

        System.out.println(
                "Translated:"
                + "\n\t(" + translate.detect(text).getLanguage() + ") " + text
                + "\n\t(" + targetLanguage + ") " + translation.getTranslatedText()
                + "\n"
        );

        return translation.getTranslatedText();
    }
}