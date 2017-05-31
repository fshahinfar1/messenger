package Model;


import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
// simple json library is a dependency now

public class Message{

    private JSONObject message;

    public Message(String content, Type type){
        message = new JSONObject();
        message.put("type", type.name());
        message.put("content",content);
    }

    public Message(String json){
        // parse json to message class
        try {
            message = new JSONObject();
//            System.out.println(json);
            JSONObject jsonObject = (JSONObject) new org.json.simple.parser.JSONParser().parse(json);
            message.put("type", jsonObject.get("type"));
            message.put("content", jsonObject.get("content"));
        }catch (ParseException e){
            System.err.println("text cannot be converted to json");
            e.printStackTrace();
        }
    }

    public Type getMessageType() throws RuntimeException{
        // todo: what is the null story here
        Type t = Type.valueOf((String) message.get("type"));
        if(t == null)
            throw new RuntimeException("Type is invalid");
        return t;

    }

    public String getContent(){
        return (String) message.get("content");
    }

    @Override
    public String toString(){
        return message.toString();
    }



}
