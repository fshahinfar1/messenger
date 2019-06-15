package ir.fshahinfar1.iust.Model;


import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

// simple json library is a dependency now

public class Message {

    private JSONObject message;

    public Message(String content, Type type, String id, String userName, int date) {
        message = new JSONObject();
        message.put("type", type.name());
        message.put("content", content);
        message.put("id", id);
        message.put("author", userName);
        message.put("date", date);
    }

    public Message(String json) {
        // parse json to message class
        try {
            message = new JSONObject();
//            System.out.println(json);
            JSONObject jsonObject = (JSONObject) new org.json.simple.parser.JSONParser().parse(json);
            message.put("type", jsonObject.get("type"));
            message.put("content", jsonObject.get("content"));
            message.put("id", jsonObject.get("id"));
            message.put("author", jsonObject.get("author"));
            message.put("date", jsonObject.get("date"));
        } catch (ParseException e) {
            System.err.println("text cannot be converted to json");
            e.printStackTrace();
        }
    }

    public Type getMessageType() throws RuntimeException {
        // todo: what is the null story here
        Type t = Type.valueOf((String) message.get("type"));
        if (t == null)
            throw new RuntimeException("Type is invalid");
        return t;

    }

    public String getContent() {
        return (String) message.get("content");
    }

    public String getAuthorId() {
        return (String) message.get("id");
    }

    public String getMessageAuthor() {
        return (String) message.get("author");
    }

    public int getMessageDate() {
        return Integer.valueOf(((Long)message.get("date")).intValue());
    }

    @Override
    public String toString() {
        return message.toString();
    }


}
