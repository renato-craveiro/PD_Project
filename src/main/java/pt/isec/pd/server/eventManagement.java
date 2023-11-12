package pt.isec.pd.server;

import pt.isec.pd.types.event;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.stream.*;
import java.util.ArrayList;

public class eventManagement {


    private ArrayList<event> events = new ArrayList<>();
    public ArrayList<event> getEvents() {
        return events;
    }

    /*public void setEvents(ArrayList<event> events) {
        this.events = events;
    }*/




    //-----------------------CSV???-----------------------

    /*//https://www.baeldung.com/java-csv
    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    //https://www.baeldung.com/java-csv end*/



    public void createEvent(String name, String local, Calendar date, Calendar start, Calendar end) {
        events.add(new event(name, local, date, start, end));
    }

    public event getEventByCode(String otherParam) {
        return events.stream().filter((event event) -> event.getCode() == Integer.parseInt(otherParam)).findFirst().get();
    }


    /*public void exportToCSV() {
        File report = new File("events.csv");
        try (PrintWriter pw = new PrintWriter(report)) {
            events.stream()
                    .map((event data) -> convertToCSV(data))
                    .forEach(pw::println);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        assertTrue(csvOutputFile.exists());
    }*/


}
