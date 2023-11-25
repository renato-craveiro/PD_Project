package pt.isec.pd.server;

import pt.isec.pd.server.databaseManagement.DatabaseVersionControlManager;
import pt.isec.pd.server.databaseManagement.EventDatabaseManager;
import pt.isec.pd.server.databaseManagement.UserDatabaseManager;
import pt.isec.pd.types.event;
import pt.isec.pd.types.user;

import java.io.*;
import java.net.*;
import java.nio.file.AccessDeniedException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


class EventValidityChecker implements Runnable{
    eventManagement eventManager;

    public EventValidityChecker(eventManagement eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public void run() {
        while(true){
            eventManager.checkEventsValidity();
        }
    }
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

        request req;
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
                            && !req.req.equalsIgnoreCase("CHANGE")
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
                            break;
                        case "LOGOUT", "QUIT":
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
                                String response = header+sb+"\n";

                                oout.writeObject(response);
                                oout.flush();
                            }

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
                                    ev.addPresence(userManager.getUser(req.user.getEmail()));
                                    eventManager.editEvent(ev.getId(), ev);
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
                            }

                            break;
                        case "CHANGE":
                            if(userManager.checkUser(req.user)){
                                userManager.editUser(req.user.getEmail(),req.nUser);
                                String response = "ALTERADO";

                                oout.writeObject(response);
                                oout.flush();
                                System.out.println("USER "+userManager.getUser(req.user.getEmail()).getName()+" ALTERADO!");


                            }else {
                                userManager.createUser(req.user);
                                String response = "Falha na verificação do user!";

                                oout.writeObject(response);
                                oout.flush();
                            }
                            break;
                    }

                }



            } catch (Exception e) {
                System.out.println("Problema na comunicacao com o cliente " +
                        toClientSocket.getInetAddress().getHostAddress() + ":" +
                        toClientSocket.getPort() + "\n\t" + e);
            }



    }
}

class ManagerBackups extends UnicastRemoteObject implements ServerBackupServiceInterface, Runnable {

    public static final String SERVICE_NAME = "servidor-backup-database";
    public static final int MAX_CHUNCK_SIZE = 10000; //bytes
    protected File localDirectory;
    List<ServerBackupServiceInterface> backupServers;

    public ManagerBackups() throws RemoteException {
        backupServers = new ArrayList<>();
        this.localDirectory = new File("E:\\Estudo\\4ano\\PD\\TP\\PD_Project");
    }

    protected FileInputStream getRequestedFileInputStream(String fileName) throws IOException {
        String requestedCanonicalFilePath;

        fileName = fileName.trim();

        /*
         * Verifica se o ficheiro solicitado existe e encontra-se por baixo da localDirectory.
         */

        requestedCanonicalFilePath = new File(localDirectory+File.separator+fileName).getCanonicalPath();

        if(!requestedCanonicalFilePath.startsWith(localDirectory.getCanonicalPath()+File.separator)){
            System.out.println("Nao e' permitido aceder ao ficheiro " + requestedCanonicalFilePath + "!");
            System.out.println("A directoria de base nao corresponde a " + localDirectory.getCanonicalPath()+"!");
            throw new AccessDeniedException(fileName);
        }

        /*
         * Abre o ficheiro solicitado para leitura.
         */
        return new FileInputStream(requestedCanonicalFilePath);

    }

