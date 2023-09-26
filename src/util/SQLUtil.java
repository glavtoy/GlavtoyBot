package util;

import java.sql.*;

public class SQLUtil {

    private String url;
    private String user;
    private String password;
    private Connection conn;
    private Statement statement;
    private String bankTable;

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public SQLUtil(String url, String user, String password) throws SQLException {
        this.url = url;
        this.user = user;
        this.password = password;
        this.conn = DriverManager.getConnection(url, user, password);
        this.statement = conn.createStatement();
    }

    public void executeQuery(String query) {
        try {
            //conn = DriverManager.getConnection(url, user, password);
            //Statement statement = conn.createStatement();
            statement.executeQuery(query);
            //conn.close();
        } catch (SQLException e) {}
    }

    public ResultSet executeQueryWithResultSet(String query) {

        try {
            //conn = DriverManager.getConnection(url, user, password);
            //Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet != null) {
                //conn.close();
                return resultSet;
            }
        } catch (SQLException e) {}
        return null;
    }
}
