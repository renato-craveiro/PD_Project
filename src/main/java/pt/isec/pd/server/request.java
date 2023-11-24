package pt.isec.pd.server;

import java.io.Serializable;

import pt.isec.pd.types.user;

public class request implements Serializable {

    private static final String[] validRequests = {"REGISTER", "LOGIN", "LOGOUT", "LIST","EXPORT_CSV", "SEND", "CHANGE", "QUIT"};
    String req;
    user user, nUser;
    String otherParam;


    public request (user user) {
        this.user = user;
    }

    public request(String req, user user) {
        if (isValid(req)) {
            this.user = user;
            this.req = req;
        } else {
            this.req = "INVALID";
        }
    }

    public request(String req, user user, user newUser) {
        if (isValid(req)) {
            this.user = user;
            this.req = req;
            this.nUser = newUser;
        } else {
            this.req = "INVALID";
        }
    }

    public request(String req, user user, String other) {
        if (isValid(req)) {
            this.user = user;
            this.req = req;
            this.otherParam = other;
        } else {
            this.req = "INVALID";
        }
    }

    public pt.isec.pd.types.user getUser() {
        return user;
    }

    public String getOtherParam() {
        return otherParam;
    }

    public String getReq() {
        return req;
    }

    public void setOtherParam(String otherParam) {
        this.otherParam = otherParam;
    }


    public void register(){
        req = "REGISTER";
    }

    public void login(){
        req = "LOGIN";
    }

    public void logout(){
        req = "LOGOUT";
    }

    public void list(){
        req = "LIST";
    }

    public void send(){
        req = "SEND";
    }

    public void receive(){
        req = "RECEIVE";
    }

    public void quit() {
        req = "QUIT";
    }

    public static boolean isValid(String req) {
        for (String validRequest : validRequests) {
            if (validRequest.equals(req)) {
                return true;
            }
        }
        return false;
    }

}
