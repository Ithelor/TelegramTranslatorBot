package sample;

import com.vdurmont.emoji.EmojiParser;

public class EmojiHandler {

    public enum Icons {

        ARROW_LEFT(":arrow_left:"),
        ARROW_RIGHT(":arrow_right:"),
        ZERO(":zero:"),
        ONE(":one:"),
        TWO(":two:"),
        THREE(":three:"),
        FOUR(":four:"),
        FIVE(":five:"),
        SIX(":six:"),
        SEVEN(":seven:"),
        EIGHT(":eight:"),
        NINE(":nine:");

        private final String value;

        public String get() {
            return EmojiParser.parseToUnicode(value);
        }

        Icons (String value) { this.value = value; }
    }
}
