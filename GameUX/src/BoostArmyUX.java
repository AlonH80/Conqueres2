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

public class BoostArmyUX implements Initializable {

    @FXML private Pane mPane;
    @FXML private Label turingsLabel;
    @FXML private VBox recoverVBox;
    @FXML private VBox newUnitsVBox;
    @FXML private Button confirmButton;

    private FXMLLoader root;
    private Map<Label,Spinner<Integer>> unitsBoost;
    private Map<Label,Spinner<Integer>> unitsNew;
    private Map<String,Integer> armyBoost = null;
    private Map<String,Integer> armyNew = null;
    private BoostArmyNotifier notifier;
    private Stage pStage;
    private Scene scene;

    public BoostArmyUX() throws Exception{
        root = new FXMLLoader(getClass().getResource("boostArmy.fxml"));
        root.setController(this);
        mPane = root.load();
    }

    public void setNotifier(Observer observer){
        notifier = new BoostArmyNotifier(observer);
    }

    @FXML
    void confirmInput(ActionEvent event) {
        armyBoost = new HashMap<>();
        armyNew = new HashMap<>();
        unitsBoost.keySet().forEach(k->armyBoost.put(k.getText(),unitsBoost.get(k).getValue()));
        unitsNew.keySet().forEach(k->armyNew.put(k.getText(),unitsNew.get(k).getValue()));
        pStage.close();
        notifier.notifyController("getBoostArmy");
    }

    public void bindToTuringsLabel(IntegerProperty turings){
        turingsLabel.textProperty().bind((new SimpleStringProperty("Turings: ")).concat(turings.toString()));
    }

    public void setStage(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Boost army on territory");
        scene = new Scene(mPane,700,400);
        primaryStage.setScene(scene);
    }

    public void setRecoverSpinners(Map<String,Integer> units){
        this.unitsBoost = new HashMap<>();
        units.keySet().forEach(k ->{
            Label label = new Label();
            label.textProperty().setValue(k);
            Spinner<Integer> spinner = new Spinner<>(0,units.get(k),0);
            this.unitsBoost.put(label,spinner);
        });
        this.unitsBoost.keySet().forEach(k->{
            HBox hBox = new HBox();
            hBox.getChildren().add(k);
            hBox.getChildren().add(this.unitsBoost.get(k));
            recoverVBox.getChildren().add(hBox);
        });
    }

    public void setNewUnitsSpinners(Map<String,Integer> units){
        this.unitsNew = new HashMap<>();
        units.keySet().forEach(k ->{
            Label label = new Label();
            label.textProperty().setValue(k);
            Spinner<Integer> spinner = new Spinner<>(0,units.get(k),0);
            this.unitsNew.put(label,spinner);
        });
        this.unitsNew.keySet().forEach(k->{
            HBox hBox = new HBox();
            hBox.getChildren().add(k);
            hBox.getChildren().add(this.unitsNew.get(k));
            newUnitsVBox.getChildren().add(hBox);
        });
    }

    public void launchLoader() throws Exception {
        pStage = new Stage();
        setStage(pStage);
        pStage.show();
    }

    public Map<String,Integer> getArmyNew(){
        return armyNew;
    }

    public Map<String,Integer> getArmyBoost(){
        return armyBoost;
    }

    public VBox getRecoverVBox(){
        return recoverVBox;
    }

    public VBox getNewUnitsVBoxVBox(){
        return newUnitsVBox;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    private class BoostArmyNotifier extends Observable {

        public BoostArmyNotifier(Observer observer){
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
