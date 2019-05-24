import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.fxml.FXML;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

public class GameUX extends Observable {
    @FXML private ButtonBar gameAction;
    @FXML private TitledPane loadVbox;
    @FXML private Label playerInfo;
    @FXML private Label teritoryInfo;
    @FXML private GridPane gameBoard;
    @FXML private ButtonBar roundAction;
    @FXML private Button newGameBtn;
    @FXML private Button newRoundBtn;
    @FXML private Button historyBtn;
    @FXML private Button saveGameBtn;
    @FXML private Button loadXmlBtn;
    @FXML private Button loadSavelBtn;
    @FXML private Button undoBtn;
    @FXML private Label roundsLeft;
    @FXML private Button forefitButton;
    @FXML private Button armyButton;
    @FXML private Button endGameButton;

    private FXMLLoader root;
    private Parent mainPane;
    private Stage pStage;

    public GameUX(){
        root = new FXMLLoader(getClass().getResource("conqueresUI.fxml"));
        root.setController(this);
        try {
            mainPane = root.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void boost(ActionEvent event) {
        setChanged();
        notifyObservers("boost");
    }

    @FXML
    void conquer(ActionEvent event) {
        setChanged();
        notifyObservers("conquer");
    }

    @FXML
    void doNothing(ActionEvent event) { setChanged(); notifyObservers("doNothing"); }

    @FXML
    void exit(ActionEvent event) {
        setChanged();
        notifyObservers("exit");
        pStage.close();
    }

    @FXML
    void history(ActionEvent event) { setChanged(); notifyObservers("history"); }

    @FXML
    void loadSavedGame(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open saved game");
        File file = fileChooser.showOpenDialog(new Stage());
        if (file!=null) {
            setChanged();
            notifyObservers("loadSavedGame " + file.getAbsolutePath());
        }
    }

    @FXML
    void loadXML(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showOpenDialog(new Stage());
        if (file!=null) {
            setChanged();
            notifyObservers("loadXML " + file.getAbsolutePath());
        }
    }

    @FXML
    void startNewGame(ActionEvent event) { setChanged(); notifyObservers("startNewGame"); }

    @FXML
    void startNewRound(ActionEvent event) { setChanged(); notifyObservers("startNewRound");}

    @FXML
    void undo(ActionEvent event) { setChanged(); notifyObservers("undo");}

    @FXML
    void saveGame(ActionEvent event){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save directory");
        File saveDirectory = fileChooser.showSaveDialog(new Stage());
        setChanged();
        notifyObservers("saveGame "+saveDirectory.getAbsolutePath());
    }

    @FXML
    void forefit(ActionEvent event){
        setChanged();
        notifyObservers("forefit");
    }

    @FXML
    void showArmy(ActionEvent event){
        setChanged();
        notifyObservers("showArmy");
    }

    @FXML
    void endGame(ActionEvent event) {
        setChanged();
        notifyObservers("endGame");
    }

    public void bindPlayerInfo(StringProperty stringProperty){
        playerInfo.textProperty().bind(stringProperty);
    }

    public void bindTeritoryInfo(StringProperty stringProperty){
        teritoryInfo.textProperty().bind(stringProperty);
    }

    public void bindInGameButtonsAccess(BooleanBinding bool){
        newGameBtn.disableProperty().bind(bool);
    }

    public void bindLoadMenuButtonsAccess(BooleanBinding bool){
        loadXmlBtn.disableProperty().bind(bool);
        loadSavelBtn.disableProperty().bind(bool);
    }

    public void bindInRoundButton(BooleanBinding bool){
        newRoundBtn.disableProperty().bind(bool);
        undoBtn.disableProperty().bind(bool);
    }

    public void bindRoundActionButtons(BooleanBinding bool){
        roundAction.getButtons().forEach(b->b.disableProperty().bind(bool));
    }

    public void bindRoundsLeftLabel(IntegerBinding intBinding) {
        roundsLeft.textProperty().bind(new SimpleStringProperty("Rounds left: ").concat(intBinding));
    }

    public void setContoller(Observer observer){
        addObserver(observer);
    }

    public void setStage(Stage primaryStage) throws IOException {
        pStage = primaryStage;
        primaryStage.setTitle("Conqueres");
        //disableRoundAction();
        Scene scene = new Scene(mainPane,800,800);
        primaryStage.setScene(scene);
    }

    public void setGameBoard(Integer rows, Integer columns){
        for (int i = 0; i < rows; ++i){
            gameBoard.addColumn(i);
            for (int j = 0;j < columns; ++j){
                gameBoard.addColumn(j);
                Button cell = new Button();
                final Integer currId = i*columns+j+1;
                cell.setText(currId.toString());
                cell.setOnAction(e->{setChanged(); notifyObservers("territory "+currId);});
                gameBoard.add(cell, i, j);
            }
        }
    }

    public void setTeritoryColor(Integer terId, Color col){
        Button terButton = (Button)gameBoard.getChildren().get(terId-1);
        ArrayList<StringBuilder> rgbHexStr = new ArrayList<>(3);

        rgbHexStr.add(new StringBuilder(Long.toUnsignedString((int) (col.getRed()*255),16)));
        rgbHexStr.add(new StringBuilder(Long.toUnsignedString((int) (col.getGreen()*255),16)));
        rgbHexStr.add(new StringBuilder(Long.toUnsignedString((int) (col.getBlue()*255),16)));

        for (StringBuilder c:rgbHexStr){
            if(c.length() == 1){
                c.insert(0,"0");
            }
        }

        terButton.setStyle("-fx-background-color: #"+rgbHexStr.get(0).toString()+rgbHexStr.get(1).toString()+rgbHexStr.get(2).toString());
    }

    public void setLoaders(Boolean cond){
        ((AnchorPane)loadVbox.getContent()).getChildren().forEach(b->{
            if (((Button)b).getText().compareTo("Exit")!=0)
                ((Button)b).setDisable(cond);}
        );
    }

    public void setArmyShowButton(Boolean toActivate){
        armyButton.setDisable(!toActivate);
        if(toActivate){
            armyButton.setOpacity(1);
        }
        else{
            armyButton.setOpacity(0);
        }
    }

    public void bindToDisableForefitButton(BooleanBinding prop){
        forefitButton.disableProperty().bind(prop);
    }

}
