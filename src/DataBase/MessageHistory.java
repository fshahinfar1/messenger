package DataBase;

import Model.Message;
import Model.Type;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Created by fsh on 7/7/17.
 */
public class MessageHistory extends DataBaseManager {

    public MessageHistory(String url, String tableName) throws SQLException{
        super(url, tableName);
    }

    public void InsertMessage(String message, String authorId, int date) throws SQLException{
        String sql = String.format("INSERT INTO %s(rowid, message, authorId, date) VALUES(NULL, \"%s\", \"%s\", %s)",
                tableName, message, authorId, date);
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }
    public void InsertMessage(Message message) throws SQLException{
        String m = message.getContent();
        String authorId = message.getAuthorId();
        int date = message.getMessageDate();
        InsertMessage(m, authorId, date);
    }

    public void clear() throws SQLException{
        String sql = String.format("DROP TABLE %s;",tableName);
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        sql = String.format("CREATE TABLE MessageHistory(rowid INTEGER NOT NULL PRIMARY KEY, message TEXT, authorId TEXT, date INTEGER);");
        stmt.executeUpdate(sql);
        stmt.close();
    }

    public ArrayList<Message> getMessagesBeforeDate(int date) throws SQLException{
        String sql = String.format("SELECT * FROM %s WHERE date <= %s", tableName, date);
        ResultSet rs = null;
        Statement stmt = null;
        stmt = connection.createStatement();
        rs = stmt.executeQuery(sql);

        ArrayList<Message> messages = new ArrayList<Message>();
        while(rs.next()){
            String content =  rs.getString("message");
            String id = rs.getString("authorId");
            int d = rs.getInt("date");
            Message message = new Message(content, Type.textMessage, id, "NotSet", d);
            messages.add(message);
//            System.out.println(m+id+"\n"+d);
        }
        return messages;
    }

    public static void main(String[] args) throws SQLException{
        // test
        MessageHistory mhdb = new MessageHistory("jdbc:sqlite:data/database/history.db", "MessageHistory");
        mhdb.getMessagesBeforeDate(1499579918);
//        mhdb.clear();
    }
}
