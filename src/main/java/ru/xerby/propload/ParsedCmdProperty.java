package ru.xerby.propload;

import lombok.Data;

@Data
class ParsedCmdProperty {
    private final String key;
    private final String value;
    private final boolean isSurelyParametrized;

    @Override
    public String toString() {
        if (value == null)
            return key;
        else
            return key + (isSurelyParametrized ? "=" : ":") + value;
    }
}
