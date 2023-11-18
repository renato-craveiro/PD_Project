package pt.isec.pd.server.databaseManagement;

import pt.isec.pd.types.event;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventDatabaseManager {
    private Connection connection;

    public EventDatabaseManager(String dbName) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbName + ".db");
            createTable();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS events ("
                + "name TEXT, "
                + "local TEXT, "
                + "date TEXT, "
                + "start TEXT, "
                + "end TEXT, "
                + "code INTEGER PRIMARY KEY, "
                + "codeValidity TEXT)";
        try (PreparedStatement statement = connection.prepareStatement(createTableQuery)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveEvent(event e) {
        String insertQuery = "INSERT INTO events (name, local, date, start, end, code, codeValidity) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setString(1, e.getName());
            statement.setString(2, e.getLocal());
            statement.setString(3, e.getFormatDate(e.getDate()));
            statement.setString(4, e.getFormatTime(e.getStart()));
            statement.setString(5, e.getFormatTime(e.getEnd()));
            statement.setInt(6, e.getCode());
            statement.setString(7, e.getFormatTime(e.getCodeValidity()));
            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public List<event> loadEvents() {
        List<event> eventList = new ArrayList<>();
        String selectQuery = "SELECT * FROM events";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectQuery)) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String local = resultSet.getString("local");
                Calendar date = parseDate(resultSet.getString("date"));
                Calendar start = parseTime(resultSet.getString("start"));
                Calendar end = parseTime(resultSet.getString("end"));
                int code = resultSet.getInt("code");
                Calendar codeValidity = parseTime(resultSet.getString("codeValidity"));

                event e = new event(name, local, date, start, end);
                e.setCode(code);
                e.setCodeValidity(codeValidity);
                eventList.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return eventList;
    }

    public void deleteEvent(int code) {
        String deleteQuery = "DELETE FROM events WHERE code=?";
        try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
            statement.setInt(1, code);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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


    public Calendar parseTime(String timeString) {
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            calendar.setTime(timeFormat.parse(timeString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    public Calendar parseDate(String dateString) {
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            calendar.setTime(dateFormat.parse(dateString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    /*

    //Recebe um Calender date, e retorna uma String no formato dd/mm/yyyy dessa date
    public String getFormatDate(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(calendar.getTime());
    }
    //Recebe um Calender date, e retorna uma String no formato hh:mm dessa date
    public String getFormatTime(Calendar calendar) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        return timeFormat.format(calendar.getTime());
    }

    */


}
