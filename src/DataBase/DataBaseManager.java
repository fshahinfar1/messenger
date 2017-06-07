package DataBase;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.HashMap;

/**
 * Created by fsh on 5/31/17.
 */
public class DataBaseManager {
    private Connection connection;
    private String tableName;
    private String url;

    public DataBaseManager() throws SQLException{
        tableName = "users_data";
        url = "jdbc:sqlite:data/database/users.db";
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

    public void selectAll() {
        String sql = "SELECT * FROM users_data;";
        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                System.out.println(rs.getString("userName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public HashMap getUserData(String userName) throws SQLException {
        // todo: working here ...
        String sql = "SELECT * FROM " + tableName + " WHERE username=" + "\"" + userName + "\"" + "LIMIT 1";
        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            HashMap<String, String> result = new HashMap<String, String>();
            result.put("username", rs.getString("username"));
            result.put("id", rs.getString("id"));
            result.put("password", rs.getString("password"));
            return result;
        }finally {
            try {
                rs.close();
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    public void insertUserData(String userName, String password, String id) throws SQLException{
        String sql = String.format("INSERT INTO %s (id, username, password) VALUES(%s,'%s','%s')",
                tableName, id,userName,password);
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }

    public int getLastID(){
        ResultSet rs = null;
        int id=-1;
        try {
            String sql = String.format("SELECT id FROM %s ORDER BY id DESC LIMIT 1", tableName);
            Statement stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            id=rs.getInt("id");
        }catch (SQLException e){
            return -1;
        }
        return id;
    }

    public static void main(String[] args) throws SQLException {
        DataBaseManager db = new DataBaseManager();
//        db.selectAll();
//        try {
////            HashMap<String, String> hm = db.getUserData("Farbod0");
////            System.out.println("id: "+hm.get("id")+"  pass: "+hm.get("password"));
////            db.insertUserData("SomeONeElse","1111", "13");
//        }catch (SQLException e){
//            System.out.println("No user");
//        }
        System.out.println(db.getLastID());
    }
}
