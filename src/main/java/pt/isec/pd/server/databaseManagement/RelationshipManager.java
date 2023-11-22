package pt.isec.pd.server.databaseManagement;

import pt.isec.pd.types.event;
import pt.isec.pd.types.user;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RelationshipManager {
    private Connection connection;

    public RelationshipManager(String dbName) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbName+ ".db");
            createRelationshipTable();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void createRelationshipTable() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS user_event_relationship ("
                + "user_id INTEGER, "
                + "event_id INTEGER, "
                + "PRIMARY KEY (user_id, event_id), "
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, "
                + "FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE)";
        try (PreparedStatement statement = connection.prepareStatement(createTableQuery)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addUserToEventRelationship(user user, event event) {
        String insertQuery = "INSERT INTO user_event_relationship (user_id, event_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setInt(1, user.getId());
            statement.setInt(2, event.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("User already in event");
            ex.printStackTrace();
        }
    }

    public void removeUserFromEventRelationship(user user, event event) {
        String deleteQuery = "DELETE FROM user_event_relationship WHERE user_id=? AND event_id=?";
        try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
            statement.setInt(1, user.getId());
            statement.setInt(2, event.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public List<user> getUsersByEvent(event event) {
        List<user> userList = new ArrayList<>();
        String selectQuery = "SELECT users.* FROM users " +
                "JOIN user_event_relationship ON users.id = user_event_relationship.user_id " +
                "WHERE user_event_relationship.event_id=?";
        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            statement.setInt(1, event.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    String NEstudante = resultSet.getString("NEstudante");
                    String email = resultSet.getString("email");
                    String password = resultSet.getString("password");

                    user u = new user(name, NEstudante, email, password);
                    u.setId(id);
                    userList.add(u);
                }
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
