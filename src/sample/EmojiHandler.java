package sample;

import com.vdurmont.emoji.EmojiParser;

public class EmojiHandler {

    public final static String[] digitsToEmojis = {

            ":zero:",
            ":one:",
            ":two:",
            ":three:",
            ":four:",
            ":five:",
            ":six:",
            ":seven:",
            ":eight:",
            ":nine:"
    };

    public enum Icons {

        ARROW_LEFT(":arrow_left:"),
        ARROW_RIGHT(":arrow_right:");

        private final String value;

        public String get() {
            return EmojiParser.parseToUnicode(value);
        }

        Icons (String value) { this.value = value; }
    }
}
