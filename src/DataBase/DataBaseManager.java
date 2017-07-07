package DataBase;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.HashMap;

/**
 * Created by fsh on 5/31/17.
 */
public class DataBaseManager {
    protected Connection connection;
    protected String tableName;
    protected String url;

    public DataBaseManager(String url, String tableName) throws SQLException{
        this.tableName = tableName;
        this.url = url;
        connect();
    }

    public void connect() throws SQLException{
        try {
            // create a connection
            connection = DriverManager.getConnection(url);

            System.out.println("Connected to the database");
        }finally {
            try {
                if (connection == null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public ResultSet selectAll() throws SQLException {
        String sql = String.format("SELECT * %s;", tableName);
        ResultSet rs = null;
        Statement stmt = null;
        stmt = connection.createStatement();
        rs = stmt.executeQuery(sql);
        return rs;
    }

    public int getLastID(String columnName){
        ResultSet rs = null;
        int id=-1;
        try {
            String sql = String.format("SELECT %s FROM %s ORDER BY id DESC LIMIT 1", columnName,tableName);
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            id=rs.getInt(columnName);
        }catch (SQLException e){
            return -1;
        }
        return id;
    }

    public static void main(String[] args) throws SQLException {
        DataBaseManager db = new DataBaseManager("jdbc:sqlite:data/database/users.db", "users_data");
//        db.selectAll();
//        try {
////            HashMap<String, String> hm = db.getUserData("Farbod0");
////            System.out.println("id: "+hm.get("id")+"  pass: "+hm.get("password"));
////            db.insertUserData("SomeONeElse","1111", "13");
//        }catch (SQLException e){
//            System.out.println("No user");
//        }
        System.out.println(db.getLastID("id"));
    }
}
