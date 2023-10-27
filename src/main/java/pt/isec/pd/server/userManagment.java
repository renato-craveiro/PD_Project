package pt.isec.pd.server;

import pt.isec.pd.types.user;

import java.util.ArrayList;

public class userManagment {
    ArrayList<user> users = new ArrayList<>();

    public void createUser(String name, String NEstudante, String email, String password) {
        users.add(new user(name, NEstudante, email, password));
    }

    public void createUser(user u) {
        users.add(u);
    }

    public boolean checkUser(user u) {
        return users.stream().anyMatch((user user) -> user.checkUser(u));
    }

    public boolean checkUser(String email) {
        return users.stream().anyMatch((user user) -> user.getEmail().equals(email));
    }

    public boolean checkPassword(String email, String password) {
        return users.stream().anyMatch((user user) -> user.getEmail().equals(email) && user.getPassword().equals(password));
    }

    public user getUser(String email) {
        return users.stream().filter((user user) -> user.getEmail().equals(email)).findFirst().get();
    }

    public void removeUser(String email) {
        users.removeIf((user user) -> user.getEmail().equals(email));
    }

    public void removeUser(user u) {
        users.removeIf((user user) -> user.checkUser(u));
    }

    public void updateUser(user u) {
        users.stream().filter((user user) -> user.checkUser(u)).forEach((user user) -> {
            user.setName(u.getName());
            user.setNEstudante(u.getNEstudante());
            user.setEmail(u.getEmail());
            user.setPassword(u.getPassword());
        });
    }

    public void updateUser(String email, String name, String NEstudante, String password) {
        users.stream().filter((user user) -> user.getEmail().equals(email)).forEach((user user) -> {
            user.setName(name);
            user.setNEstudante(NEstudante);
            user.setPassword(password);
        });
    }

    public boolean isLogged(String email) {
        return users.stream().anyMatch((user user) -> user.getEmail().equals(email) && user.isLogged());
    }

    public void setLogged(String email, boolean logged) {
        users.stream().filter((user user) -> user.getEmail().equals(email)).forEach((user user) -> user.setLogged(logged));
    }






}
