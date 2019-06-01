import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ChooseArmyUX implements Initializable {

    @FXML private ScrollPane mainPane;
    @FXML private Label turingsLabel;
    @FXML private VBox spinners;
    @FXML private VBox spinnersLabels;
    @FXML private Button confirmButton;
    @FXML private RadioButton deterministAttack;
    @FXML private RadioButton lotteryAttack;
    @FXML private VBox chooseAttackBox;

    private FXMLLoader root;
    private Map<Label,Spinner<Integer>> units;
    private Map<String,Integer> army = null;
    ChooseArmyNotifier notifier;
    private Integer totalTurings;
    Stage pStage;
    Scene scene;

    public ChooseArmyUX() throws Exception{
        root = new FXMLLoader(getClass().getResource("chooseArmy.fxml"));
        root.setController(this);
        mainPane = root.load();
        totalTurings = 0;
    }

    public void setNotifier(Observer observer){
        notifier = new ChooseArmyNotifier(observer);
    }

    @FXML
    void confirmInput(ActionEvent event) {
        army = new HashMap<>();
        units.keySet().forEach(k->army.put(k.getText(),units.get(k).getValue()));
        pStage.close();
        if (deterministAttack.selectedProperty().getValue() == true){
            notifier.notifyController("getArmy determinist");
        }
        else {
            notifier.notifyController("getArmy lottery");
        }
    }

    public void bindToTuringsLabel(IntegerBinding turings){
        turingsLabel.textProperty().bind((new SimpleStringProperty("Turings: ")).concat(turings));
    }

    public void setStage(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Loader");
        scene = new Scene(mainPane,700,400);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                notifier.notifyController("noActionOnChooseWindow");
                pStage.close();
                event.consume();
            }
        });
        primaryStage.setScene(scene);
        setSkin();
    }

    public void setArmyUnitsSpinners(Map<String,Integer> units){
        this.units = new HashMap<>();
        units.keySet().forEach(k ->{
            Label label = new Label();
            label.setPrefHeight(25);
            label.textProperty().setValue(k);
            label.setAlignment(Pos.CENTER_LEFT);
            label.setWrapText(true);
            Spinner<Integer> spinner = new Spinner<>(0,units.get(k),0);

            spinner.valueProperty().addListener((obs, oldValue, newValue) ->
                    updateTuringsLabel(oldValue, newValue, units.get(k)));
            this.units.put(label,spinner);
        });
        this.units.keySet().forEach(k->{
            spinners.getChildren().add(this.units.get(k));
            spinnersLabels.getChildren().add(k);
        });
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

    private void updateTuringsLabel(Integer oldValue, Integer newValue, Integer purch){
        totalTurings += (newValue - oldValue)*purch;
        turingsLabel.textProperty().setValue("Total turings: "+totalTurings.toString());
    }

    public void disableChooseAttackBox(){
        chooseAttackBox.disableProperty().setValue(true);
        chooseAttackBox.setOpacity(0);
    }

    public void enableChooseAttackBox(){
        chooseAttackBox.disableProperty().setValue(false);
        chooseAttackBox.setOpacity(1);
    }

    private void setSkin(){
        String style = GameUX.resolveSkin(GameUX.getSkinColor());
        mainPane.setId(style);
        Button[] buttons = {confirmButton};
        for(Button btn:buttons){
            btn.getStyleClass().remove(GameUX.resolveSkin(GameUX.getPrevSkinColor()));
            btn.getStyleClass().add(style);
        }
    }

    public Integer getTotalTurings(){
        return totalTurings;
    }
}
