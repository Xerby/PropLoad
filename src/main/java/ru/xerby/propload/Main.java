package ru.xerby.propload;

public class Main {
    public static void main(String[] args) {
        System.out.println(ParsedCmdProperties.parse (new String[]{"/delayed", "--no-ops", "pzdariki", "gigi", "/commando", "opa", "--size=plpl"}, true, false));
    }
}