package pt.isec.pd.server;

import pt.isec.pd.types.event;
import pt.isec.pd.types.user;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

class jdbcManager{
    //BASE DE DADOS ACESSOS ASYNC E ESSAS CENAS!!!! EM SQLITE

    private static Connection conn;
    private static Statement stmt;
    private static ResultSet rs;
    private static String sql;
    private static String url = "jdbc:sqlite:database.db";
    
}


class managerCLients implements Runnable {
    Socket toClientSocket;
    userManagment userManager;
    eventManagement eventManager;

    public managerCLients(Socket socket, userManagment userManager, eventManagement evMgr) {
        this.toClientSocket = socket;
        this.userManager = userManager;
        this.eventManager = evMgr;
    }

    @Override
    public void run(){
        int myID;
        request req;
        //System.out.println("NA THREAD");
            try (
                    ObjectOutputStream oout = new ObjectOutputStream(toClientSocket.getOutputStream());
                    ObjectInputStream oin = new ObjectInputStream(toClientSocket.getInputStream())) {

                Object o = oin.readObject();
                if (o instanceof request) {
                    req = (request) o;
                    System.out.println("Recebido \"" + req.req + "\" de " +
                            toClientSocket.getInetAddress().getHostAddress() + ":" +
                            toClientSocket.getPort()+"["+req.user.getName()+"]");

                    if (!req.req.equalsIgnoreCase("REGISTER")
                            && !req.req.equalsIgnoreCase("LOGIN")
                            && !req.req.equalsIgnoreCase("LOGOUT")
                            && !req.req.equalsIgnoreCase("LIST")
                            && !req.req.equalsIgnoreCase("SEND")
                            && !req.req.equalsIgnoreCase("RECEIVE")
                            && !req.req.equalsIgnoreCase("QUIT")) {
                        System.out.println("Unexpected request received from " +
                                toClientSocket.getInetAddress().getHostAddress() + ":" +
                                toClientSocket.getPort());
                        String response ="Pedido nao esperado!";

                        oout.writeObject(response);
                        oout.flush();
                        return;

                    }
                    switch (req.req) {
                        case "REGISTER":
                            if(userManager.checkUser(req.user)){
                                String response = "User already exists";

                                oout.writeObject(response);
                                oout.flush();


                            }else {
                                userManager.createUser(req.user);
                                String response = "Utilizador criado!";

                                oout.writeObject(response);
                                oout.flush();
                            }
                            break;
                        case "LOGIN":
                            if(!userManager.checkUser(req.user)){
                                String response = "Utilizador nao existente...";

                                oout.writeObject(response);
                                oout.flush();
                            }
                            if(userManager.checkPassword(req.user.getEmail(), req.user.getPassword())) {
                                String response = "OK";

                                oout.writeObject(response);
                                oout.flush();
                            }else{
                                String response = "Palavra passe incorreta";

                                oout.writeObject(response);
                                oout.flush();
                            }
                            //login(req, oout);
                            break;
                        case "LOGOUT":
                            //logout(req, oout);
                            break;
                        case "LIST":
                            StringBuilder sb = new StringBuilder();
                            int counter = 0;
                            for (event e : eventManager.getEvents()) {
                                if(e.checkPresenceEmail(req.user.getEmail())){
                                    sb.append(e.toClientString());
                                    counter++;
                                }
                            }
                            if(counter==0){
                                String response = "Nao foram encontrados eventos!";

                                oout.writeObject(response);
                                oout.flush();
                            }else {
                                String header = "Descricao;Local;Data;HoraInicio\n";
                                String response = header+sb.toString()+"\n";

                                oout.writeObject(response);
                                oout.flush();
                            }

                            //list(req, oout);
                            break;
                        case "SEND":
                            //INSCREVER EM EVENTO
                            System.out.println("Evento a subscrever: "+req.otherParam);
                            if(req.otherParam == null){
                                String response = "Nao foi fornecido nenhum codigo de evento";

                                oout.writeObject(response);
                                oout.flush();
                                System.out.println("SEM EVENTO!");
                                break;
                            }else {
                                try {
                                    event ev = eventManager.getEventByCode(req.otherParam);
                                    if(ev.checkPresenceEmail(req.user.getEmail())){
                                        String response = "Ja se encontra inscrito ao evento "+ev.getName()+"!";

                                        oout.writeObject(response);
                                        oout.flush();
                                        System.out.println("JA SUBSCRITO!");
                                        break;
                                    }
                                    ev.addPresence(req.user);
                                    String response = "Inscrito ao Evento "+ev.getName()+"!";
                                    System.out.println("SUBSCRITO!");
                                    oout.writeObject(response);
                                    oout.flush();
                                }catch (NoSuchElementException e){

                                    String response = "Evento nao encontrado";

                                    oout.writeObject(response);
                                    oout.flush();
                                    break;
                                }

                                /*if(ev == null){
                                    String response = "Event not found";

                                    oout.writeObject(response);
                                    oout.flush();
                                    System.out.println("EVENTO NAO ENCONTRADO!");
                                    break;
                                }*/

                            }

                            //send(req, oout);
                            break;
                        case "RECEIVE":
                            //receive(req, oout);
                            break;
                        case "QUIT":
                            //quit(req, oout);
                            break;
                    }

                    // do something
                }



            } catch (Exception e) {
                System.out.println("Problema na comunicacao com o cliente " +
                        toClientSocket.getInetAddress().getHostAddress() + ":" +
                        toClientSocket.getPort() + "\n\t" + e);
            }



    }
}


