package ir.fshahinfar1.iust.DataBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 * Created by fsh on 7/7/17.
 */
public class UsersData extends DataBaseManager {

    public UsersData(String url, String tableName) throws SQLException {
        super(url, tableName);
    }

    public HashMap getUserData(String userName) throws SQLException {
        String sql = "SELECT * FROM " + tableName + " WHERE Username=" + "\"" + userName + "\"" + "LIMIT 1";
        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            HashMap<String, String> result = new HashMap<String, String>();
            result.put("username", rs.getString("Username"));
            result.put("id", rs.getString("Id"));
            result.put("password", rs.getString("Password"));
            return result;
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String getUserName(String id) throws SQLException {
        // todo: working here ...
        String sql = "SELECT Username FROM " + tableName + " WHERE Id=" + "\"" + id + "\"" + "LIMIT 1";
        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(sql);
            String result = rs.getString("Username");
            return result;
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void insertUserData(String userName, String password, String id) throws SQLException {
        String sql = String.format("INSERT INTO %s (Id, Username, Password) VALUES(%s,'%s','%s')",
                tableName, id, userName, password);
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }


}
