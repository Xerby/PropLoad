package ru.xerby.propload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParsedCmdProperties {
    private final List<ParsedCmdProperty> properties = new ArrayList<>();

    public void add(String key, String value, boolean isSurelyParametrized) {
        properties.add(new ParsedCmdProperty(key, value, isSurelyParametrized));
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

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (ParsedCmdProperty parsedCmdProperty : properties) {
            sb.append("--").append(parsedCmdProperty.getKey());
            if (parsedCmdProperty.isSurelyParametrized()) {
                sb.append("=").append(parsedCmdProperty.getValue());
            }
            else if (parsedCmdProperty.getValue() != null) {
                sb.append(" ").append(parsedCmdProperty.getValue());
            }
            sb.append(" ");
        }
        return sb.toString();
    }

    public static ParsedCmdProperties parse(String[] args, boolean isWindows, boolean throwExceptionIfUnboundTokenFound) {
        String propname = null;
        String propval = null;
        boolean lastSymbolIsKey = false;
        ParsedCmdProperties res = new ParsedCmdProperties();
        for (String key : args) {
            if (key.strip().startsWith("/") && isWindows && key.strip().length() > 1) {
                if (propname != null) {
                    res.add(propname, propval, false);
                }
                propname = key.strip().substring(1);
                lastSymbolIsKey = true;
            } else if (key.strip().startsWith("--") && key.strip().length() > 2) {
                if (propname != null) {
                    res.add(propname, propval, false);
                }
                propname = key.strip().substring(2);
                lastSymbolIsKey = true;
            } else if (lastSymbolIsKey) {
                propval = key.strip();
                lastSymbolIsKey = false;
                res.add(propname, propval, false);
                propname = null;
                propval = null;
            } else {
                if (throwExceptionIfUnboundTokenFound)
                    throw new RuntimeException("Unbound token found: " + key+" in "+ Arrays.toString(args).replace(", ", " "));
                else
                    System.err.println("Unbound token found: " + key+"\n in "+Arrays.toString(args).replace(", ", " "));
            }

            if (propname != null && propname.contains("=")) {
                propval = propname.substring(propname.indexOf("=")+1).strip();
                propname = propname.substring(0, propname.indexOf("=")).strip();

                lastSymbolIsKey = false;
                res.add(propname, propval, true);
                propname = null;
                propval = null;
            }
        }
        if (propname != null) {
            res.add(propname, propval, false);
        }
        return res;
    }


}
