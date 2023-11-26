package pt.isec.pd.server.databaseManagement;

import pt.isec.pd.server.HeartbeatSender;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseVersionControlManager {
    private Connection connection;
    public HeartbeatSender heartbeatSender;

    public DatabaseVersionControlManager(String dbName, int currentVersion, String rmiServiceName){
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbName + ".db");
            createTable();
            this.heartbeatSender = new HeartbeatSender();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS version_control ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "version INTEGER NOT NULL, "
                + "release_date TEXT NOT NULL)";
        try (PreparedStatement statement = connection.prepareStatement(createTableQuery)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Returns version
    public int getCurrentVersion(){
        String selectQuery = "SELECT MAX(version) FROM version_control";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectQuery)) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Return a default value if no version is found
    }

    //Updates Version using getCurrentVersion() method and adding 1
    public void updateVersion() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String formattedCurrentDate = dateFormat.format(new Date());
        String insertQuery = "INSERT INTO version_control (version, release_date) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setInt(1, getCurrentVersion()+1);
            statement.setString(2, formattedCurrentDate);
            statement.executeUpdate();
            heartbeatSender.sendHeartbeat(getCurrentVersion());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Mainly for Develop purposes
    public void resetDatabaseVersion() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String formattedCurrentDate = dateFormat.format(new Date());
        String insertQuery = "INSERT INTO version_control (version, release_date) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setInt(1, 0);
            statement.setString(2, formattedCurrentDate);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //Mainly for Develop purposes
    public String getCurrentVersionDate() {
        String selectDateQuery = "SELECT release_date FROM version_control WHERE version = ?";
        try (PreparedStatement statement = connection.prepareStatement(selectDateQuery)) {
            statement.setInt(1, getCurrentVersion());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("release_date");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if no release date is found
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