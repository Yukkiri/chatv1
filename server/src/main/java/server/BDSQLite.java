package server;

import java.sql.*;

public class BDSQLite {
    public static Connection connection;
    public static Statement statement;
    public static ResultSet resultSet;

    public static void setConnection() throws ClassNotFoundException, SQLException {
        connection = null;
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:CHAT.users");
    }

    public static void onCreate() throws SQLException {
        statement = connection.createStatement();
        statement.execute("CREATE TABLE if NOT EXISTS 'users' " +
                "('login' text PRIMARY KEY, 'password' text, 'nickname' text);");
    }

    public static void newUser(String login, String password, String nickname) throws SQLException {
        statement.execute("INSERT INTO 'users' ('login', 'password', 'nickname') VALUES ('" + login + "', '" + password + "', '" + nickname + "')");
    }

    public static void loginUser() throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM 'users'");

        while (resultSet.next()){
            String login = resultSet.getString("login");
            String password = resultSet.getString("password");
            String nickname = resultSet.getString("nickname");
        }
    }

    public static void closeConnection() throws SQLException {
        resultSet.close();
        statement.close();
        connection.close();
    }
}
