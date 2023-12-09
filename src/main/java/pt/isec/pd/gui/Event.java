package pt.isec.pd.gui;

public class Event {

    private String descricao;
    private String local;
    private String data;
    private String horaInicio;

    public Event(String descricao, String local, String data, String horaInicio) {
        this.descricao = descricao;
        this.local = local;
        this.data = data;
        this.horaInicio = horaInicio;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getLocal() {
        return local;
    }

    public String getData() {
        return data;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    @Override
    public String toString() {
        return "Event{" +
                "descricao='" + descricao + '\'' +
                ", local='" + local + '\'' +
                ", data='" + data + '\'' +
                ", horaInicio='" + horaInicio + '\'' +
                '}';
    }
}
