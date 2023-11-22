package pt.isec.pd.types;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;


public class event {
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    int id;
    private static final AtomicInteger count = new AtomicInteger(0);
    public ArrayList<user> getUsersPresent() {
        return usersPresent;
    }


    ArrayList<user> usersPresent = new ArrayList<>();
    String name;
    String local;
    Calendar date;
    Calendar start;
    Calendar end;

    public void setCode(int code) {
        this.code = code;
    }

    int code;

    public Calendar getCodeValidity() {
        return codeValidity;
    }

    public void setCodeValidity(Calendar codeValidity) {
        this.codeValidity = codeValidity;
    }

    Calendar codeValidity;

    public event(String name, String local, Calendar date, Calendar start, Calendar end) {
        //eventID++;
        id=count.incrementAndGet();
        setName(name);
        setLocal(local);
        setDate(date);
        setStart(start);
        setEnd(end);
        generateRandomCode();
    }

    public void generateRandomCode(){
        codeValidity = Calendar.getInstance();
        codeValidity.add(Calendar.MINUTE, 5);
        code = (int) (Math.random() * 1000000);
    }

    public void generateRandomCodeWithValidity(int validity){
        codeValidity = Calendar.getInstance();
        codeValidity.add(Calendar.MINUTE, validity);
        code = (int) (Math.random() * 1000000);
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getLocal() {
        return local;
    }

    public Calendar getDate() {
        return date;
    }

    public Calendar getStart() {
        return start;
    }

    public Calendar getEnd() {
        return end;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public void setStart(Calendar start) {
        this.start = start;
    }

    public void setEnd(Calendar end) {
        this.end = end;
    }


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

    @Override
    public String toString() {
        return "event{ "+"id="+id + " name=" + name + ", local=" + local +", date=" + getFormatDate(date) + ", start=" + getFormatTime(start) + ", end=" + getFormatTime(end) + " code=" + code + " codeValidity=" + getFormatTime(codeValidity) +'}';

    }

    public String toClientString() {
        return name + ";" + local + ";" + getFormatDate(date) + ";" + getFormatTime(start);

    }
    public void addPresence(user u){
        usersPresent.add(u);
    }

    public void removePresence(user u){

        usersPresent.removeIf(user -> user.getEmail().equals(u.getEmail()));

    }

    public boolean checkPresenceEmail(String email){
        return usersPresent.stream().anyMatch((user user) -> user.getEmail().equals(email));
    }

}
