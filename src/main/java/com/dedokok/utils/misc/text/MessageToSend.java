package com.dedokok.utils.misc.text;

public class MessageToSend {
    private Long timeGet;
    private String text;
    private int timeToWait;
    public MessageToSend(Long timeGet, String text, int timeToWait) {
        this.timeGet = timeGet;
        this.text = text;
        this.timeToWait = timeToWait;
    }
    public MessageToSend() {
    }
    public Long getTimeGet() {
        return timeGet;
    }
    public String getText() {return text;}
    public int getTimeToWait() {return timeToWait;}
}
