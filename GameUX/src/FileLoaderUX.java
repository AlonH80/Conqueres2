import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class FileLoaderUX {

    @FXML private ProgressBar progressBar;

    @FXML private Label info;

    @FXML private BorderPane mainPane;

    private FXMLLoader root;

    public FileLoaderUX() throws Exception {
        root = new FXMLLoader(getClass().getResource("loadFile.fxml"));
        root.setController(this);
        mainPane = root.load();
    }

    public void setStage(Stage primaryStage) throws IOException {
            primaryStage.setTitle("Loader");
            Scene scene = new Scene(mainPane,200,100);
            primaryStage.setScene(scene);
    }

    public void bindProgressBar(DoubleProperty prog){
        progressBar.progressProperty().bind(prog);
    }

    public void launchLoader() throws Exception {
        Stage pStage = new Stage();
        setStage(pStage);
        pStage.show();
    }
}
