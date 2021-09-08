package server;

import java.sql.SQLException;

public class SimpleAuth implements Authentication {

    private final BDSQLite base = new BDSQLite();

    public SimpleAuth() {
        try {
            BDSQLite.onCreate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNick(String log, String pass) {
        try {
            return BDSQLite.loginUser(log, pass);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean registration(String log, String pass, String nick) {
        try {
            return BDSQLite.newUser(log, pass, nick);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        try {
            BDSQLite.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
