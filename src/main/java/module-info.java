module com.example.pd_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.rmi;


    opens pt.isec.pd to javafx.fxml;
    exports pt.isec.pd;
    exports pt.isec.pd.gui;
    opens pt.isec.pd.gui to javafx.fxml;
    exports pt.isec.pd.server;
    exports pt.isec.pd.client;

}