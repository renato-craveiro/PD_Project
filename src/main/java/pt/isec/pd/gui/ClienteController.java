package pt.isec.pd.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import pt.isec.pd.client.client;
import pt.isec.pd.server.request;
import pt.isec.pd.types.user;

import java.util.ArrayList;
import java.util.List;

public class ClienteController {

    @FXML
    private Label nameL, nestudentL, emailL, passL, fileNameL;
    @FXML
    private TextField  nameTF, nestudentTF, emailTF, passTF, submitCodeTF;
    @FXML
    private Button signBTN, loginBTN, listBTN, exportBTN, editBTN, codeBTN, submitSignBTN, submitLoginBTN, submitCodeBTN, submitExportBTN, submitEditBTN;
    @FXML
    private TableView<Event> tableTV;

    public String srvAddress;
    public int srvPort;

    public void setClienteSocket(String srvAddress, int srvPort) {
        this.srvAddress = srvAddress;
        this.srvPort = srvPort;
    }

    public void popUpMensage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void divideResponse(List<Event> events, String response){

        String[] entradas = response.split("\n");

        for (int i = 1; i<entradas.length; i++){

            String[] re = entradas[i].split(";");
            Event e = new Event(re[0],re[1],re[2],re[3]);
            events.add(e);


        }

    }


    @FXML
    protected void onSignButton() {

        // Hide Login and Sign IN
        loginBTN.setVisible(false);
        signBTN.setVisible(false);
        // Show sign in fields
        nameTF.setVisible(true);
        nestudentTF.setVisible(true);
        emailTF.setVisible(true);
        passTF.setVisible(true);
        nameL.setVisible(true);
        nestudentL.setVisible(true);
        emailL.setVisible(true);
        passL.setVisible(true);
        submitSignBTN.setVisible(true);

    }

    @FXML
    protected void onSignSubmit() {

        String name = nameTF.getText();
        String nestudent = nestudentTF.getText();
        String email = emailTF.getText();
        String password = passTF.getText();

        if (name.isEmpty() || nestudent.isEmpty() || email.isEmpty() || password.isEmpty()) {

            popUpMensage("Error", "Erro: Preencha todos os campos");
        } else {

            client.currUser = new pt.isec.pd.types.user(name, nestudent, email, password);
            String response = client.sendRequest("REGISTER","1");

            if (response.equals("User already exists")) {
                nameTF.clear();
                nestudentTF.clear();
                emailTF.clear();
                passTF.clear();

                popUpMensage("Error", "[Servidor]: " + response);

            }else {

                nameTF.clear();
                nestudentTF.clear();
                emailTF.clear();
                passTF.clear();

                nameTF.setVisible(false);
                nestudentTF.setVisible(false);
                emailTF.setVisible(false);
                passTF.setVisible(false);
                nameL.setVisible(false);
                nestudentL.setVisible(false);
                emailL.setVisible(false);
                passL.setVisible(false);
                submitSignBTN.setVisible(false);

                listBTN.setVisible(true);
                exportBTN.setVisible(true);
                editBTN.setVisible(true);
                codeBTN.setVisible(true);

                popUpMensage("Success", "[Servidor]: " + response);

            }
        }

    }

    @FXML
    protected void onLoginButton() {

        // Hide Login and Sign IN
        loginBTN.setVisible(false);
        signBTN.setVisible(false);
        // Show sign in fields
        emailTF.setVisible(true);
        passTF.setVisible(true);
        emailL.setVisible(true);
        passL.setVisible(true);
        submitLoginBTN.setVisible(true);

    }

    @FXML
    protected void onLoginSubmit() {

        // Get the email and password from the text fields
        String email = emailTF.getText();
        String password = passTF.getText();

        // Check if both email and password are not empty
        if (email.isEmpty() || password.isEmpty()) {
            // If either email or password is empty, show an error message
            popUpMensage("Error", "Erro ao fazer login: Preencha ambos os campos");
        } else {
            // If both email and password are not empty, proceed with the login operation
            client.currUser = new pt.isec.pd.types.user("", "", email, password);
            String response = client.sendRequest("LOGIN", "1");

            if (response.equals("OK")) {
                // If the login is successful, clear the text fields and adjust visibility
                emailTF.clear();
                passTF.clear();

                emailTF.setVisible(false);
                passTF.setVisible(false);
                emailL.setVisible(false);
                passL.setVisible(false);
                submitLoginBTN.setVisible(false);

                listBTN.setVisible(true);
                exportBTN.setVisible(true);
                editBTN.setVisible(true);
                codeBTN.setVisible(true);

                popUpMensage("Success", "Login efetuado com sucesso");
            } else {
                // If the login fails, clear the text fields and show an error message
                emailTF.clear();
                passTF.clear();

                popUpMensage("Error", "Credenciais erradas");
            }
        }
    }

