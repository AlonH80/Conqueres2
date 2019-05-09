import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ChooseArmyUX implements Initializable {

    @FXML
    private Pane mainPane;

    @FXML
    private Label turingsLabel;

    @FXML
    private VBox spinners;

    @FXML
    private Button confirmButton;

    private FXMLLoader root;
    private Map<Label,Spinner<Integer>> units;
    private Map<String,Integer> army = null;
    ChooseArmyNotifier notifier;
    Stage pStage;
    Scene scene;

    public ChooseArmyUX() throws Exception{
        root = new FXMLLoader(getClass().getResource("chooseArmy.fxml"));
        root.setController(this);
        mainPane = root.load();
    }

    public void setNotifier(Observer observer){
        notifier = new ChooseArmyNotifier(observer);
    }

    @FXML
    void confirmInput(ActionEvent event) {
        army = new HashMap<>();
        units.keySet().forEach(k->army.put(k.getText(),units.get(k).getValue()));
        pStage.close();
        notifier.notifyController("getArmy");
    }

    public void bindToTuringsLabel(IntegerProperty turings){
        turingsLabel.textProperty().bind((new SimpleStringProperty("Turings: ")).concat(turings.toString()));
    }

    public void setStage(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Loader");
        scene = new Scene(mainPane,700,400);
        primaryStage.setScene(scene);
    }

    public void setArmyUnitsSpinners(Map<String,Integer> units){
        this.units = new HashMap<>();
        units.keySet().forEach(k ->{
            Label label = new Label();
            label.textProperty().setValue(k);
            Spinner<Integer> spinner = new Spinner<>(0,units.get(k),0);
            this.units.put(label,spinner);
        });
        this.units.keySet().forEach(k->{
            HBox hBox = new HBox();
            hBox.getChildren().add(k);
            hBox.getChildren().add(this.units.get(k));
            spinners.getChildren().add(hBox);
        });
        spinners.getChildren().sorted();
    }

    public void launchLoader() throws Exception {
        pStage = new Stage();
        setStage(pStage);
        pStage.show();
    }

    public Map<String,Integer> getArmy(){
        return army;
    }

    public VBox getSpinners(){
        return spinners;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private class ChooseArmyNotifier extends Observable {

        public ChooseArmyNotifier(Observer observer){
            setObserver(observer);
        }

        public void setObserver(Observer observer){
            this.addObserver(observer);
        }

        public void notifyController(String mess){
            setChanged();
            notifyObservers(mess);
        }

    }
}
