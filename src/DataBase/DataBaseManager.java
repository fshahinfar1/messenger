package DataBase;

import javax.xml.crypto.Data;
import java.sql.*;

/**
 * Created by fsh on 5/31/17.
 */
public class DataBaseManager {
    private Connection connection;

    public DataBaseManager(){
        connect();
    }

    public void connect(){
        try{
            // db parameters
            String url = "jdbc:sqlite:database/users.db";
            // create a connection
            connection  = DriverManager.getConnection(url);

            System.out.println("Connected to the database");
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            try{
                if(connection == null){
                    connection.close();
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    public void selectAll(){
        String sql = "SELECT * FROM users;";
        ResultSet rs = null;
        Statement stmt = null;
        try{
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);

            while(rs.next()){
                System.out.println(rs.getString("userName"));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public boolean hasUserName(String userName){
        // todo: working here ...
        String sql = "";
        return false;
    }

    public static void main(String[] args) {
        DataBaseManager db = new DataBaseManager();
        db.selectAll();
    }
}
