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
            e.printStackTrace();
        }
    }

    private void createTable() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS users ("
                + "name TEXT, "
                + "NEstudante TEXT, "
                + "email TEXT PRIMARY KEY, "
                + "password TEXT, "
                + "logged BOOLEAN)";
        try (PreparedStatement statement = connection.prepareStatement(createTableQuery)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                String name = resultSet.getString("name");
                String NEstudante = resultSet.getString("NEstudante");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");
                boolean logged = resultSet.getBoolean("logged");

                user u = new user(name, NEstudante, email, password);
                u.setLogged(logged);
                userList.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
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
