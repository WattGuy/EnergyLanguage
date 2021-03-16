package me.wattguy.energy;

import lombok.var;
import me.wattguy.energy.infos.ConditionResponse;
import org.mvel2.MVEL;

import java.util.AbstractMap;
import java.util.Map;

public class Logic {

    public String initialize(String instruction){
        var sb = new StringBuilder(instruction.replace("\n", "").replace("\r", ""));

        while(true){
            var cr = firstCondition(sb.toString());

            if (cr != null && cr.getLogic() != null && cr.getBody() != null && cr.getInstruction() != null) {
                sb = new StringBuilder(cr.getInstruction());

                if (logic(cr.getLogic())){
                    Main.handle(cr.getBody(), Main.logic, Main.variables, Main.methods);
                }
            }else break;
        }

        return sb.toString();
    }

    public static ConditionResponse firstCondition(String s){
        if (s.length() < 2) return null;

        StringBuilder sb = new StringBuilder(s.trim());

        if (!sb.substring(0, 2).equalsIgnoreCase("if")) return null;

        var logicEntry = betweenBrackets(sb.toString(), '(', ')');
        var bodyEntry = betweenBrackets(sb.toString(), '{', '}');

        String logic = null;
        String body = null;

        if (logicEntry.getKey() != null && logicEntry.getValue() != null)
            logic = sb.substring(logicEntry.getKey(), logicEntry.getValue());

        if (bodyEntry.getKey() != null && bodyEntry.getValue() != null)
            body = sb.substring(bodyEntry.getKey(), bodyEntry.getValue());

        if (logic != null && body != null) {
            sb.delete(0, bodyEntry.getValue() + 1);

            return new ConditionResponse(logic, body, sb.toString());
        }

        return null;
    }

    private static Map.Entry<Integer, Integer> betweenBrackets(String s, char open, char close){
        Integer start = null;
        Integer end = null;

        int opened = 0;

        for (int cursor = 2; cursor < s.length(); cursor++){
            char c = s.charAt(cursor);

            if (c == open){

                if (start == null)
                    start = cursor + 1;
                else
                    opened++;

            }else if (c == close){

                if (opened > 0)
                    opened--;
                else
                    end = cursor;

            }
        }

        return new AbstractMap.SimpleEntry<>(start, end);
    }

    public boolean logic(String s){
        return (Boolean) MVEL.eval(s, Main.variables.map);
    }

}