    @FXML
    protected void onListButton() {

        tableTV.setVisible(true);
        tableTV.getItems().clear();

        String response = client.sendRequest("LIST", "1");
        List<Event> events = new ArrayList<>();

        divideResponse(events, response);

        for(Event event : events){
            tableTV.getItems().add(event);
        }


        listBTN.setVisible(true);
        exportBTN.setVisible(true);
        editBTN.setVisible(true);
        codeBTN.setVisible(true);

    }

    @FXML
    protected void onExportButton() {

        listBTN.setVisible(false);
        exportBTN.setVisible(false);
        editBTN.setVisible(false);
        codeBTN.setVisible(false);

        tableTV.setVisible(false);

        submitExportBTN.setVisible(true);
        fileNameL.setVisible(true);
        nameL.setVisible(true);
        nameTF.setVisible(true);

    }

    @FXML
    protected void onExportSubmit() {

        // Check if the code is not empty
        if (nameTF.getText().isEmpty()) {
            // If the code is empty, show an error message
            popUpMensage("Error", "Preencha o campo");
        } else {

            client.exportCSV(nameTF.getText());

            popUpMensage("Success", "CSV escrito com sucesso");

            nameTF.clear();

            submitExportBTN.setVisible(false);
            fileNameL.setVisible(false);
            nameL.setVisible(false);
            nameTF.setVisible(false);

            listBTN.setVisible(true);
            exportBTN.setVisible(true);
            editBTN.setVisible(true);
            codeBTN.setVisible(true);
        }

    }

    @FXML
    protected void onEditButton() {

        listBTN.setVisible(false);
        exportBTN.setVisible(false);
        editBTN.setVisible(false);
        codeBTN.setVisible(false);

        tableTV.setVisible(false);
        fileNameL.setVisible(false);

        nameTF.setVisible(true);
        nameL.setVisible(true);
        nestudentTF.setVisible(true);
        nestudentL.setVisible(true);
        emailTF.setVisible(true);
        passTF.setVisible(true);
        emailL.setVisible(true);
        passL.setVisible(true);
        submitEditBTN.setVisible(true);

    }

    @FXML
    protected void onEditSubmit() {

        // Check if any of the required fields is empty
        if (nameTF.getText().isEmpty() || nestudentTF.getText().isEmpty() || emailTF.getText().isEmpty() || passTF.getText().isEmpty()) {
            // If any field is empty, show an error message
            popUpMensage("Error", "Erro ao Editar: Preencha todos os campos");
        } else {

            client.auxUser = new user(nameTF.getText(), nestudentTF.getText(), emailTF.getText(), passTF.getText());

            // If all fields are filled, create a request to change user data
            request req = new request("CHANGE", client.currUser, client.auxUser);

            // Perform the user data change
            client.changeData(true);

            // Show other buttons
            listBTN.setVisible(true);
            exportBTN.setVisible(true);
            editBTN.setVisible(true);
            codeBTN.setVisible(true);

            // Hide the user information editing components
            nameTF.setVisible(false);
            nameL.setVisible(false);
            nestudentTF.setVisible(false);
            nestudentL.setVisible(false);
            emailTF.setVisible(false);
            passTF.setVisible(false);
            emailL.setVisible(false);
            passL.setVisible(false);
            submitEditBTN.setVisible(false);
        }

    }

    @FXML
    protected void onCodeButton() {

        listBTN.setVisible(false);
        exportBTN.setVisible(false);
        editBTN.setVisible(false);
        codeBTN.setVisible(false);

        tableTV.setVisible(false);
        fileNameL.setVisible(false);

        submitCodeTF.setVisible(true);
        submitCodeBTN.setVisible(true);

    }

    @FXML
    protected void onCodeSubmit() {
        // Get the code from the text field
        String code = submitCodeTF.getText();

        // Check if the code is not empty
        if (code.isEmpty()) {
            // If the code is empty, show an error message
            popUpMensage("Error", "Erro ao Inscrever: Código vazio");
        } else {
            // If the code is not empty, send the request to the server
            String response = client.sendRequest("SEND", code);

            // Check the response from the server
            if (response.contains("Inscrito")) {
                // If the response contains "Inscrito," show a success message
                popUpMensage("Success", "[Servidor]: " + response);
            } else {
                // If the response does not contain "Inscrito," show an error message
                popUpMensage("Error", "Erro ao Inscrever " + response);
            }

            // Clear the text field after submission
            submitCodeTF.clear();

            // Hide the submit code components
            submitCodeTF.setVisible(false);
            submitCodeBTN.setVisible(false);

            // Show other buttons
            listBTN.setVisible(true);
            exportBTN.setVisible(true);
            editBTN.setVisible(true);
            codeBTN.setVisible(true);
        }
    }

    @FXML
    protected void onExitButton() {

        System.exit(0);

    }

}
