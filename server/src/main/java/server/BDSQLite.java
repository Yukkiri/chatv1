package server;

import org.omg.PortableInterceptor.ServerRequestInfo;

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

    public static void onCreate() throws SQLException, ClassNotFoundException {
        setConnection();
        statement = connection.createStatement();
        statement.execute("CREATE TABLE if NOT EXISTS 'users' " +
                "('login' text PRIMARY KEY, 'password' text, 'nickname' text);");
    }

    public static boolean newUser(String log, String pass, String nick) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM 'users' WHERE 'login' = " + log + "OR 'nickname' = " + nick);
        if (resultSet.wasNull()){
            statement.execute("INSERT INTO 'users' ('login', 'password', 'nickname') VALUES ('" + log + "', '" + pass + "', '" + nick + "')");
            return true;
        }
        return false;
    }

    public static String loginUser(String log, String pass) throws SQLException {
        resultSet = statement.executeQuery("SELECT * FROM 'users'");

        while (resultSet.next()){
            String login = resultSet.getString("login");
            String password = resultSet.getString("password");
            String nickname = resultSet.getString("nickname");
            if (log.equals(login) && pass.equals(password)){
                return nickname;
            }
        }
        return null;
    }

    public static void closeConnection() throws SQLException {
        resultSet.close();
        statement.close();
        connection.close();
    }
}
