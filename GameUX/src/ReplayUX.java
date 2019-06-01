import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ReplayUX extends Observable implements Initializable {

    @FXML private ScrollPane mPane;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private VBox playersVbox;
    @FXML private GridPane gameBoard;
    @FXML private Label territoryInfo;
    @FXML private Label playerInfo;
    private FXMLLoader root;
    private Stage pStage;
    private Scene scene;
    private Integer currState;
    private Integer maxState;
    private Integer currTerritory;
    private String currPlayer;

    public ReplayUX(Integer currState) throws Exception{
        root = new FXMLLoader(getClass().getResource("replay.fxml"));
        root.setController(this);
        mPane = root.load();
        setSkin();
        this.maxState = currState;
        this.currState = this.maxState-1;
        currPlayer = null;
    }

    public Integer getCurrState(){
        return currState;
    }

    public Integer getCurrTerritory(){
        return currTerritory;
    }

    public String getCurrPlayerName(){
        return currPlayer;
    }

    public void setObserver(Observer observer){
        addObserver(observer);
    }

    public void setStage(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Replay");
        scene = new Scene(mPane,900,800);
        primaryStage.setScene(scene);
    }

    public void launchLoader() throws Exception {
        pStage = new Stage();
        setStage(pStage);
        pStage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) { }

    public void setNextButton(Boolean bool){
        nextButton.disableProperty().setValue(!bool);
    }

    public void setPrevButton(Boolean bool){
        prevButton.disableProperty().setValue(!bool);
    }

    @FXML
    void nextState(ActionEvent event) {
        ++currState;
        if (currState == maxState - 1){
            setNextButton(false);
        }
        if (currState > 0){
            setPrevButton(true);
        }
        setChanged();
        notifyObservers("changeState");
    }

    @FXML
    void prevState(ActionEvent event) {
        --currState;
        if (currState == 0){
            setPrevButton(false);
        }
        if (currState < maxState - 1){
            setNextButton(true);
        }
        setChanged();
        notifyObservers("changeState");
    }

    @FXML
    void showArmy(ActionEvent event){
        setChanged();
        notifyObservers("replayShowArmy");
    }

    void setTerritoryInfo(String terInfo){
        territoryInfo.textProperty().setValue(terInfo);
    }

    void setPlayerInfo(String playerInfo){
        this.playerInfo.textProperty().setValue(playerInfo);
    }

    public void setGameBoard(Integer rows, Integer columns){
        gameBoard.getChildren().clear();
        for (int i = 0; i < rows; ++i){
            gameBoard.addRow(i);
            for (int j = 0;j < columns; ++j){
                gameBoard.addColumn(j);
                Button cell = new Button();
                final Integer currId = i*columns+j+1;
                cell.setText(currId.toString());
                cell.getStyleClass().add("boardButton");
                cell.setAlignment(Pos.CENTER);
                cell.setOnAction(e->{currTerritory = currId;setChanged(); notifyObservers("replayTerritory");});
                gameBoard.add(cell, j, i);
            }
        }
    }

    public void paintCell(Color color, Integer terId){
        Button terButton = (Button)gameBoard.getChildren().get(terId-1);

        if (color == Color.TRANSPARENT)
            terButton.setStyle(null);
        else {
            ArrayList<StringBuilder> rgbHexStr = new ArrayList<>(3);

            rgbHexStr.add(new StringBuilder(Long.toUnsignedString((int) (color.getRed()*255),16)));
            rgbHexStr.add(new StringBuilder(Long.toUnsignedString((int) (color.getGreen()*255),16)));
            rgbHexStr.add(new StringBuilder(Long.toUnsignedString((int) (color.getBlue()*255),16)));

            for (StringBuilder c:rgbHexStr){
                if(c.length() == 1){
                    c.insert(0,"0");
                }
            }
            terButton.setStyle("-fx-background-color: #" + rgbHexStr.get(0).toString() + rgbHexStr.get(1).toString() + rgbHexStr.get(2).toString());
        }
    }

    public void addPlayerToVbox(String playerName, Color playerColor){
        Button playerButton = new Button();
        String col = GameUX.resolveColor(playerColor);
        playerButton.setText(playerName);
        playerButton.setPrefWidth(100);
        playerButton.setStyle("-fx-background-color: #"+col);
        playerButton.setOnAction(e->{
            this.currPlayer = playerName;
            setChanged();
            notifyObservers("replayPlayerInfo");
        });

        playersVbox.getChildren().add(playerButton);
    }

    public void clearPlayersVbox(){
        playersVbox.getChildren().clear();
    }

    private void setSkin(){
        String style = GameUX.resolveSkin(GameUX.getSkinColor());
        mPane.setId(style);
        Button[] buttons = {prevButton, nextButton};
        for(Button btn:buttons){
            btn.getStyleClass().remove(GameUX.resolveSkin(GameUX.getPrevSkinColor()));
            btn.getStyleClass().add(style);
        }
        gameBoard.getChildren().forEach(btn->{
            btn.getStyleClass().remove(GameUX.resolveSkin(GameUX.getPrevSkinColor())+"Button");
            btn.getStyleClass().add(style);
        });
        playersVbox.getChildren().forEach(btn->{
            btn.getStyleClass().remove(GameUX.resolveSkin(GameUX.getPrevSkinColor())+"Button");
            btn.getStyleClass().add(style);
        });
    }
}
