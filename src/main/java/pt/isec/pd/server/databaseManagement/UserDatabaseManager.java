package pt.isec.pd.server.databaseManagement;

import pt.isec.pd.types.user;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDatabaseManager {
    private Connection connection;

    public UserDatabaseManager(String dbName) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbName + ".db");
            createTable();
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("erro");
            e.printStackTrace();
        }
    }

    private void createTable() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT, "
                + "NEstudante TEXT, "
                + "email TEXT UNIQUE, "
                + "password TEXT, "
                + "logged BOOLEAN)";
        try (PreparedStatement statement = connection.prepareStatement(createTableQuery)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public List<user> loadUsers() {
        List<user> userList = new ArrayList<>();
        String selectQuery = "SELECT * FROM users";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectQuery)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String NEstudante = resultSet.getString("NEstudante");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");
                boolean logged = resultSet.getBoolean("logged");

                user u = new user(name, NEstudante, email, password);
                u.setId(id);
                u.setLogged(logged);
                userList.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    public void saveUser(user u) {
        String insertQuery = "INSERT INTO users (name, NEstudante, email, password, logged) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setString(1, u.getName());
            statement.setString(2, u.getNEstudante());
            statement.setString(3, u.getEmail());
            statement.setString(4, u.getPassword());
            statement.setBoolean(5, u.isLogged());
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void deleteUser(String email) {
        String deleteQuery = "DELETE FROM users WHERE email=?";
        try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
            statement.setString(1, email);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updateUser(user u) {
        String updateQuery = "UPDATE users SET name=?, NEstudante=?, email=?, password=? WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setString(1, u.getName());
            statement.setString(2, u.getNEstudante());
            statement.setString(3, u.getEmail());
            statement.setString(4, u.getPassword());
            statement.setInt(5, u.getId());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("User updated successfully.");
            } else {
                System.out.println("User not found or not updated.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    public boolean userExists(String email) {
        String selectQuery = "SELECT * FROM users WHERE email=?";
        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
