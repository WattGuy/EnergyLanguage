package me.wattguy.energy;

import lombok.var;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

public class Variables {

    private static Set<Class<?>> types = new HashSet<Class<?>>(){{

        add(int.class);
        add(float.class);
        add(double.class);
        add(String.class);

    }};

    public HashMap<String, Object> map = new HashMap<>();

    public boolean initialize(String instruction){
        Class<?> type = null;

        for(var t : types){

            if (instruction.toLowerCase().startsWith(t.getSimpleName().toLowerCase()))
            {

                type = t;
                break;

            }

        }

        if (type == null) return false;

        instruction = instruction.substring(type.getSimpleName().length()).trim();
        var splitted = instruction.split("=");

        String value = null;
        if (splitted.length > 1) {
            value = splitted[1].trim();

            var pattern = Pattern.compile("input\\(.*\\)");

            while(true){
                var m = pattern.matcher(value);
                if (!m.matches()) break;

                String print = null;

                try{
                    print = Objects.requireNonNull(Methods.getMethod(value.substring(m.start(), m.end()))).getValue().replace("\"", "");
                }catch(Exception ignored){ }

                String s = input(type, print);

                value = value.replaceFirst("input\\(.*\\)", s);
            }

            if (type == String.class) {
                var matcher = Pattern.compile("(.*)").matcher(value);

                if (matcher.find())
                    value = constructionToString(this, value.substring(matcher.start(), matcher.end()));
            }else if (type == float.class || type == double.class || type == int.class) {
                value = value.replace(",", ".");

                for(var entry : map.entrySet()){

                    if (value.contains(entry.getKey())) {

                        if (entry.getValue() instanceof Float || entry.getValue() instanceof Double || entry.getValue() instanceof Integer)
                            value = value.replace(entry.getKey(), entry.getValue().toString());
                        else
                            value = value.replace(entry.getKey(), "0");

                    }

                }

                value = eval(value) + "";
            }
        }

        Object o = instance(type, value);

        map.put(splitted[0].trim(), o);
        //System.out.println("DEBUG | " + map.toString());
        return true;
    }

    public static String input(Class<?> c, String print) {
        String s = "";

        try {
            System.out.print(print);
            String line = "";

            while(true) {

                try {
                    if (Main.scanner.hasNextFloat()) {
                        line = Main.scanner.nextFloat() + "";
                        break;
                    }
                }catch(Exception ignored) {}

            }

            if (c == String.class)
                s = line;
            else if (c == float.class)
                s = Float.parseFloat(line) + "";
            else if (c == double.class)
                s = Double.parseDouble(line) + "";
            else if (c == int.class)
                s = ((int) Double.parseDouble(line)) + "";
        }catch(Exception ignored) {
            ignored.printStackTrace();
        }

        return s;
    }

    public static Double constructionToDouble(Variables v, String s){
        Double result = null;

        for (String arg : s.split("\\+")) {
            arg = arg.trim();

            var add = 0d;

            if (arg.startsWith("\"") && arg.endsWith("\""))
                add = Double.parseDouble(arg.substring(1, arg.length() - 1));
            else if (v.map.containsKey(arg))
                add = Double.parseDouble(v.map.get(arg).toString());
            else
                add = 0d;

            if (result == null)
                result = 0d;

            result += add;
        }

        return result;
    }

    // Input: 'v1 + " " + v2'
    // Output: '1 2'
    public static String constructionToString(Variables v, String s){
        // TODO DELETE "+" FROM QUOTES

        var result = "";

        for (String arg : s.split("\\+")) {
            arg = arg.trim();

            String add = "";

            if (!result.isEmpty())
                add += " ";

            if (arg.startsWith("\"") && arg.endsWith("\"")){

                add = arg.substring(1, arg.length() - 1);

            }else if (arg.startsWith("\"") && arg.endsWith("\"")){

                add = arg.substring(1, arg.length() - 1);

            }else if (v.map.containsKey(arg)){

                add = v.map.get(arg).toString();

            }else
                add = "UNDEFINED";

            result += add;
        }

        return result;
    }

    private Object instance(Class<?> c, String equal){

        switch (c.getSimpleName().toLowerCase()){

            case "float":{
                if (equal != null)
                    return Float.parseFloat(equal);
                else
                    return 0f;
            }

            case "double":{
                if (equal != null)
                    return Double.parseDouble(equal);
                else
                    return 0d;
            }

            case "int":{
                if (equal != null)
                    return Double.valueOf(equal).intValue();
                else
                    return 0;
            }

            case "string":{
                if (equal != null)
                    return equal;
                else
                    return "";
            }

        }

        return null;
    }

    public static double eval(String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

}