    @Override
    public byte [] getFileChunk(String fileName, long offset) throws RemoteException, IOException {
        fileName = "presences.db";
        byte [] fileChunk = new byte[MAX_CHUNCK_SIZE];
        int nbytes;

        fileName = fileName.trim();
        //System.out.println("Recebido pedido para: " + fileName);

        try(FileInputStream requestedFileInputStream = getRequestedFileInputStream(fileName)){

            /*
             * Obtem um bloco de bytes do ficheiro, omitindo os primeiros offset bytes.
             */
            requestedFileInputStream.skip(offset);
            nbytes = requestedFileInputStream.read(fileChunk);

            if(nbytes == -1){//EOF
                return null;
            }

            /*
             * Se fileChunk nao esta' totalmente preenchido (MAX_CHUNCK_SIZE), recorre-se
             * a um array auxiliar com tamanho correspondente ao numero de bytes efectivamente lidos.
             */
            if(nbytes < fileChunk.length){
                return Arrays.copyOf(fileChunk, nbytes);
            }

            return fileChunk;

        }catch(IOException e){
            System.out.println("Ocorreu a excecao de E/S: \n\t" + e);
            throw new IOException(fileName, e.getCause());
        }

    }

    public void getFile(String fileName, ServerBackupInterface cliRemoto) throws IOException {
        byte [] fileChunk = new byte[MAX_CHUNCK_SIZE];
        int nbytes;

        fileName = fileName.trim();
        System.out.println("Recebido pedido para: " + fileName);

        try(FileInputStream requestedFileInputStream = getRequestedFileInputStream(fileName)){

            /*
             * Obtem os bytes do ficheiro por blocos de bytes ("file chunks").
             */
            while((nbytes = requestedFileInputStream.read(fileChunk))!=-1){

                /*
                 * Escreve o bloco actual no cliente, invocando o metodo writeFileChunk da
                 * sua interface remota.
                 */

                cliRemoto.writeFileChunk(fileChunk,nbytes);

                /*...*/

            }

            System.out.println("Ficheiro " + new File(localDirectory+File.separator+fileName).getCanonicalPath() + " transferido para o cliente com sucesso.");
            System.out.println();

            return;

        }catch(FileNotFoundException e){   //Subclasse de IOException
            System.out.println("Ocorreu a excecao {" + e + "} ao tentar abrir o ficheiro!");
            throw new FileNotFoundException(fileName);
        }catch(IOException e){
            System.out.println("Ocorreu a excecao de E/S: \n\t" + e);
            throw new IOException(fileName, e.getCause());
        }

    }

    @Override
    public void addBackup(ServerBackupServiceInterface sBackup) throws RemoteException {
        synchronized (backupServers){
            if(!backupServers.contains(sBackup)){
                backupServers.add(sBackup);
                System.out.println("\n<SERVER BACKUP added>\n");
            }
        }
    }

    @Override
    public void removeBackup(ServerBackupServiceInterface sBackup) throws RemoteException {
        synchronized (backupServers){
            if(!backupServers.contains(sBackup)){
                backupServers.remove(sBackup);
                System.out.println("\n<SERVER BACKUP removed>\n");
            }
        }
    }

    @Override
    public void run(){
        File localDirectory;

        localDirectory = new File("E:\\Estudo\\4ano\\PD\\TP\\PD_Project");

        if(!localDirectory.exists()){
            System.out.println("A directoria " + localDirectory + " nao existe!");
            return;
        }

        if(!localDirectory.isDirectory()){
            System.out.println("O caminho " + localDirectory + " nao se refere a uma diretoria!");
            return;
        }

        if(!localDirectory.canRead()){
            System.out.println("Sem permissoes de leitura na diretoria " + localDirectory + "!");
            return;
        }


        /*
         * Lanca o rmiregistry localmente no porto TCP por omissao (1099).
         */
        try{

            try{
                System.out.println("<SERVER BACKUP> Tentativa de lancamento do registry no porto " + Registry.REGISTRY_PORT + "...");
                LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
                System.out.println("<SERVER BACKUP> Registry lancado!");
            }catch(RemoteException e){
                System.out.println("Registry provavelmente ja' em execucao!");
            }

            /*
             * Cria o servico.
             */
            ManagerBackups fileService = new ManagerBackups();

            System.out.println("<SERVER BACKUP> Servico GetRemoteFile criado e em execucao ("+fileService.getRef().remoteToString()+"...");

            /*
             * Regista o servico no rmiregistry local para que os clientes possam localiza'-lo, ou seja,
             * obter a sua referencia remota (endereco IP, porto de escuta, etc.).
             */

            Naming.bind("rmi://localhost/" + SERVICE_NAME, fileService);
            System.out.println("<SERVER BACKUP> Servico " + SERVICE_NAME + " registado no registry...");

        }catch(RemoteException e){
            System.out.println("Erro remoto - " + e);
            System.exit(1);
        }catch(Exception e){
            System.out.println("Erro - " + e);
            System.exit(1);
        }
    }


}