class KBMgmt implements Runnable{
    boolean adminLogged = false;
    eventManagement eventManager;
    userManagment userManager;

    public KBMgmt(boolean adminLogged, eventManagement eventManager, userManagment userManager) {
        this.adminLogged = adminLogged;
        this.eventManager = eventManager;
        this.userManager = userManager;
    }


    private void createEvent(){
        int day,mth,yr;
        int startHr, startMn, endHr, endMn;
        Scanner sc = new Scanner(System.in);
        System.out.println("Nome do evento:");
        String name = sc.nextLine();
        System.out.println("Local do evento:");
        String local = sc.nextLine();
        System.out.println("Data do evento (dd/mm/aaaa):");
        String date = sc.nextLine();
        day = Integer.parseInt(date.split("/")[0]);
        mth = Integer.parseInt(date.split("/")[1]);
        yr = Integer.parseInt(date.split("/")[2]);
        System.out.println("Hora de inicio do evento (hh:mm):");
        String start = sc.nextLine();
        startHr = Integer.parseInt(start.split(":")[0]);
        startMn = Integer.parseInt(start.split(":")[1]);
        System.out.println("Hora de fim do evento (hh:mm):");
        String end = sc.nextLine();
        endHr = Integer.parseInt(end.split(":")[0]);
        endMn = Integer.parseInt(end.split(":")[1]);

        Calendar eventDate = Calendar.getInstance();
        eventDate.set(yr,mth,day);
        Calendar eventStart = Calendar.getInstance();
        eventStart.set(Calendar.HOUR_OF_DAY,startHr);
        eventStart.set(Calendar.MINUTE,startMn);
        Calendar eventEnd = Calendar.getInstance();
        eventEnd.set(Calendar.HOUR_OF_DAY,endHr);
        eventEnd.set(Calendar.MINUTE,endMn);
        eventManager.createEvent(name, local, eventDate, eventStart, eventEnd);
        eventManager.getEvents().get(eventManager.getEvents().size()-1).generateRandomCode();
        System.out.println("Codigo do evento: "+eventManager.getEvents().get(eventManager.getEvents().size()-1).getCode());
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        String buffer;
        while(true){
            System.out.println("Menu:\n\n1-admin login");
            if(adminLogged){
                System.out.println("2-criar evento\n" +
                        "3-listar eventos\n" +
                        "4-remover evento\n" +
                        "5-editar evento\n" +
                        "6-gerar código evento\n" +
                        "7-consultar presenças\n" +
                        "8-Obter ficheiro csv\n" +
                        "9-eliminar presença\n" +
                        "10-inserir presença\n" +
                        "11-logout\n");
            }
            System.out.println("Escreva \"exit\" para terminar o servidor");
            buffer = sc.nextLine();
            switch (buffer){
                case "1":
                    System.out.println("Email:");
                    String email = sc.nextLine();
                    System.out.println("Password:");
                    String password = sc.nextLine();
                    if(email.equals("admin") && password.equals("admin")){
                        adminLogged = true;
                        System.out.println("Login efetuado com sucesso");
                    }else{
                        System.out.println("Login falhou");
                    }
                    break;

                case "2":
                    createEvent();
                    break;
                case "3":
                    for (event e : eventManager.getEvents()) {
                        System.out.println(e.toString());
                    }
                    break;
                case "4":
                    System.out.println("Codigo do evento:");
                    String code = sc.nextLine();
                    eventManager.getEvents().remove(eventManager.getEventByCode(code));
                    break;
                case "5":

                    break;
                case "6":
                    System.out.println("Codigo do evento:");
                    String code2 = sc.nextLine();
                    eventManager.getEventByCode(code2).generateRandomCode();
                    System.out.println("Codigo do evento: "+eventManager.getEventByCode(code2).getCode());
                    break;
                case "7":
                    System.out.println("Codigo do evento:");
                    String code3 = sc.nextLine();
                    System.out.println("Presenças no evento"+eventManager.getEventByCode(code3).getName()+":");
                    for (user u : eventManager.getEventByCode(code3).getUsersPresent())
                    {
                        System.out.println(u.toString());
                    }
                    System.out.println(eventManager.getEventByCode(code3).getUsersPresent().toString());
                    break;
                case "8":
                    //csv
                    break;
                case "9":
                    System.out.println("Codigo do evento:");
                    String code4 = sc.nextLine();
                    System.out.println("Email do utilizador:");
                    String email2 = sc.nextLine();
                    eventManager.getEventByCode(code4).removePresence(userManager.getUser(email2));
                    break;
                case "10":
                    System.out.println("Codigo do evento:");
                    String code5 = sc.nextLine();
                    System.out.println("Email do utilizador:");
                    String email3 = sc.nextLine();
                    eventManager.getEventByCode(code5).addPresence(userManager.getUser(email3));
                    break;
                case "11":
                    adminLogged = false;
                    break;
                case "exit":
                    System.exit(0);
                    break;
                default:
                    System.out.println("Opção inválida");
                    break;
            }

            /*if(buffer.equalsIgnoreCase("exit")){
                System.exit(0);
            }*/
        }

    }
}



