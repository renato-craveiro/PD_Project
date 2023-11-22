package pt.isec.pd.server;

import pt.isec.pd.server.databaseManagement.UserDatabaseManager;
import pt.isec.pd.types.user;

import java.util.ArrayList;

public class userManagment {
    ArrayList<user> users = new ArrayList<>();
    private UserDatabaseManager dbManager;

    public userManagment(UserDatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.users = (ArrayList<user>) dbManager.loadUsers();
    }

    public void editUser(String email, user newUser){
        user userToUpdate = getUser(email);
        if (userToUpdate != null) {
            // Update the details of the existing user with the new user's details
            userToUpdate.setName(newUser.getName());
            userToUpdate.setNEstudante(newUser.getNEstudante());
            userToUpdate.setEmail(newUser.getEmail());
            userToUpdate.setPassword(newUser.getPassword());
            dbManager.updateUser(userToUpdate);
            System.out.println("User updated successfully.");
        }
    }

    public void createAdminIfNotExists() {
        String adminEmail = "admin";  // You can set the admin email
        String adminPassword = "admin"; // You can set the admin password

        if (!dbManager.userExists(adminEmail)) {
            // Create the admin user
            createUser("Admin", "Admin", adminEmail, adminPassword);
            System.out.println("Admin user created successfully.");
        } else {
            System.out.println("Admin account available.");
        }
    }


    public synchronized void createUser(String name, String NEstudante, String email, String password) {
        user tmp = new user(name, NEstudante, email, password);
        users.add(tmp);
        dbManager.saveUser(tmp);
    }

    public synchronized void  createUser(user u) {
        users.add(u);
        dbManager.saveUser(u);
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

    public synchronized void removeUser(String email) {
        users.removeIf((user user) -> user.getEmail().equals(email));
    }

    public synchronized void removeUser(user u) {
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
