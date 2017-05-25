package Model;

import java.io.Serializable;

public class Message <T extends Serializable> implements Serializable {

    private Type messageType;
    private T content;

    public Message(Type type , T content){
        this.content = content;
        messageType = type;
    }

    public Type getMessageType(){
        return messageType;
    }

    public T getContent(){
        return content;
    }

}