class KBMgmt implements Runnable{
    boolean adminLogged;
    eventManagement eventManager;
    userManagment userManager;
    DatabaseVersionControlManager versionManager;

    public KBMgmt(boolean adminLogged, eventManagement eventManager, userManagment userManager, DatabaseVersionControlManager versionManager) {
        this.adminLogged = adminLogged;
        this.eventManager = eventManager;
        this.userManager = userManager;
        this.versionManager = versionManager;
    }


    private void createEvent() {

        boolean sucsess = false;

        Date dateTime = null, startHourTime = null, endHourTime = null;
        Scanner sc = new Scanner(System.in);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat hourFormat = new SimpleDateFormat("hh:mm");


        System.out.println("Nome do evento:");
        String name = sc.nextLine();

        System.out.println("Local do evento:");
        String local = sc.nextLine();



        while (!sucsess){

            System.out.println("Data do evento (dd/mm/aaaa):");
            String date = sc.nextLine();

            try {
                dateTime = dateFormat.parse(date);
                sucsess = true;
            } catch (ParseException e) {
                System.out.println("Formato de data/hora inválido. Usa dd/mm/aaaa.");
            }

        }
        sucsess = false;
        while (!sucsess){
            System.out.println("Hora de inicio do evento (hh:mm):");
            String start = sc.nextLine();
            try {
                startHourTime = hourFormat.parse(start);
                sucsess = true;
            } catch (ParseException e) {
                System.out.println("Formato de hora inválido. Usa hh:mm.");
            }
        }
        sucsess = false;
        while (!sucsess){
            System.out.println("Hora de fim do evento (hh:mm):");
            String end = sc.nextLine();
            try {
                endHourTime = hourFormat.parse(end);
                sucsess = true;
            } catch (ParseException e) {
                System.out.println("Formato de hora inválido. Usa hh:mm.");
            }
        }

        Calendar eventDate = Calendar.getInstance();
        eventDate.setTime(dateTime);

        Calendar eventStart = Calendar.getInstance();
        eventStart.set(Calendar.HOUR_OF_DAY,startHourTime.getHours());
        eventStart.set(Calendar.MINUTE,startHourTime.getMinutes());

        Calendar eventEnd = Calendar.getInstance();
        eventEnd.set(Calendar.HOUR_OF_DAY,endHourTime.getHours());
        eventEnd.set(Calendar.MINUTE,endHourTime.getMinutes());

        eventManager.createEvent(name, local, eventDate, eventStart, eventEnd);
        eventManager.getEvents().get(eventManager.getEvents().size()-1).generateRandomCode();
        System.out.println("Codigo do evento: "+eventManager.getEvents().get(eventManager.getEvents().size()-1).getCode());
        versionManager.updateVersion();
    }

