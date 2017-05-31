package DataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by fsh on 5/31/17.
 */
public class DriverConnection {

    public static void connect(){
        Connection conn =null;
        try{
            // db parameters
            String url = "jdbc:sqlite:database/users.db";
            // create a connection
            conn = DriverManager.getConnection(url);

            System.out.println("Connected to the database");

        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            try{
                if(conn == null){
                    conn.close();
                }
            }catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        connect();
    }
}
