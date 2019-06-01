import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.ScaleTransition;
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
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

public class GameUX extends Observable {
    public enum Skin {DARK, LIGHT, UGLY}

    @FXML private ButtonBar gameAction;
    @FXML private TitledPane loadVbox;
    @FXML private TitledPane skinTitlePane;
    @FXML private Label playerInfo;
    @FXML private Label teritoryInfo;
    @FXML private GridPane gameBoard;
    @FXML private HBox roundAction;
    @FXML private Button newGameBtn;
    @FXML private Button newRoundBtn;
    @FXML private Button replayBtn;
    @FXML private Button saveGameBtn;
    @FXML private Button loadXmlBtn;
    @FXML private Button loadSavelBtn;
    @FXML private Button undoBtn;
    @FXML private Label roundsLeft;
    @FXML private Button forefitButton;
    @FXML private Button armyButton;
    @FXML private Button endGameButton;
    @FXML private ScrollPane boardScrollPane;
    @FXML private BorderPane centerPane;
    @FXML private VBox playersBox;
    @FXML private CheckBox enableAnimation;

    private Button[] buttons;
    private FXMLLoader root;
    private ScrollPane mainPane;
    private Stage pStage;
    private static Skin skin = Skin.DARK;
    private static Skin prevSkin = Skin.DARK;

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
    void replay(ActionEvent event) { setChanged(); notifyObservers("replay"); }

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

    @FXML
    void changeSkinDark(ActionEvent event){
        prevSkin = skin;
        skin = Skin.DARK;
        mainPane.setId("DarkSkin");
        changeButtonsStyle();
    }

    @FXML
    void changeSkinLight(ActionEvent event){
        prevSkin = skin;
        skin = Skin.LIGHT;
        mainPane.setId("LightSkin");
        changeButtonsStyle();
    }

