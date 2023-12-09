package pt.isec.pd.server;

import pt.isec.pd.server.databaseManagement.EventDatabaseManager;
import pt.isec.pd.types.event;
import pt.isec.pd.types.user;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Optional;

public class eventManagement {

    private EventDatabaseManager dbManager;
    private static ArrayList<event> events = new ArrayList<>();
    public ArrayList<event> getEvents() {
        return events;
    }

    public eventManagement(EventDatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.events = (ArrayList<event>) dbManager.loadEvents();
    }


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

            System.out.println("DEBUG!\nUSERS TO ADD: " + newEvent.getUsersPresent());

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
    }


    public boolean removeEvent(int id) {
        if(events.removeIf(e -> e.getId() == id)){
            dbManager.deleteEvent(id);
            return true;
        }else
            return false;
    }

    public event getEventByCode(String otherParam) {
        return events.stream().filter((event event) -> event.getCode() == Integer.parseInt(otherParam)).findFirst().get();
    }

    public static event getEventById(int id) {
        return events.stream().filter((event event) -> event.getId() == id).findFirst().get();
    }

    public void updateEventDB(int id){
        dbManager.updateEvent(getEventById(id));
    }

    public void removeUserEvent(user u,event e){
        dbManager.removeUserFromEventRelationship(u,e);
    }

    public void checkEventsValidity(){
        Calendar now = Calendar.getInstance();

        events.stream().filter((event e) -> e.getCodeValidity().before(now)).forEach((event ev) -> {
            System.out.println("Event " + ev + " is no longer valid.");
            ev.generateRandomCode();
            updateEventDB(ev.getId());
            System.out.println("New Data: " + ev);
        });
    }




}
