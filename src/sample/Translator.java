package sample;

import java.io.IOException;

import com.darkprograms.speech.translator.GoogleTranslate;

public class Translator {

    public static String translate(String stringToTranslate) throws IOException {

        String stringTranslated = GoogleTranslate.translate("en", stringToTranslate);
        String stringDetected = GoogleTranslate.detectLanguage(stringToTranslate);
        return "Translated \"" + stringToTranslate + "\" (" + stringDetected + "): " + stringTranslated;
    }
}