package ru.xerby.propload;

import lombok.Data;

@Data
public class ParsedCmdProperty {
    private final String key;
    private final String value;
    private final boolean isSurelyParametrized;
}
