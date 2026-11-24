package com.dedokok.utils.misc.text;

import com.dedokok.utils.classes.SendMessagesTypes;

public class MessageToSend {
    private Long timeGet;
    private String text;
    private int timeToWait;
    private SendMessagesTypes type;
    public MessageToSend(Long timeGet, String text, int timeToWait,  SendMessagesTypes type) {
        this.timeGet = timeGet;
        this.text = text;
        this.timeToWait = timeToWait;
        this.type = type;
    }
    public MessageToSend() {
    }
    public Long getTimeGet() {
        return timeGet;
    }
    public String getText() {return text;}
    public int getTimeToWait() {return timeToWait;}
    public SendMessagesTypes getType() {return type;}
}