public class server {
    public static final String TIME_REQUEST = "TIME";

    public static void main(String args[]) {
        userManagment userManager = new userManagment();
        int nCreatedThreads = 0;
        eventManagement eventManager = new eventManagement();
        //String request;
        request req;
        Thread thr;

        /*if (args.length != 1) {
            System.out.println("Sintaxe: java TcpSerializedTimeServerIncomplete listeningPort");
            return;
        }*/

        Thread kb = new Thread(new KBMgmt(false, eventManager, userManager));
        kb.start();
        try (ServerSocket socket = new ServerSocket(/*Integer.parseInt(args[0]))*/5000)) {

            System.out.println("Servidor iniciado no porto " + socket.getLocalPort() + " ...");

            eventManager.createEvent("Evento1", "Local1", Calendar.getInstance(), Calendar.getInstance(), Calendar.getInstance());
            eventManager.getEvents().get(0).generateRandomCode();
            System.out.println("Codigo do evento: "+eventManager.getEvents().get(0).getCode());
            while (true) {
                Socket toClientSocket = socket.accept();
                thr = new Thread((Runnable) new managerCLients(toClientSocket, userManager,eventManager), "Thread_" + nCreatedThreads);
                thr.start();
                nCreatedThreads++;
            }

        } catch (NumberFormatException e) {
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        } catch (IOException e) {
            System.out.println("Ocorreu um erro ao nivel do socket de escuta:\n\t" + e);
        }


    }
}
