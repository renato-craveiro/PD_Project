package pt.isec.pd.types;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Criação de um evento, sendo este caracterizado pelo nome, local, data de realização,
 * hora de início e hora de fim;
 * ▪ Edição dos dados de um evento. O período de realização apenas pode ser alterado se
 * não possuir qualquer presença registada;
 * ▪ Eliminação de um evento, desde que ainda não tenha qualquer presença registada;
 * ▪ Consulta dos eventos criados, podendo ser aplicados diversos tipos de critérios/filtros
 * (período, nome do evento, etc.);
 * ▪ Geração de um código para registo de presenças em um evento que esteja a decorrer
 * no momento, com indicação do tempo de validade em minutos. Podem ser gerados
 * códigos sucessivos para o mesmo evento, prevalecendo o mais recente (os anteriores
 * deixam de ser válidos);
 */
public class event {
    private static int eventID = 0;

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
        eventID++;
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
        return "event{ "+"id="+eventID + " name=" + name + ", local=" + local +", date=" + getFormatDate(date) + ", start=" + getFormatTime(start) + ", end=" + getFormatTime(end) + " code=" + code + " codeValidity=" + getFormatTime(codeValidity) +'}';
        //return "event{ "+"id="+eventID + "name=" + name + ", local=" + local + ", date=" + date + ", start=" + start + ", end=" + end + " code=" + code + " codeValidity=" + codeValidity.toString() +'}';
    }

    public String toClientString() {
        return name + ";" + local + ";" + getFormatDate(date) + ";" + getFormatTime(start);
        //return name + ";" + local + ";" + date.get(Calendar.DAY_OF_MONTH)+"/"+date.get(Calendar.MONTH)+"/"+date.get(Calendar.YEAR) + ";" + start.get(Calendar.HOUR_OF_DAY)+":"+start.get(Calendar.MINUTE);
    }
    public void addPresence(user u){
        usersPresent.add(u);
    }

    public void removePresence(user u){
        usersPresent.remove(u);
    }

    public boolean checkPresenceEmail(String email){
        return usersPresent.stream().anyMatch((user user) -> user.getEmail().equals(email));
    }

}
