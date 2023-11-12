package pt.isec.pd.client;

import pt.isec.pd.server.request;
import pt.isec.pd.types.user;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.Objects;
import java.util.Scanner;

import static java.lang.System.exit;


public class client {
    static user currUser;
    static String srvAdress;
    static int srvPort;

    static Socket socket;
    public static String sendRequest(String reqStr){
        request req;// = new request();


        String response;
        try(Socket socket = new Socket(InetAddress.getByName(srvAdress),srvPort/*InetAddress.getByName(args[0]), Integer.parseInt(args[1])*/);
            ObjectInputStream oin = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream())){

            socket.setSoTimeout(TIMEOUT*1000);
            //currUser = new user("teste", "teste", "teste","teste");
            if(reqStr == null){
                return("Pedido nulo!");
            }
            if(reqStr.equalsIgnoreCase("SEND")){
                System.out.println("Codigo do evento:");
                Scanner sc = new Scanner(System.in);
                int code = sc.nextInt();
                req = new request(reqStr, currUser, String.valueOf(code));
            }else{
                req = new request(reqStr, currUser);

            }

            oout.writeObject(req);
            oout.flush();



            //Deserializa a resposta recebida em socket
            response = (String)oin.readObject();

            return Objects.requireNonNullElse(response, ("O servidor nao enviou qualquer respota antes de"
                    + " fechar aligacao TCP!"));

        }catch(Exception e){
            return("Ocorreu um erro no acesso ao socket:\n\t"+e);
        }
    }

    public static void register(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Nome:");
        String name = sc.nextLine();
        System.out.println("Número de estudante:");
        String NEstudante = sc.nextLine();
        System.out.println("Email:");
        String email = sc.nextLine();
        System.out.println("Password:");
        String password = sc.nextLine();
        currUser = new user(name, NEstudante, email, password);
        System.out.println("[Servidor]: "+sendRequest("REGISTER"));
    }

    public static void login() throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Email:");
        String email = sc.nextLine();
        System.out.println("Password:");
        String password = sc.nextLine();
        currUser = new user("", "", email, password);
        String rsp = sendRequest("LOGIN");
        if(rsp.equals("OK")) {
            System.out.println("Login efetuado com sucesso");
            while (appMenu()) {
                System.out.println("Pressione enter para continuar");
                System.in.read();
            }
        }else{
            System.out.println("[Servidor]: "+rsp);
        }

        //System.out.println("[Servidor]: "+sendRequest("LOGIN"));
    }

    public static boolean appMenu(){
        System.out.println("1 - Listar eventos a que estou inscrito");
        System.out.println("2 - Inscrever em evento");
        System.out.println("3 - Csv");
        System.out.println("4 - Alterar dados da minha conta");

        System.out.println("0 - Sair");

        Scanner sc = new Scanner(System.in);
        int op = sc.nextInt();
        switch (op){
            case 1:
                //listMyEvents();
                System.out.println(sendRequest("LIST"));
                return true;
                //break;
            case 2:
                subscribeEvent();
                return true;
            /*case 3:
                exportCSV();
                break;
            case 4:
                changeData();
                break;*/
            case 0:
                return false;
                //break;
            default:
                System.out.println("Opção inválida");
                return true;
                //break;
        }
        //return false;
    }

    private static void subscribeEvent() {
        //Scanner sc = new Scanner(System.in);
        //System.out.println("Codigo do evento:");
        //int code = sc.nextInt();
        System.out.println("[Servidor]: "+sendRequest("SEND"));
    }


    public static boolean logMenu() throws IOException {
        System.out.println("1 - Registar");
        System.out.println("2 - Login");
        System.out.println("0 - Sair");

        Scanner sc = new Scanner(System.in);
        int op = sc.nextInt();
        switch (op){
            case 1:
                register();
                break;
            case 2:
                login();
                break;
            case 0:
                return false;
            default:
                System.out.println("Opção inválida");
                return true;
        }
        return true;
    }



    public static final int TIMEOUT = 10; //segundos
    public static void main(String[] args) throws Exception {
        if(args.length != 2){
            System.out.println("Sintaxe: java client serverAddress serverUdpPort");
            return;
        }
        srvAdress = args[0];
        srvPort = Integer.parseInt(args[1]);

        try (Socket connectionTest = new Socket(srvAdress, srvPort)) {
        } catch (ConnectException e) {
            throw new Exception("Server nao esta a correr");
        } catch (IOException e) {
            throw new IllegalStateException("Erro a tentar procurar por servidor/porto", e);
        }


        while(logMenu()){
            System.out.println("Pressione enter para continuar");
            System.in.read();
        }

    }

}