    public void editEvent(){
        event newEv;
        Scanner sc = new Scanner(System.in);
        System.out.println("ID do evento:");
        String strID1 = sc.nextLine();
        int id1 = Integer.parseInt(strID1);
        System.out.println("Dados atuais do evento: \n"+eventManager.getEventById(id1).toString());
        System.out.println("Novo nome do evento:");
        String name = sc.nextLine();
        System.out.println("Novo local do evento:");
        String local = sc.nextLine();
        if(!eventManager.getEventById(id1).getUsersPresent().isEmpty()){
            System.out.println("O evento ja tem presencas registadas. Não pode alterar a data do evento!");
             newEv = new event(name, local,
                    eventManager.getEventById(id1).getDate(),
                    eventManager.getEventById(id1).getStart(),
                    eventManager.getEventById(id1).getEnd());
        }else {
            boolean sucsess = false;
            Date dateTime = null, startHourTime = null, endHourTime = null;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat hourFormat = new SimpleDateFormat("hh:mm");
            while (!sucsess){

                System.out.println("Data do evento (dd/mm/aaaa):");
                String date = sc.nextLine();

                try {
                    dateTime = dateFormat.parse(date);
                    sucsess = true;
                } catch (ParseException e) {
                    System.out.println("Formato de data/hora inválido. Usa dd/mm/aaaa.");
                }

            }
            sucsess = false;
            while (!sucsess){
                System.out.println("Hora de inicio do evento (hh:mm):");
                String start = sc.nextLine();
                try {
                    startHourTime = hourFormat.parse(start);
                    sucsess = true;
                } catch (ParseException e) {
                    System.out.println("Formato de hora inválido. Usa hh:mm.");
                }
            }
            sucsess = false;
            while (!sucsess){
                System.out.println("Hora de fim do evento (hh:mm):");
                String end = sc.nextLine();
                try {
                    endHourTime = hourFormat.parse(end);
                    sucsess = true;
                } catch (ParseException e) {
                    System.out.println("Formato de hora inválido. Usa hh:mm.");
                }
            }
            Calendar eventDate = Calendar.getInstance();
            eventDate.setTime(dateTime);

            Calendar eventStart = Calendar.getInstance();
            eventStart.set(Calendar.HOUR_OF_DAY,startHourTime.getHours());
            eventStart.set(Calendar.MINUTE,startHourTime.getMinutes());

            Calendar eventEnd = Calendar.getInstance();
            eventEnd.set(Calendar.HOUR_OF_DAY,endHourTime.getHours());
            eventEnd.set(Calendar.MINUTE,endHourTime.getMinutes());

            newEv = new event(name, local, eventDate, eventStart, eventEnd);
        }
        eventManager.editEvent(id1, newEv);
    }

