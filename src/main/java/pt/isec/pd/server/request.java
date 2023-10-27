package pt.isec.pd.server;

import java.io.Serializable;

import pt.isec.pd.types.user;

public class request implements Serializable {
    String req;

    user user;
    private static final String[] validRequests = {"REGISTER", "LOGIN", "LOGOUT", "LIST", "SEND", "RECEIVE", "QUIT"};

    public request (user user) {
        this.user = user;
    }

    public pt.isec.pd.types.user getUser() {
        return user;
    }

    public request(String req, user user) {
        if (isValid(req)) {
            this.user = user;
            this.req = req;
        } else {
            this.req = "INVALID";
        }
    }

    public static boolean isValid(String req) {
        for (String validRequest : validRequests) {
            if (validRequest.equals(req)) {
                return true;
            }
        }
        return false;
    }

    public String getReq() {
        return req;
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

}
