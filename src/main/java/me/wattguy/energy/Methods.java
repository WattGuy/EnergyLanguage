package me.wattguy.energy;

import lombok.var;

import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Methods {

    public Variables variables = null;

    public Methods(Variables variables){
        this.variables = variables;
    }

    public boolean system(String label, String args){

        switch(label.toLowerCase()){

            case "print":
            case "println": {

                try {
                    System.out.getClass().getMethod(label.toLowerCase(), String.class)
                            .invoke(System.out, Variables.constructionToString(variables, args));
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }

                return true;
            }

        }

        return false;
    }

    public boolean initialize(String instruction){
        var matcher = Pattern.compile("\\(.*\\)").matcher(instruction);

        String args = null;

        while(matcher.find()){
            var found = instruction.substring(matcher.start(), matcher.end());

            if (instruction.endsWith(found)) {

                args = found;
                break;

            }
        }

        if (args == null) return false;

        instruction = instruction.substring(0, instruction.length() - args.length());
        args = args.replace("(", "").replace(")", "");

        //System.out.println("DEBUG | " + instruction + " " + args);

        if (system(instruction, args))
            return true;

        return false;
    }

    public static Map.Entry<String, String> getMethod(String instruction){
        var matcher = Pattern.compile("\\(.*\\)").matcher(instruction);

        String args = null;

        while(matcher.find()){
            var found = instruction.substring(matcher.start(), matcher.end());

            if (instruction.endsWith(found)) {

                args = found;
                break;

            }
        }

        if (args == null) return null;

        instruction = instruction.substring(0, instruction.length() - args.length());
        args = args.replaceFirst("\\(", "");
        args = args.substring(0, args.length() - 1);

        return new AbstractMap.SimpleEntry<>(instruction, args);
    }

}
