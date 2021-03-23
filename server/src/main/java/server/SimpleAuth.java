package server;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

public class SimpleAuth implements Authentication{

    private class DataUser{
        String log;
        String pass;
        String nick;
        public DataUser(String log, String pass, String nick){
            this.log = log;
            this.pass = pass;
            this.nick = nick;
        }
    }

    private List<DataUser> users;

    public SimpleAuth(){
        users = new ArrayList<>();

        users.add(new DataUser("qwe", "qwe", "qwe"));
        users.add(new DataUser("asd", "asd", "asd"));
        users.add(new DataUser("zxc", "zxc", "zxc"));

        for (int i = 0; i < 10; i++) {
            users.add(new DataUser("log" + i, "pass" + i, "nick" + i));
        }
    }

    @Override
    public String getNick(String log, String pass) {
        for (DataUser user : users) {
            if (user.log.equals(log) && user.pass.equals(pass)){
                return user.nick;
            }
        }
        return null;
    }

    @Override
    public boolean registration(String log, String pass, String nick) {
        for (DataUser user : users) {
            if (user.log.equals(log) || user.nick.equals(nick)){
                return false;
            }
        }
        users.add(new DataUser(log, pass, nick));
        return true;
    }
}
