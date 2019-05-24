import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PopMessageUX implements Initializable {

    @FXML private Button confirmButton;
    @FXML private Label messageLabel;
    @FXML private Pane mPane;

    private Scene scene;
    private Stage pStage;

    private FXMLLoader root;

    public PopMessageUX() throws Exception {
        root = new FXMLLoader(getClass().getResource("popMessage.fxml"));
        root.setController(this);
        mPane = root.load();
        //confirmButton.setOnAction(e->exitLoader());
    }

    @FXML
    void confirmInput(ActionEvent event) {
        pStage.close();
    }

    public void setStage(Stage primaryStage) throws IOException {
        primaryStage.setTitle("");
        scene = new Scene(mPane,600,300);
        primaryStage.setScene(scene);
    }

    public void setMessageLabel(String mess){
        messageLabel.textProperty().setValue(mess);
    }

    public void launchWindow() throws Exception {
        pStage = new Stage();
        setStage(pStage);
        pStage.show();
    }

    public void exitLoader() {
        /*try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e){}*/
        pStage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
