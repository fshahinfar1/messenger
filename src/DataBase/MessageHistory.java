package DataBase;

import Model.Message;

import java.sql.SQLException;
import java.sql.Statement;

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

    public void getMessagesBeforeDate(String date){

    }

    public static void main(String[] args) throws SQLException{
        // test
        MessageHistory mhdb = new MessageHistory("jdbc:sqlite:data/database/history.db", "MessageHistory");
        mhdb.clear();
    }
}
