package DataBase;

import Model.Message;
import Model.Type;
import org.json.simple.JSONArray;

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
        String sql = String.format("INSERT INTO %s(rowid, AuthorId, Message, Date) VALUES(NULL, \"%s\", \"%s\", %s)",
                tableName, authorId, message, date);
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
        sql = String.format("CREATE TABLE MessageHistory(rowid INTEGER NOT NULL PRIMARY KEY, AuthorId TEXT NOT NULL, Message TEXT, Date INTEGER NOT NULL)");
        stmt.executeUpdate(sql);
        stmt.close();
    }

    public JSONArray getMessagesBeforeDate(int date) throws SQLException{
        String sql = String.format("SELECT * FROM %s WHERE Date <= %s", tableName, date);
        Statement stmt = null;
        ResultSet rs = null;
        stmt = connection.createStatement();
        rs = stmt.executeQuery(sql);
        JSONArray messages = new JSONArray();
        while(rs.next()){
            String content =  rs.getString("Message");
            String id = rs.getString("AuthorId");
            int d = rs.getInt("Date");
            // todo : how should I join to talbe ??
            // todo : check for sql injection
            // start of user name
//            System.out.println(id);
            sql = String.format("SELECT Username FROM Users WHERE Id = \"%s\" LIMIT 1", id);
            Statement stmt2 = connection.createStatement();
            ResultSet usernameResultSet = stmt2.executeQuery(sql);
//            System.out.println(usernameResultSet.next());
            String username = usernameResultSet.getString("Username");
            // end of user name
            Message message = new Message(content, Type.textMessage, id, username, d);
            messages.add(message.toString());
//            System.out.println(m+id+"\n"+d);
        }
        rs.close();
        stmt.close();
        return messages;
    }

    public static void main(String[] args) throws SQLException{
        // test
        MessageHistory mhdb = new MessageHistory("jdbc:sqlite:data/database/MessengerDatabase.db", "MessageHistory");
        mhdb.getMessagesBeforeDate(1499662209);
//        mhdb.clear();
    }
}
