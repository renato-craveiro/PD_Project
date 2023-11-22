package pt.isec.pd.types;


import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * Registo de um novo utilizador, sendo este caraterizado por um nome, um número de
 * identificação (cartão de cidadão, número de identificação fiscal, número de
 * estudante, etc.), um endereço de email e uma password. O endereço de email deve
 * ser único e serve de username.
 *
 */
public class user implements Serializable {
    private static final AtomicInteger count = new AtomicInteger(0);

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    int id;
    String name;
    String NEstudante;
    String email;
    String password;

    boolean logged = false;

    public user(String name, String NEstudante, String email, String password) {
        this.id = count.incrementAndGet();
        this.name = name;
        this.NEstudante = NEstudante;
        this.email = email;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getNEstudante() {
        return NEstudante;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNEstudante(String NEstudante) {
        this.NEstudante = NEstudante;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean checkUser(user u) {
        return this.email.equals(u.getEmail());
    }

    @Override
    public String toString() {
        return "user{"+"id="+id + "name=" + name + ", NEstudante=" + NEstudante + ", email=" + email + ", password=" + password + '}';
    }

    public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }


}
