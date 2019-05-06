import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class ChooseArmyUX {

    @FXML private Label turingsLabel;

    @FXML private VBox spinners;

    @FXML private Pane mainPane;

    private Map<Label,Spinner<Integer>> units;

    public void bindToTuringsLabel(StringProperty turings){
        turingsLabel.textProperty().bind(turings);
    }

    public void setStage(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Loader");
        Scene scene = new Scene(mainPane,500,100);
        primaryStage.setScene(scene);
    }

    public void setArmyUnits(Map<String,Integer> units){
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
    }

    public void launchLoader() throws Exception {
        Stage pStage = new Stage();
        setStage(pStage);
        pStage.show();
    }

}