    @FXML
    void changeSkinUgly(ActionEvent event){
        prevSkin = skin;
        skin = Skin.UGLY;
        mainPane.setId("UglySkin");
        changeButtonsStyle();
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

    public void bindEndGameButton(BooleanBinding bool){
        endGameButton.disableProperty().bind(bool);
    }

    public void bindRoundActionButtons(BooleanBinding bool){
        roundAction.getChildren().forEach(b->b.disableProperty().bind(bool));
    }

    public void bindRoundsLeftLabel(IntegerBinding intBinding) {
        roundsLeft.textProperty().bind(new SimpleStringProperty("Rounds left: ").concat(intBinding));
    }

    public void bindDisableReplay(BooleanBinding bool){
        replayBtn.disableProperty().bind(bool);
    }

    public void setContoller(Observer observer){
        addObserver(observer);
    }

    public void setStage(Stage primaryStage) throws IOException, IllegalStateException {
        pStage = primaryStage;
        primaryStage.setTitle("Conqueres");
        Scene scene = new Scene(mainPane,800,800);
        mainPane.setId(resolveSkin(skin));
        primaryStage.setScene(scene);
    }

    public void setGameBoard(Integer rows, Integer columns){
        gameBoard.getChildren().clear();
        for (int i = 0; i < rows; ++i){
            gameBoard.addRow(i);
            for (int j = 0;j < columns; ++j){
                gameBoard.addColumn(j);
                StackPane cell = new StackPane();
                Button cellButton = new Button();
                Rectangle cellRect = new Rectangle();
                final Integer currId = i*columns+j+1;
                cellButton.setText(currId.toString());
                cellButton.setStyle("-fx-background-color: Transparent");
                cellRect.setStyle("-fx-fill: White");
                cellButton.getStyleClass().add("boardButton");
                cellRect.setWidth(50);
                cellRect.setHeight(40);
                cell.setAlignment(Pos.CENTER);
                cellButton.setOnAction(e->{setChanged(); notifyObservers("territory "+currId);});
                cell.getChildren().add(cellRect);
                cell.getChildren().add(cellButton);
                gameBoard.add(cell, j, i);
            }
        }
    }

    public void setTeritoryColor(Integer terId,Color prev, Color col,Boolean animate){
        String color = resolveColor(col);

        if (color == null || col.equals(Color.WHITE))
            if (animate && enableAnimation.isSelected()){
                FillTransition ft = new FillTransition(Duration.millis(2000), (Rectangle)((StackPane)gameBoard.getChildren().get(terId - 1)).getChildren().get(0));
                ft.setFromValue(prev);
                ft.setToValue(Color.WHITE);
                ft.setCycleCount(1);
                ft.setOnFinished(e-> {
                    ((StackPane)gameBoard.getChildren().get(terId - 1)).getChildren().get(0).setStyle("-fx-background-color: #" + color);
                });
                ft.play();
            }
            else {
                ((StackPane)gameBoard.getChildren().get(terId - 1)).getChildren().get(0).setStyle("-fx-fill: White");
            }
        else {
            if (animate && enableAnimation.isSelected()) {
                FillTransition ft = new FillTransition(Duration.millis(2000), (Rectangle)((StackPane)gameBoard.getChildren().get(terId - 1)).getChildren().get(0));
                ft.setFromValue(prev);
                ft.setToValue(col);
                ft.setCycleCount(1);
                ft.setOnFinished(e-> {
                    ((StackPane)gameBoard.getChildren().get(terId - 1)).getChildren().get(0).setStyle("-fx-background-color: #" + color);
                });
                ft.play();

            }
            else {
                ((StackPane)gameBoard.getChildren().get(terId - 1)).getChildren().get(0).setStyle("-fx-fill: #" + color);
            }
        }
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

    public void addPlayerToVbox(String playerName){
        Button playerButton = new Button();
        playerButton.setText(playerName);
        playerButton.setPrefWidth(150);
        playerButton.setOnAction(e->{
            setChanged(); notifyObservers("playerInfo "+playerName);
        });

        playersBox.getChildren().add(playerButton);
    }

    public void clearPlayersBox(){
        playersBox.getChildren().clear();
    }

    public void paintPlayerButton(Integer playerInd, Color col){
        if (playerInd < playersBox.getChildren().size()){
            clearPlayersButtonsColor();
            String color = resolveColor(col);
            playersBox.getChildren().get(playerInd).setStyle("-fx-background-color: #" + color );
        }
        else{
            System.out.println("Invalid player ind: "+playerInd);
        }
    }

    public void clearPlayersButtonsColor(){
        playersBox.getChildren().forEach(b->b.setStyle(null));
    }

    public static Skin getSkinColor(){
        return skin;
    }

    public static Skin getPrevSkinColor(){
        return prevSkin;
    }

    public static String resolveColor(Color col){
        String strColor = new String();
        if (col == Color.TRANSPARENT)
            strColor = null;
        else {
            ArrayList<StringBuilder> rgbHexStr = new ArrayList<>(3);

            rgbHexStr.add(new StringBuilder(Long.toUnsignedString((int) (col.getRed()*255),16)));
            rgbHexStr.add(new StringBuilder(Long.toUnsignedString((int) (col.getGreen()*255),16)));
            rgbHexStr.add(new StringBuilder(Long.toUnsignedString((int) (col.getBlue()*255),16)));

            for (StringBuilder c:rgbHexStr){
                if(c.length() == 1){
                    c.insert(0,"0");
                }
            }

            strColor = rgbHexStr.get(0).toString() + rgbHexStr.get(1).toString() + rgbHexStr.get(2).toString();
        }

        return strColor;
    }

    private void changeButtonsStyle(){
        Button[] buttons = {newGameBtn, newRoundBtn, replayBtn, saveGameBtn, loadXmlBtn, loadSavelBtn, undoBtn, forefitButton, armyButton, endGameButton};
        StringBuilder style = new StringBuilder(resolveSkin(skin));
        style.append("Button");

            for(Button btn:buttons){
                btn.getStyleClass().remove(resolveSkin(prevSkin)+"Button");
                btn.getStyleClass().add(style.toString());
            }
            //if (gameBoard.getChildren().size() > 0) gameBoard.getChildren().forEach(btn->btn.setId(style.toString()));
            /*if (playersBox.getChildren().size() > 0) playersBox.getChildren().forEach(btn->{
                btn.getStyleClass().remove(resolveSkin(prevSkin)+"Button");
                btn.getStyleClass().add(style.toString());
            });*/
            ((VBox)skinTitlePane.getContent()).getChildren().forEach(btn->{
                btn.getStyleClass().remove(resolveSkin(prevSkin)+"Button");
                btn.getStyleClass().add(style.toString());
            });
            ((VBox)loadVbox.getContent()).getChildren().forEach(btn->{
                btn.getStyleClass().remove(resolveSkin(prevSkin)+"Button");
                btn.getStyleClass().add(style.toString());
            });
            roundAction.getChildren().forEach(btn->{
                btn.getStyleClass().remove(resolveSkin(prevSkin)+"Button");
                btn.getStyleClass().add(style.toString());
            });
    }

    public Boolean getEnableAnimate(){
        return enableAnimation.isSelected();
    }

    public static String resolveSkin(Skin skin) {
        switch (skin) {
            case DARK:
                return "DarkSkin";
            case LIGHT:
                return "LightSkin";
            case UGLY:
                return "UglySkin";
        }
        return "DarkSkin";
    }

    public void glowWinnersTerritories(ArrayList<Integer> tersIds){
        if (getEnableAnimate()) {
            tersIds.forEach(terId -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(250), ((StackPane) gameBoard.getChildren().get(terId - 1)).getChildren().get(0));
                st.setByX(1.1);
                st.setByY(1.1);
                st.setCycleCount(4);
                st.setAutoReverse(true);
                st.play();
            });
        }
    }
}
