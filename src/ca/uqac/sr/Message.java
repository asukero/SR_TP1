package ca.uqac.sr;

import java.io.Serializable;

/**
 * Created by Thoma on 22/05/2017.
 */
public class Message implements Serializable{
    public SendType sendType;

    public int number1;

    public int number2;

    public long fileSize = -1;

    public String fileName = "";

    public Message(SendType sendType, int number1, int number2, long fileSize, String fileName) {
        this.sendType = sendType;
        this.number1 = number1;
        this.number2 = number2;
        this.fileSize = fileSize;
        this.fileName = fileName;
    }

    public Message(SendType sendType, int number1, int number2, String fileName) {
        this.sendType = sendType;
        this.number1 = number1;
        this.number2 = number2;
        this.fileName = fileName;
    }
}
