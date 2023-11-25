package ru.xerby.propload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class ParsedCmdProperty {
    private final String longKey;
    private final char shortKey;
    private final String value;
    private final boolean isSurelyParametrized;

    public ParsedCmdProperty(String longKey, String value, boolean isSurelyParametrized) {
        this(longKey, '\0', value, isSurelyParametrized);
    }

    public ParsedCmdProperty(char shortKey, String value, boolean isSurelyParametrized) {
        this(null, shortKey, value, isSurelyParametrized);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (longKey != null)
            sb.append("--").append(longKey);
        else
            sb.append("-").append(shortKey);

        if (isSurelyParametrized)
            sb.append("=").append(value);
        else if (value != null)
            sb.append(" ").append(value);

        return sb.toString();
    }

    public String getKey() {
        return longKey != null ? longKey : String.valueOf(shortKey);
    }
}
