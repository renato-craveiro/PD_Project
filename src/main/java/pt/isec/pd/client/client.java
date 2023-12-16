package pt.isec.pd.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pt.isec.pd.gui.ClienteController;
import pt.isec.pd.server.request;
import pt.isec.pd.types.user;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.Objects;
import java.util.Scanner;


public class client extends Application {
    public static user currUser;
    public static user auxUser;
    static String srvAdress;
    static int srvPort;

    static Socket socket;
    public static String sendRequest(String reqStr, String receivedCode){ //if receivedCode=0 its because its graphip else its reading from cmd
        request req;// = new request();

        String response;
        try(Socket socket = new Socket(InetAddress.getByName(srvAdress),srvPort/*InetAddress.getByName(args[0]), Integer.parseInt(args[1])*/);
            ObjectInputStream oin = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oout = new ObjectOutputStream(socket.getOutputStream())){

            System.out.println("OOOOOOO CARL");
            String helloUri = "http://10.65.136.232:8080/login";
            String str="admin:admin";
            String token = sendRequestAndShowResponse(helloUri, "POST", "basic "+ Base64.getEncoder().encodeToString(str.getBytes()));
            System.out.println(token);
            String helloUri2 = "http://10.65.136.232:8080/usercalls/createdEvents";

            sendRequestAndShowResponse(helloUri2, "GET", "bearer "+token);
            socket.setSoTimeout(TIMEOUT*1000);
            if(reqStr == null){
                return("Pedido nulo!");
            }
            if(reqStr.equalsIgnoreCase("SEND")){
                if (!receivedCode.equals("0"))
                    req = new request(reqStr, currUser, receivedCode);
                else {
                   // System.out.println("Aqui???");

                    System.out.println("Codigo do evento:");
                    Scanner sc = new Scanner(System.in);
                    int code = sc.nextInt();

                    req = new request(reqStr, currUser, String.valueOf(code));
                }
            }else{
                req = new request(reqStr, currUser);

            }
            if (reqStr.equalsIgnoreCase("CHANGE")){
                req = new request(reqStr, currUser, auxUser);
            }
            oout.writeObject(req);
            oout.flush();

            //Deserializa a resposta recebida em socket
            response = (String)oin.readObject();

            if(response.contains("ALTERADO")){
                currUser = auxUser;
            }

            return Objects.requireNonNullElse(response, ("O servidor nao enviou qualquer respota antes de" + " fechar a ligacao TCP!"));

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
        user auxUser = new user(name, NEstudante, email, password);
        currUser = new user(name, NEstudante, email, password);
        System.out.println("[Servidor]: "+sendRequest("REGISTER","0"));
    }

    public static void changeData(boolean grafic){
        if (!grafic) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Nome:");
            String name = sc.nextLine();
            System.out.println("Número de estudante:");
            String NEstudante = sc.nextLine();
            System.out.println("Email:");
            String email = sc.nextLine();
            System.out.println("Password:");
            String password = sc.nextLine();
            auxUser = new user(name, NEstudante, email, password);
        }

        System.out.println("[Servidor]: "+sendRequest("CHANGE","0"));
    }

    public static void login() throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Email:");
        String email = sc.nextLine();
        System.out.println("Password:");
        String password = sc.nextLine();
        currUser = new user("", "", email, password);
        String rsp = sendRequest("LOGIN","0");

        if(rsp.equals("OK")) {
            System.out.println("Login efetuado com sucesso");
            while (appMenu()) {
                System.out.println("Pressione enter para continuar");
                System.in.read();
            }
        }else{
            System.out.println("[Servidor]: "+rsp);
        }

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

                System.out.println(sendRequest("LIST","0"));
                return true;
            case 2:
                subscribeEvent();
                return true;
            case 3:
                exportCSV("cmd");
                return true;
            case 4:
                changeData(false);
                System.out.println("Efetue o seu login novamente");
                return false;

            case 0:
                return false;
                //break;
            default:
                System.out.println("Opção inválida");
                return true;

        }

    }

    public static void exportCSV(String fileNameG) {
        try {
            PrintWriter csvWriter;
            String fileName = null;

            if (fileNameG.equals("cmd")) {
                Scanner sc = new Scanner(System.in);
                System.out.println("Enter CSV file name:");
                fileName = sc.nextLine();
                csvWriter = new PrintWriter(new FileWriter(fileName + ".csv"));
            }else
                csvWriter = new PrintWriter(new FileWriter(fileNameG + ".csv"));


            String response = sendRequest("LIST","0");

            if (!response.isEmpty()) {

                csvWriter.println(response);
                if (fileNameG.equals("0"))
                    System.out.println("CSV export successful. Data written to " + fileName);
                else
                    System.out.println("CSV export successful. Data written to " + fileNameG);

            } else {
                System.out.println("Error exporting CSV: " + response);
            }

            csvWriter.close();
        } catch (Exception e) {
            System.out.println("Error exporting CSV: " + e.getMessage());
        }
    }

    private static void subscribeEvent() {
        //Scanner sc = new Scanner(System.in);
        //System.out.println("Codigo do evento:");
        //int code = sc.nextInt();
        System.out.println("[Servidor]: "+sendRequest("SEND","0"));
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

         /*   while(logMenu()){
                System.out.println("Pressione enter para continuar");
                System.in.read();
            }
            */launch(args);
        } catch (ConnectException e) {
            throw new Exception("Server nao esta a correr");
        } catch (IOException e) {
            throw new IllegalStateException("Erro a tentar procurar por servidor/porto", e);
        }


    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carrega o arquivo FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pt/isec/pd/client.fxml"));
        Parent root = loader.load();

        // Obtém o controlador do FXML
        ClienteController controlador = loader.getController();

        // Configura a cena e exibe a janela
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Cliente JavaFX");
        primaryStage.show();

        // Configura a lógica do cliente no controlador
        controlador.setClienteSocket(srvAdress, srvPort);
    }

















    public static String sendRequestAndShowResponse(String uri, String verb, String authorizationValue) throws MalformedURLException, IOException {

        String responseBody = null;
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(verb);
        connection.setRequestProperty("Accept", "application/xml, */*");

        if(authorizationValue!=null) {
            connection.setRequestProperty("Authorization", authorizationValue);
        }

        connection.connect();

        int responseCode = connection.getResponseCode();
        System.out.println("Response code: " +  responseCode + " (" + connection.getResponseMessage() + ")");

        Scanner s;

        if(connection.getErrorStream()!=null) {
            s = new Scanner(connection.getErrorStream()).useDelimiter("\\A");
            responseBody = s.hasNext() ? s.next() : null;
        }

        try {
            s = new Scanner(connection.getInputStream()).useDelimiter("\\A");
            responseBody = s.hasNext() ? s.next() : null;
        } catch (IOException e){}

        connection.disconnect();

        System.out.println(verb + " " + uri + " -> " + responseBody);
        System.out.println();

        return responseBody;
    }


}
