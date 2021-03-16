package me.wattguy.energy;

import lombok.var;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {

    public static Scanner scanner;

    public static Logic logic;
    public static Variables variables;
    public static Methods methods;

    public static void main(String[] args) throws FileNotFoundException {
        scanner = new Scanner(System.in);
        var code = readStream(new FileInputStream(args[0]));

        logic = new Logic();
        variables = new Variables();
        methods = new Methods(variables);

        handle(code, logic, variables, methods);
    }

    public static void handle(String s, Logic logic, Variables variables, Methods methods){
        var instructions = splitByInstructions(s);

        for (String instruction : instructions) {
            instruction = instruction.trim();

            instruction = logic.initialize(instruction);

            if (variables.initialize(instruction)) continue;

            methods.initialize(instruction);
        }
    }

    public static String readStream(InputStream is) {
        StringBuilder sb = new StringBuilder();

        try {
            Reader r = new InputStreamReader(is, StandardCharsets.UTF_8);
            int c;

            while ((c = r.read()) != -1) {
                sb.append((char) c);
            }

            r.close();
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return sb.toString();
    }

    public static List<String> splitByInstructions(String s){
        s = s.trim().replace("\r", "").replace("\n", "");

        List<Integer> blacklist = new ArrayList<>();

        int openedString = 0;
        int openedBrackets = 0;

        for (int cursor = 0; cursor < s.length(); cursor++){
            char c = s.charAt(cursor);

            if (c == ';' && (openedString > 0 || openedBrackets > 0)){
                blacklist.add(cursor);

                /*System.out.println("-----");
                System.out.println(openedString);
                System.out.println(openedBrackets);*/
            } else if (c == '"'){

                if (openedString > 0)
                    openedString = 0;
                else
                    openedString = 1;

            }else if (c == '{' || c == '('){

                openedBrackets++;

            }else if (c == '}' || c == ')'){

                openedBrackets--;

            }
        }

        StringBuilder sb = new StringBuilder(s);

        List<String> splitted = new ArrayList<>();

        int cursor = 0;
        var p = Pattern.compile(";");
        var m = p.matcher(sb.toString());

        //System.out.println(blacklist.toString());

        while(m.find()){
            if (blacklist.contains(m.start())) continue;

            splitted.add(sb.substring(cursor, m.end() - 1));
            cursor = m.end();
        }

        if (cursor < sb.length() - 1){
            var instruction = sb.substring(cursor, sb.length()).trim();

            if (!instruction.isEmpty())
                splitted.add(instruction);
        }

        //System.out.println(splitted.toString());

        return splitted;
    }

}