    public void consultaEventoFiltrado(){
        System.out.println("1-Data\n2-Nome\n3-Local\n4-Periodo\n");
        Scanner sc = new Scanner(System.in);
        String buffer = sc.nextLine();
        switch (buffer) {
            case "1" -> {
                System.out.println("Data do evento (dd/mm/aaaa):");
                String date = sc.nextLine();
                Calendar dateTime = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    dateTime.setTime(dateFormat.parse(date));
                } catch (ParseException e) {
                    System.out.println("Formato de data/hora inválido. Usa dd/mm/aaaa.");
                }
                for (event e : eventManager.getEvents()) {
                    if (e.getDate().equals(dateTime)) {
                        System.out.println(e.toString());
                    }
                }
            }
            case "2" -> {
                System.out.println("Nome do evento:");
                String name = sc.nextLine();
                for (event e : eventManager.getEvents()) {
                    if (e.getName().equals(name)) {
                        System.out.println(e.toString());
                    }
                }
            }
            case "3" -> {
                System.out.println("Local do evento:");
                String local = sc.nextLine();
                for (event e : eventManager.getEvents()) {
                    if (e.getLocal().equals(local)) {
                        System.out.println(e.toString());
                    }
                }
            }
            case "4" -> {
                System.out.println("Data de inicio do periodo (dd/mm/aaaa):");
                String date1 = sc.nextLine();
                Calendar dateTime1 = Calendar.getInstance();
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    dateTime1.setTime(dateFormat1.parse(date1));
                } catch (ParseException e) {
                    System.out.println("Formato de data/hora inválido. Usa dd/mm/aaaa.");
                    break;
                }
                System.out.println("Data de fim do periodo (dd/mm/aaaa):");
                String date2 = sc.nextLine();
                Calendar dateTime2 = Calendar.getInstance();
                SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    dateTime2.setTime(dateFormat2.parse(date2));
                } catch (ParseException e) {
                    System.out.println("Formato de data/hora inválido. Usa dd/mm/aaaa.");
                    break;
                }
                for (event e : eventManager.getEvents()) {
                    if (e.getDate().after(dateTime1) && e.getDate().before(dateTime2)) {
                        System.out.println(e.toString());
                    }
                }
            }
            default -> System.out.println("Opção inválida");
        }

    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        String buffer;
        while(true){
            //Just to see the DatabaseVersion
            System.out.println("DataBase version: " + versionManager.getCurrentVersion());
            System.out.println("Menu:\n\n");
            if(adminLogged){
                System.out.println(
                        "2-criar evento\n" +
                        "3-listar eventos\n" +
                        "31-listar eventos filtrado\n"+
                        "4-remover evento\n" +
                        "5-editar evento\n" +
                        "6-gerar código evento\n" +
                        "7-consultar presenças\n" +
                        "71-consultar presenças de um utilizador\n"+
                        "8-Obter ficheiro csv\n" +
                        "9-eliminar presença\n" +
                        "10-inserir presença\n" +
                        "11-logout\n"
                );
            }else {
                System.out.println("1-admin login");
            }

            System.out.println("Escreva \"exit\" para terminar o servidor");
            buffer = sc.nextLine();
            switch (buffer){
                case "1": //Login
                    if(!userManager.checkUser("admin")){
                        System.out.println("Admin nao existe... Crie um admin primeiro!");
                        break;
                    }
                    System.out.println("Email:");
                    String email = sc.nextLine();
                    System.out.println("Password:");
                    String password = sc.nextLine();


                    if(userManager.getUser("admin").getEmail().equals(email) && userManager.getUser("admin").getPassword().equals( password)) {
                        adminLogged = true;
                        System.out.println("Login efetuado com sucesso. Seja bem vindo " + userManager.getUser("admin").getName() +"!");
                    }
                    else{
                        System.out.println("Credenciais incorretas!");
                    }
                    break;
                    case "2": //Create event
                        if(adminLogged) {
                            createEvent();
                            break;
                        }
                    case "3": //List events
                        if(adminLogged) {
                            for (event e : eventManager.getEvents()) {
                                System.out.println(e.toString());
                            }
                            break;
                        }
                    case "31": //List filtered events
                        if(adminLogged){
                            consultaEventoFiltrado();
                            break;
                        }
                    case "4": //Remove event
                        if(adminLogged) {
                            int id;
                            System.out.println("ID do evento:");
                            String strID = sc.nextLine();
                            id = Integer.parseInt(strID);
                            if (eventManager.removeEvent(id)) {
                                versionManager.updateVersion();
                                System.out.println("Evento removido com sucesso!");
                            } else {
                                System.out.println("Evento nao encontrado!");
                            }
                            break;
                        }
                    case "5": //Edit event
                        if(adminLogged) {
                            editEvent();
                            break;
                        }
                    case "6": //Generate code
                        if(adminLogged) {
                            System.out.println("ID do evento:");
                            String strID2 = sc.nextLine();
                            int id2 = Integer.parseInt(strID2);
                            System.out.println("Insira a validade do codigo (em minutos):");
                            String strVal = sc.nextLine();
                            int time = Integer.parseInt(strVal);

                            eventManager.getEventById(id2).generateRandomCodeWithValidity(time);
                            System.out.println("Codigo do evento: " + eventManager.getEventById(id2).getCode());
                            eventManager.updateEventDB(id2);
                            break;
                        }
                    case "7": //Check attendance
                        if(adminLogged) {
                            System.out.println("ID do evento:");
                            String strID3 = sc.nextLine();
                            int id3 = Integer.parseInt(strID3);
                            System.out.println("Presenças no evento" + eventManager.getEventById(id3).getName() + ":");
                            for (user u : eventManager.getEventById(id3).getUsersPresent()) {
                                System.out.println(u.toString());
                            }
                            break;
                        }
                    case "71": //Check User attendance
                        if(adminLogged) {
                            System.out.println("Email do utilizador:");
                            String email1 = sc.nextLine();
                            System.out.println("Presenças do utilizador " + email1 + ":");
                            for (event e : eventManager.getEvents()) {
                                if (e.checkPresenceEmail(email1)) {
                                    System.out.println(e.toString());
                                }
                            }
                            break;
                        }
                    case "8": //Generate CSV
                        //csv
                        break;
                    case "9": //Eliminate attendance
                        if(adminLogged) {
                            System.out.println("ID do evento:");
                            String code4 = sc.nextLine();
                            int id4 = Integer.parseInt(code4);
                            System.out.println("Email do utilizador:");
                            String email2 = sc.nextLine();
                            eventManager.getEventById(id4).removePresence(userManager.getUser(email2));
                            eventManager.removeUserEvent(userManager.getUser(email2),eventManager.getEventById(id4));
                            eventManager.updateEventDB(id4);
                            versionManager.updateVersion();
                            break;
                        }
                    case "10": //Add attendance
                        if(adminLogged) {
                            System.out.println("ID do evento:");
                            String code5 = sc.nextLine();
                            int id5 = Integer.parseInt(code5);
                            System.out.println("Email do utilizador:");
                            String email3 = sc.nextLine();
                            eventManager.getEventById(id5).addPresence(userManager.getUser(email3));
                            eventManager.updateEventDB(id5);
                            versionManager.updateVersion();
                            break;
                        }
                    case "11": //Logout
                        if(adminLogged) {
                            adminLogged = false;
                            break;
                        }

                    case "debug":
                        System.out.println("Utilizadores:");
                        for (user u : userManager.users) {
                            System.out.println(u.toString());
                        }
                        System.out.println("Eventos:");
                        for (event e : eventManager.getEvents()) {
                            System.out.println(e.toString());
                        }
                        break;

                case "exit":
                    System.exit(0);
                    break;
                default:
                    System.out.println("Opção inválida");
                    break;
            }

            System.out.println("Pressione enter para continuar");
            try {
                System.in.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}


public class server {
    public static final String SQLITEDB ="presences";

    public static void main(String args[]) {

        String DB_PATH = SQLITEDB;

        if(args.length != 2 /*DEVE SER 3 POR CAUSA DO RMI*/){
            System.out.println("Sintaxe: java pt.isec.pd.server porto caminho_baseDados ");
            return;
        }

        //Reading args Server port and DataBase Path
        int port = Integer.parseInt(args[0]);
        DB_PATH = args[1];

        try (ServerSocket socket = new ServerSocket(/*Integer.parseInt(args[0])) //5000)*/port)) {
            int nCreatedThreads = 0;

            //Creates the DataBase "Controllers"
            userManagment userManager = new userManagment(new UserDatabaseManager(DB_PATH));
            eventManagement eventManager = new eventManagement(new EventDatabaseManager(DB_PATH));
            DatabaseVersionControlManager versionManager = new DatabaseVersionControlManager(DB_PATH);
            userManager.createAdminIfNotExists();

            //Just to see the DatabaseVersion
            System.out.println("DataBase version: " + versionManager.getCurrentVersion());

            //Creates Thread
            Thread thr;
            Thread eventValidityChecker = new Thread(new EventValidityChecker(eventManager));
            eventValidityChecker.start();
            Thread kb = new Thread(new KBMgmt(false, eventManager, userManager, versionManager));
            kb.start();

            Thread mb = new Thread((Runnable) new ManagerBackups(), "Thread_" + 1);
            mb.start();

            Thread hb = new Thread(new HeartbeatSender(versionManager));
            hb.start();


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
