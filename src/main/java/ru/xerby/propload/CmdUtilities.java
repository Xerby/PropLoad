package ru.xerby.propload;

import java.util.Map;
import java.util.stream.Collectors;

public class CmdUtilities {
    private CmdUtilities() {
    }

    public static Map<String, String> parseCmdArgs(String[] args, boolean isEnabledWindowsKeyCompatibility, boolean throwExceptionIfUnboundTokenFound) {
        ParsedCmdProperties parsedCmdProperties = ParsedCmdProperties.parse(args, isEnabledWindowsKeyCompatibility, throwExceptionIfUnboundTokenFound);
        if (parsedCmdProperties.isEmpty())
            return Map.of();
        return parsedCmdProperties.getKeys().stream().map(key -> Map.entry(key, parsedCmdProperties.getValue(key)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, String> parseCmdArgs(String[] args) {
        return parseCmdArgs(args, true, true);
    }
}