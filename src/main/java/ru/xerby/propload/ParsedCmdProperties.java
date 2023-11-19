package ru.xerby.propload;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Slf4j
@EqualsAndHashCode
class ParsedCmdProperties implements Iterable<ParsedCmdProperty> {
    private final List<ParsedCmdProperty> properties = new ArrayList<>();

    @SuppressWarnings("java:S3776")
    public static ParsedCmdProperties parse(String[] args, boolean isWindows, boolean throwExceptionIfUnboundTokenFound) {
        String propname = null;
        String propval = null;
        boolean lastSymbolIsKey = false;
        boolean oneHyphenMode = false;

        ParsedCmdProperties res = new ParsedCmdProperties();
        if (args == null) return res;
        for (String key : args) {
            String token = key.strip();
            if (token.startsWith("/") && isWindows && token.length() > 1) {
                if (propname != null) {
                    res.add(propname, propval, false, oneHyphenMode);
                }
                propname = token.substring(1);
                lastSymbolIsKey = true;
                oneHyphenMode = false;
            } else if (token.startsWith("--") && token.length() > 2) {
                if (propname != null) {
                    res.add(propname, propval, false, oneHyphenMode);
                }
                propname = token.substring(2);
                lastSymbolIsKey = true;
                oneHyphenMode = false;
            } else if (token.startsWith("-") && token.length() > 1) {
                if (propname != null) {
                    res.add(propname, propval, false, oneHyphenMode);
                }
                propname = token.substring(1);
                lastSymbolIsKey = true;
                oneHyphenMode = true;
            } else if (lastSymbolIsKey) {
                propval = token;
                lastSymbolIsKey = false;
                res.add(propname, propval, false, oneHyphenMode);
                propname = null;
                propval = null;
                oneHyphenMode = false;
            } else {
                if (throwExceptionIfUnboundTokenFound)
                    throw new RuntimeException("Dangling token found: " + key + " in " + Arrays.toString(args).replace(", ", " "));
                else
                    log.debug("Dangling token found: " + key + "\n in " + Arrays.toString(args).replace(", ", " "));
            }

            if (propname != null && propname.contains("=")) {
                propval = propname.substring(propname.indexOf("=") + 1).strip();
                propname = propname.substring(0, propname.indexOf("=")).strip();

                lastSymbolIsKey = false;
                res.add(propname, propval, true, oneHyphenMode);
                propname = null;
                propval = null;
                oneHyphenMode = false;
            }
        }
        if (propname != null) {
            res.add(propname, propval, false, oneHyphenMode);
        }
        return res;
    }

    public void add(String key) {
        add(key, null, false);
    }

    public void add(String key, String value, boolean isSurelyParametrized) {
        add(key, '\0', value, isSurelyParametrized);
    }

    public void add(String key, char ch, String value, boolean isSurelyParametrized) {
        if (key == null && ch == '\0')
            throw new RuntimeException("Either longKey or short key mustn't be be null");
        if (value == null && isSurelyParametrized)
            throw new RuntimeException("Value can't be null if isSurelyParametrized is true");
        properties.add(new ParsedCmdProperty(key, ch, value, isSurelyParametrized));
    }

    private void add(String str, String val, boolean isSurelyParametrized, boolean isOneHyphenMode) {
        if (isOneHyphenMode)
            add(null, str.charAt(0), val, isSurelyParametrized);
        else
            add(str, '\0', val, isSurelyParametrized);
    }

    public ParsedCmdProperty getParsedCmdProperty(String key) {
        for (ParsedCmdProperty parsedCmdProperty : properties) {
            if (parsedCmdProperty.getKey().equals(key)) {
                return parsedCmdProperty;
            }
        }
        return null;
    }

    public String getValue(String key) {
        for (ParsedCmdProperty parsedCmdProperty : properties) {
            if (parsedCmdProperty.getKey().equals(key)) {
                return parsedCmdProperty.getValue();
            }
        }
        return null;
    }

    public boolean isSurelyParametrized(String key) {
        for (ParsedCmdProperty parsedCmdProperty : properties) {
            if (parsedCmdProperty.getKey().equals(key)) {
                return parsedCmdProperty.isSurelyParametrized();
            }
        }
        return false;
    }

    public boolean containsKey(String key) {
        for (ParsedCmdProperty parsedCmdProperty : properties) {
            if (parsedCmdProperty.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public void clear() {
        properties.clear();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ParsedCmdProperty parsedCmdProperty : properties) {
            if (parsedCmdProperty.getLongKey() == null)
                sb.append("-").append(parsedCmdProperty.getShortKey());
            else
                sb.append("--").append(parsedCmdProperty.getLongKey());

            if (parsedCmdProperty.isSurelyParametrized()) {
                sb.append("=").append(parsedCmdProperty.getValue());
            } else if (parsedCmdProperty.getValue() != null) {
                sb.append(" ").append(parsedCmdProperty.getValue());
            }
            sb.append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    public Iterator<ParsedCmdProperty> iterator() {
        return properties.iterator();
    }
}
