package pt.isec.pd.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.GregorianCalendar;


class manageCLients implements Runnable{
    Socket socket;
    userManagment userManager;

    public manageCLients(Socket socket, userManagment userManager) {
        this.socket = socket;
        this.userManager = userManager;
    }



    @Override
    public void run() {
        int myId;
        int nWorkers;
        long nIntervals;
        double myResult;

        //Cria um ObjectInputStream e um ObjectOutputStream associados ao socket s
        try(ObjectInputStream in = new ObjectInputStream(this.socket.getInputStream())/***/;
            ObjectOutputStream out = new ObjectOutputStream(this.socket.getOutputStream())/***/){
            Object obj = in.readObject();

            if(obj instanceof request cltReq) {
                switch (cltReq.getReq()) {
                    case "REGISTER":
                        if (!userManager.checkUser(cltReq.getUser().getEmail())) {
                            userManager.createUser(cltReq.getUser());
                            out.writeObject("REGISTERED");
                        } else {
                            out.writeObject("NOT REGISTERED. USER ALREADY EXISTS");
                        }
                        break;
                    case "LOGIN":
                        if (userManager.checkUser(cltReq.getUser().getEmail())) {
                            if (userManager.checkPassword(cltReq.getUser().getEmail(), cltReq.getUser().getPassword())) {
                                userManager.getUser(cltReq.getUser().getEmail()).setLogged(true);
                                out.writeObject("LOGGED IN");
                                System.out.println(cltReq.getUser().getName() + " LOGGED IN");
                            } else {
                                out.writeObject("NOT LOGGED IN. WRONG PASSWORD");
                            }
                        }

                            break;
                            case "LOGOUT":
                                break;
                            case "LIST":
                                break;
                            case "SEND":
                                break;
                            case "RECEIVE":
                                break;
                            case "QUIT":
                                break;
                            default:
                                break;
                        }
                }
            } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);


            }
            System.out.format("<%s> %.10f\n", Thread.currentThread().getName(), "ok");
        }
    }

public class server {
    static eventManagement eventManager;

    static userManagment userManager;

    public static final int MAX_SIZE = 4000;
    //public static final String TIME_REQUEST = "TIME";
    public static final int TIMEOUT = 10;




    public static void main(String[] args) {
        eventManager = new eventManagement();
        userManager = new userManagment();
        int nCreatedThreads = 0;
        Thread thr;


        if(args.length != 1){
            System.out.println("Sintaxe: java TcpSerializedTimeServerIncomplete listeningPort");
            return;
        }

        try(ServerSocket socket = new ServerSocket(Integer.parseInt(args[0]))){

            System.out.println("TCP Time Server iniciado no porto " + socket.getLocalPort() + " ...");
            while(true){
                Socket toClientSocket = socket.accept();
                nCreatedThreads++;
                thr = new Thread((Runnable) new manageCLients(toClientSocket , userManager), "Thread_"+nCreatedThreads);
                thr.run();
            }


        }catch(NumberFormatException e){
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        }catch(IOException e){
            System.out.println("Ocorreu um erro ao nivel do socket de escuta:\n\t"+e);
        }



    }
}
