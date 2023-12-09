package pt.isec.pd.server.databaseManagement;

import pt.isec.pd.server.eventManagement;
import pt.isec.pd.types.event;
import pt.isec.pd.types.user;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventDatabaseManager {
    private Connection connection;
    private RelationshipManager relationshipManager;
    public EventDatabaseManager(String dbName) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbName + ".db");
            createTable();
            relationshipManager = new RelationshipManager(dbName);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS events ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT, "
                + "local TEXT, "
                + "date TEXT, "
                + "start TEXT, "
                + "end TEXT, "
                + "code INTEGER, "
                + "codeValidity TEXT)";
        try (PreparedStatement statement = connection.prepareStatement(createTableQuery)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Calendar parseCalendar(String dateString) {
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            calendar.setTime(dateFormat.parse(dateString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
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

    public void deleteEvent(int eventId) {
        String deleteQuery = "DELETE FROM events WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(deleteQuery)) {
            statement.setInt(1, eventId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<event> loadEvents() {
        List<event> eventList = new ArrayList<>();
        String selectQuery = "SELECT * FROM events";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectQuery)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String local = resultSet.getString("local");
                Calendar date = parseDate(resultSet.getString("date"));
                Calendar start = parseTime(resultSet.getString("start"));
                Calendar end = parseTime(resultSet.getString("end"));
                int code = resultSet.getInt("code");
                Calendar codeValidity = parseTime(resultSet.getString("codeValidity"));

                event e = new event(name, local, date, start, end);
                e.setId(id);
                e.setCode(code);
                e.setCodeValidity(codeValidity);

                List<user> users = relationshipManager.getUsersByEvent(e);
                e.getUsersPresent().addAll(users);

                eventList.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return eventList;
    }
    public void removeUserFromEventRelationship(user user, event event) {
        relationshipManager.removeUserFromEventRelationship(user, event);
    }

    public void saveEvent(event e) {
        String insertQuery = "INSERT INTO events (name, local, date, start, end, code, codeValidity) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setString(1, e.getName());
            statement.setString(2, e.getLocal());
            statement.setString(3, e.getFormatDate(e.getDate()) + " " + e.getFormatTime(e.getDate()));
            statement.setString(4, e.getFormatTime(e.getStart()));
            statement.setString(5, e.getFormatTime(e.getEnd()));
            statement.setInt(6, e.getCode());
            statement.setString(7, e.getFormatTime(e.getCodeValidity()));
            statement.executeUpdate();

            // Fetch the maximum ID from the table
            int lastInsertedId = getLastInsertedId();
            e.setId(lastInsertedId); // Set the id in the event object

            for (user u : e.getUsersPresent()) {
                relationshipManager.addUserToEventRelationship(u, e);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private int getLastInsertedId() throws SQLException {
        String maxIdQuery = "SELECT MAX(id) FROM events";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(maxIdQuery)) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }
        return -1; // Return a default value if no ID is found
    }

    public void updateEvent(event e) {
        String updateQuery = "UPDATE events SET name=?, local=?, date=?, start=?, end=? WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setString(1, e.getName());
            statement.setString(2, e.getLocal());
            statement.setString(3, e.getFormatDate(e.getDate()) + " " + e.getFormatTime(e.getDate()));
            statement.setString(4, e.getFormatTime(e.getStart()));
            statement.setString(5, e.getFormatTime(e.getEnd()));
            statement.setInt(6, e.getId());  // Assuming id is the primary key

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Event updated successfully.");
            } else {
                System.out.println("Event not found or not updated.");
            }



            List<user> existingUsers = relationshipManager.getUsersByEvent(e);

            boolean containUser=false;

            for (user u : e.getUsersPresent()) {


                for (user user : existingUsers) {
                    if (user.getId() == u.getId()) {
                        containUser=true;
                        break;
                    }
                }
                if(!containUser)
                    relationshipManager.addUserToEventRelationship(u, e);
                containUser=false;

            }
            containUser=false;
            for (user u : existingUsers) {

                for (user user : e.getUsersPresent()) {
                    if (user.getId() == u.getId()) {
                        containUser=true;
                        break;
                    }
                }

                if (!containUser) {
                    relationshipManager.removeUserFromEventRelationship(u, e);
                }
            }

        } catch (SQLException ex) {
            System.out.println("EVENTDATABASEMANAGER: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void exportPresencesEvent(int eventoId) {

        event evento = eventManagement.getEventById(eventoId);

        if (evento != null) {
            System.out.println("Exportando presenças do evento " + evento.getName() + " (ID: " + eventoId + ")");

            try (FileWriter writer = new FileWriter("presencas_evento_" + eventoId + ".csv")) {
                // Escrever cabeçalho do CSV
                writer.write("Nome;Email\n");

                // Escrever presenças no CSV
                for (user usuario : evento.getUsersPresent()) {
                    writer.write(usuario.getName() + ";" + usuario.getEmail() + "\n");
                }

                System.out.println("Presenças exportadas com sucesso para o arquivo presencas_evento_" + eventoId + ".csv");
            } catch (IOException e) {
                System.out.println("Erro ao exportar presenças para CSV: " + e.getMessage());
            }
        } else {
            System.out.println("Evento não encontrado com o ID: " + eventoId);
        }
    }

    public void close() {
        try {
            if (relationshipManager != null) {
                relationshipManager.close();
            }
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
