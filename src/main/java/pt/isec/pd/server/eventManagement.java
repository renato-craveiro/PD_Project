package pt.isec.pd.server;

import pt.isec.pd.server.databaseManagement.EventDatabaseManager;
import pt.isec.pd.types.event;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Optional;
import java.util.stream.*;
import java.util.ArrayList;

public class eventManagement {

    private EventDatabaseManager dbManager;
    private ArrayList<event> events = new ArrayList<>();
    public ArrayList<event> getEvents() {
        return events;
    }

    public eventManagement(EventDatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.events = (ArrayList<event>) dbManager.loadEvents();
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


    public void editEvent(int id, event newEvent){
        Optional<event> eventToUpdate = events.stream().filter(e -> e.getId() == id).findFirst();

        if (eventToUpdate.isPresent()) {
            // Update the details of the existing event with the new event's details
            event existingEvent = eventToUpdate.get();
            existingEvent.setName(newEvent.getName());
            existingEvent.setLocal(newEvent.getLocal());
            existingEvent.setDate(newEvent.getDate());
            existingEvent.setStart(newEvent.getStart());
            existingEvent.setEnd(newEvent.getEnd());


            // Update the event in the database
            dbManager.updateEvent(existingEvent);
            System.out.println("Event updated successfully. New data: " + existingEvent);
        } else {
            System.out.println("Event with id " + id + " not found.");
        }

    }

    public void createEvent(String name, String local, Calendar date, Calendar start, Calendar end) {
        event newEvent = new event(name, local, date, start, end);
        events.add(newEvent);
        dbManager.saveEvent(newEvent);

        //events.add(new event(name, local, date, start, end));
    }

    /*public void removeEvent(int code) {

        events.removeIf(e -> e.getCode() == code);
        dbManager.deleteEvent(code);
    }*/

    public boolean removeEvent(int id) {
        if(events.removeIf(e -> e.getId() == id)){
            dbManager.deleteEvent(id);
            return true;
        }else
            return false;
        //dbManager.deleteEvent(id);
    }

    public event getEventByCode(String otherParam) {
        return events.stream().filter((event event) -> event.getCode() == Integer.parseInt(otherParam)).findFirst().get();
    }

    public event getEventById(int id) {
        return events.stream().filter((event event) -> event.getId() == id).findFirst().get();
    }

    public void updateEventDB(int id){
        dbManager.updateEvent(getEventById(id));
    }

    public void checkEventsValidity(){
        Calendar now = Calendar.getInstance();
        //System.out.println("Now = " + now.getTime());
        events.stream().filter((event e) -> e.getCodeValidity().before(now)).forEach((event ev) -> {
            System.out.println("Event " + ev + " is no longer valid.");
            ev.generateRandomCode();
            updateEventDB(ev.getId());
            System.out.println("Event updated successfully. Data: " + ev);
        });
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
