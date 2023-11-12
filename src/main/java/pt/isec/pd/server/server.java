package pt.isec.pd.server;

import pt.isec.pd.types.event;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.NoSuchElementException;


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
                                String response = header+sb.toString();

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

        try (ServerSocket socket = new ServerSocket(/*Integer.parseInt(args[0]))*/5000)) {

            System.out.println("Servidor iniciado no porto " + socket.getLocalPort() + " ...");

            eventManager.createEvent("Evento1", "Local1", Calendar.getInstance(), Calendar.getInstance(), Calendar.getInstance());
            eventManager.getEvents().get(0).generateRandomCode();
            System.out.println("Codigo do evento: "+eventManager.getEvents().get(0).getCode());
            while (true) {
                Socket toClientSocket = socket.accept();
                thr = new Thread((Runnable) new managerCLients(toClientSocket, userManager,eventManager), "Thread_" + nCreatedThreads);
                thr.run();
                nCreatedThreads++;
            }

        } catch (NumberFormatException e) {
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        } catch (IOException e) {
            System.out.println("Ocorreu um erro ao nivel do socket de escuta:\n\t" + e);
        }


    }
}
