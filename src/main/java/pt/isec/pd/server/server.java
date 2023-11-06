package pt.isec.pd.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.GregorianCalendar;


class managerCLients implements Runnable {
    Socket toClientSocket;
    userManagment userManager;

    public managerCLients(Socket socket, userManagment userManager) {
        this.toClientSocket = socket;
        this.userManager = userManager;
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

                        /*if (!request.equalsIgnoreCase(TIME_REQUEST)) {
                            System.out.println("Unexpected request");
                            continue;
                        }*/
                    String response ="ok";

                    oout.writeObject(response);
                    oout.flush();
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

            System.out.println("TCP Time Server iniciado no porto " + socket.getLocalPort() + " ...");

            while (true) {
                Socket toClientSocket = socket.accept();
                thr = new Thread((Runnable) new managerCLients(toClientSocket, userManager), "Thread_" + nCreatedThreads);
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
