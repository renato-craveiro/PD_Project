package pt.isec.pd.client;

import pt.isec.pd.server.request;
import pt.isec.pd.types.user;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Calendar;

public class client {

    public static final int TIMEOUT = 10; //segundos
    public static void main(String[] args) throws IOException {
        request req;// = new request();
        String response;

        /*if(args.length != 2){
            System.out.println("Sintaxe: java TcpSerializedTimeClientIncomplete serverAddress serverUdpPort");
            return;
        }*/


        try(Socket socket = new Socket("localhost",5000/*InetAddress.getByName(args[0]), Integer.parseInt(args[1])*/);
            ObjectInputStream oin = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream())){

            socket.setSoTimeout(TIMEOUT*1000);
            user user = new user("teste", "teste", "teste","teste");

            req = new request("REGISTER", user);

            oout.writeObject(req);
            oout.flush();

            //Deserializa a resposta recebida em socket
            response = (String)oin.readObject();

            if(response == null){
                System.out.println("O servidor nao enviou qualquer respota antes de"
                        + " fechar aligacao TCP!");
            }else{
                System.out.println("Respota do Servidor: " + response);
            }

        }catch(Exception e){
            System.out.println("Ocorreu um erro no acesso ao socket:\n\t"+e);
        }
    }

}
