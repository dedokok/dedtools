package com.dedokok.systems.modules.Feature.CirclePlayer;

import com.dedokok.systems.modules.Feature.CirclePlayerCommand;

public class CirclePlayerClass {
    public String sendString;
    public boolean enabled;
    public CirclePlayerCommand.MessageTypes type;

    public CirclePlayerClass(String sendString, boolean enabled, CirclePlayerCommand.MessageTypes type) {
        this.sendString = sendString;
        this.enabled = enabled;
        this.type = type;
    }
    public CirclePlayerClass(){
        this.sendString = "";
        this.enabled = true;
        this.type = CirclePlayerCommand.MessageTypes.Command;
    }
}
