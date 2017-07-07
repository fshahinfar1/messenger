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

    public void InsertMessage(String message, String date) throws SQLException{
        String sql = String.format("INSERT INTO %s(rowid, message, date) VALUES(NULL, \"%s\", \"%s\")",
                tableName, message, date);
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }
    public void InsertMessage(Message message) throws SQLException{
        String m = message.getContent();
        String date = message.getMessageDate();
        InsertMessage(m, date);
    }

    public void clear() throws SQLException{
        String sql = String.format("DROP TABLE %s;",tableName);
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        sql = String.format("CREATE TABLE MessageHistory ( rowid INTEGER NOT NULL PRIMARY KEY, message TEXT, date TEXT NOT NULL);");
        stmt.executeUpdate(sql);
        stmt.close();
    }

    public static void main(String[] args) throws SQLException{
        // test
        MessageHistory mhdb = new MessageHistory("jdbc:sqlite:data/database/history.db", "MessageHistory");
        mhdb.clear();
    }
}
