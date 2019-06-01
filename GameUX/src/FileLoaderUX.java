import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FileLoaderUX implements Initializable {

    @FXML private ProgressBar progressBar;
    @FXML private Button info;
    @FXML private BorderPane mPane;
    @FXML private Label messageLabel;

    Scene scene;
    Stage pStage;

    private FXMLLoader root;

    public FileLoaderUX() throws Exception {
        root = new FXMLLoader(getClass().getResource("loadFile.fxml"));
        root.setController(this);
        mPane = root.load();
        info.setOnAction(e->exitLoader());
        setSkin();
    }

    public void setStage(Stage primaryStage) throws IOException {
            primaryStage.setTitle("Loader");
            scene = new Scene(mPane,600,300);
            primaryStage.setScene(scene);
    }

    public void bindProgressBar(DoubleProperty prog){
        progressBar.progressProperty().bind(prog);
    }

    public void launchLoader() throws Exception {
        pStage = new Stage();
        setStage(pStage);
        pStage.show();
    }

    public void exitLoader() {
        pStage.close();
    }

    public void enableFinishLoader(){
        info.setDisable(false);
        info.setText("OK");
    }

    public void bindMessageLabel(StringProperty message){
        messageLabel.textProperty().bind(message);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private void setSkin(){
        String style = GameUX.resolveSkin(GameUX.getSkinColor());
        mPane.setId(style);
        Button[] buttons = {info};
        for(Button btn:buttons){
            btn.getStyleClass().remove(GameUX.resolveSkin(GameUX.getPrevSkinColor()));
            btn.getStyleClass().add(style);
        }
    }
}
