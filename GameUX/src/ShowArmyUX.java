import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;

public class ShowArmyUX implements Initializable {
    @FXML private Label totalPower;
    @FXML private Label turingsToRecoverLabel;
    @FXML private Button closeButton;
    @FXML private TableView<TableItem> table;
    @FXML private TableColumn<TableItem,String> typeCol;
    @FXML private TableColumn<TableItem,String> rankCol;
    @FXML private TableColumn<TableItem,String> purchaseCol;
    @FXML private TableColumn<TableItem,String> maxPowerCol;
    @FXML private TableColumn<TableItem,String> roundReductionCol;
    @FXML private TableColumn<TableItem,String> singlePowerCol;
    @FXML private TableColumn<TableItem,String> totalUnitsCol;
    @FXML private TableColumn<TableItem,String> totalPowerCol;
    @FXML private Pane mPane;

    private Stage pStage;
    private Scene scene;

    public ShowArmyUX() throws Exception {
        FXMLLoader root = new FXMLLoader(getClass().getResource("showArmy.fxml"));
        root.setController(this);
        mPane = root.load();
        mPane.setStyle("-fx-background: "+GameUX.getBackgroundColor());
    }

    @FXML
    void close(ActionEvent event) {
        pStage.close();
    }

    public void setStage() throws IOException {
            pStage = new Stage();
            pStage.setTitle("Army on ground");
            scene = new Scene(mPane,600,300);
            pStage.setScene(scene);
    }

    public void launchStage() throws IOException{
        setStage();
        pStage.show();
    }

    public void setTotalPowerLabel(Integer totPower){
        totalPower.textProperty().setValue("Total power: "+totPower.toString());
    }

    public void setTuringsToRecoverLabel(Integer tursToRecover){
        turingsToRecoverLabel.textProperty().setValue("Turings to recover: "+tursToRecover.toString());
    }

    public void setTable(ArrayList<ArrayList<String>> props){
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        purchaseCol.setCellValueFactory(new PropertyValueFactory<>("purchase"));
        maxPowerCol.setCellValueFactory(new PropertyValueFactory<>("maxPower"));
        roundReductionCol.setCellValueFactory(new PropertyValueFactory<>("roundReduction"));
        singlePowerCol.setCellValueFactory(new PropertyValueFactory<>("singlePower"));
        totalUnitsCol.setCellValueFactory(new PropertyValueFactory<>("totalUnits"));
        totalPowerCol.setCellValueFactory(new PropertyValueFactory<>("totalPower"));

        ObservableList<TableItem> obList = FXCollections.observableArrayList();

        props.forEach(propLine->{
            obList.add(new TableItem(propLine));
        });

        table.setItems(obList);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public class TableItem
    {
        private SimpleStringProperty type;
        private SimpleStringProperty rank;
        private SimpleStringProperty purchase;
        private SimpleStringProperty maxPower;
        private SimpleStringProperty roundReduction;
        private SimpleStringProperty singlePower;
        private SimpleStringProperty totalUnits;
        private SimpleStringProperty totalPower;

        public TableItem(ArrayList<String> props){
            type = new SimpleStringProperty(props.get(0));
            rank = new SimpleStringProperty(props.get(1));
            purchase = new SimpleStringProperty(props.get(2));
            maxPower = new SimpleStringProperty(props.get(3));
            roundReduction = new SimpleStringProperty(props.get(4));
            singlePower = new SimpleStringProperty(props.get(5));
            totalUnits = new SimpleStringProperty(props.get(6));
            totalPower = new SimpleStringProperty(props.get(7));
        }

        public String getType(){
            return type.getValue();
        }

        public String getRank(){
            return rank.getValue();
        }

        public String getPurchase(){
            return purchase.getValue();
        }

        public String getMaxPower(){
            return maxPower.getValue();
        }

        public String getRoundReduction(){
            return roundReduction.getValue();
        }

        public String getSinglePower(){
            return singlePower.getValue();
        }

        public String getTotalUnits(){
            return totalUnits.getValue();
        }

        public String getTotalPower(){
            return totalPower.getValue();
        }
    }
}
