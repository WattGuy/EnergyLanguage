package me.wattguy.energy.infos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ConditionResponse {

    @Getter
    private String logic;

    @Getter
    private String body;

    @Getter
    private String instruction;

}
