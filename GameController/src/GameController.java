import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import javafx.scene.paint.Color;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class GameController implements Observer {

    enum RoundAction {NO_ACTION, DO_NOTHING, BOOST, CONQUER}
    private GameUX gameUX;
    private ChooseArmyUX chooseArmy;
    private BoostArmyUX boostArmy;
    private ShowArmyUX showArmyUX;
    private GameEngine gameEngine;
    private StringProperty currPlayerInfo;
    private StringProperty currTerritoryInfo;
    private IntegerProperty roundsleft;
    private Integer currTerritoryId = 0;
    private BooleanProperty inRound;
    private BooleanProperty inRoundAction;
    private ArrayList<String> args;
    private RoundAction actionFlag = RoundAction.NO_ACTION;
    private ArrayList<Thread> threads;

    public GameController(){
        gameUX = new GameUX();
        gameEngine = new GameEngine();
        gameUX.setContoller(this);
        currPlayerInfo = new SimpleStringProperty("");
        currTerritoryInfo = new SimpleStringProperty("");
        inRound = new SimpleBooleanProperty(false);
        inRoundAction = new SimpleBooleanProperty(false);
        roundsleft = new SimpleIntegerProperty(0);
        gameUX.bindPlayerInfo(currPlayerInfo);
        gameUX.bindTeritoryInfo(currTerritoryInfo);
        gameUX.bindInGameButtonsAccess(gameEngine.getValid().not().or(gameEngine.getGameSet()));
        gameUX.bindLoadMenuButtonsAccess(gameEngine.getGameSet().and(gameEngine.getGameSet()));
        gameUX.bindInRoundButton(gameEngine.getGameSet().not().or(inRound));
        gameUX.bindRoundActionButtons(inRound.not().or(inRoundAction));
        gameUX.bindRoundsLeftLabel(roundsleft.add(0));
        gameUX.bindToDisableForefitButton(inRound.not());
        args = new ArrayList<>();
        threads = new ArrayList<>();
    }

    @Override
    public void update(Observable o, Object arg) {
        args.clear();
        Arrays.stream(((String) arg).split(" ")).forEach(args::add);
        System.out.println(args.get(0) +" called");
        try {
            invokeMethod(args.get(0));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void invokeMethod(String methodName){
        ArrayList<Method> methods = new ArrayList<>();
        Arrays.stream(GameController.class.getDeclaredMethods()).forEach(methods::add);
        methods.stream().forEach(method -> {if (method.getName().compareTo(methodName)==0){
            try {
                method.invoke(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        });
    }

    public GameUX getGameUX(){
        return gameUX;
    }

    public void loadSavedGame(){
        System.out.println("Loading "+args.get(1));
        runCommand(()->{gameEngine = FileHandling.fileToObj(args.get(1));});
    }

    public void loadXML() throws Exception {
        FileLoaderUX loaderScene = new FileLoaderUX();
        runCommand(()->{System.out.println(gameEngine.loadFile(args.get(1)));Platform.runLater(()->loaderScene.enableFinishLoader());});
        loaderScene.bindProgressBar(gameEngine.getLoadProgress());
        loaderScene.launchLoader();
    }

    public void startNewGame(){
        System.out.println("Starting new game");
        runCommand(()->{
                        gameEngine.setGame();
                        Integer roundsSet = gameEngine.getCurrRound();
                        Platform.runLater(()->roundsleft.setValue(roundsSet));
        });
        Platform.runLater(()->gameUX.setGameBoard(gameEngine.getBoard().getRows(),gameEngine.getBoard().getColumns()));
        //Platform.runLater(()->gameUX.setLoaders(gameEngine.isGameSet()));
    }

    public void territory(){
        currTerritoryId = Integer.parseInt(args.get(1));
        showTerritory();
    }

    private void reShowTerritory(){
        if (currTerritoryId!=0) {
            showTerritory();
        }
    }

    private void showTerritory(){
        Platform.runLater(() -> currTerritoryInfo.setValue(gameEngine.getGroundInfo(currTerritoryId)));
        Color terColor = gameEngine.getTeritoryColor(currTerritoryId);
        gameUX.setTeritoryColor(currTerritoryId, gameEngine.getTeritoryColor(currTerritoryId));

        if (gameEngine.getCurrTurn() < gameEngine.getPlayers().size()) {
            gameUX.setArmyShowButton(gameEngine.getPlayerTeritoryIds(gameEngine.getCurrTurn()).contains(currTerritoryId));
        }
        else{
            gameUX.setArmyShowButton(false);
        }
    }

    public void saveGame(){
        String saveDir = args.get(1);
        System.out.println("Saving in "+saveDir);
        runCommand(()->FileHandling.objToFile(gameEngine,saveDir));
    }

    public void startNewRound(){
        inRound.setValue(true);
        nextPlayer();
        Platform.runLater(()->currPlayerInfo.setValue(gameEngine.getPlayers().get(gameEngine.getCurrTurn()).toString()));
    }

    public void doNothing(){
        actionFlag = RoundAction.DO_NOTHING;
        nextPlayer();
    }

    public void boost() throws Exception {
        actionFlag = RoundAction.BOOST;
        ArrayList<Integer> playerTers = gameEngine.getPlayerTeritoryIds(gameEngine.getCurrTurn());
        if (currTerritoryId != 0 && playerTers.contains(currTerritoryId)) {
            inRoundAction.setValue(true);
            Map<String, Integer> unitsRec = gameEngine.turingsToRecvoerByType(currTerritoryId);
            Map<String, Integer> unitsNew = new HashMap<>();
            gameEngine.getArmy().forEach(un->unitsNew.put(un.getType(),un.getPurchase()));
            boostArmy = new BoostArmyUX();
            boostArmy.setNotifier(this);
            boostArmy.setRecoverSpinners(unitsRec);
            boostArmy.setNewUnitsSpinners(unitsNew);
            /*boostArmy.bindToTuringsLabel(new SimpleIntegerProperty(boostArmy.getSpinners().getChildren().stream().
                    mapToInt(k->((Spinner<Integer>)(((HBox)k).getChildren().get(1))).getValue()).sum()));*/
            boostArmy.launchLoader();
        }
    }

    public void conquer() throws Exception{
        actionFlag = RoundAction.CONQUER;
        Map<Integer, Boolean> availableTers = gameEngine.getAvailableTeritoryToConquer();
        if (currTerritoryId != 0 && availableTers.keySet().contains(currTerritoryId)) {
            inRoundAction.setValue(true);
            Map<String, Integer> units = new HashMap<>();
            ArrayList<ArmyUnit> army = gameEngine.getArmy();
            army.forEach(un -> units.put(un.getType(), un.getPurchase()));
            chooseArmy = new ChooseArmyUX();
            if (gameEngine.isConquered(currTerritoryId) == false){
                chooseArmy.disableChooseAttackBox();
            }
            else{
                chooseArmy.enableChooseAttackBox();
            }
            chooseArmy.setNotifier(this);
            chooseArmy.setArmyUnitsSpinners(units);
            chooseArmy.launchLoader();
        }
    }

    public void undo(){
        runCommand(()-> {
            if (gameEngine.isGameSet()) {
                if (GameEngine.gameState.size() > 0) {
                    gameEngine = (GameEngine) GameEngine.getLastGameState().clone();
                    GameEngine.gameState.remove(GameEngine.gameState.size() - 1);
                    popMessage("Undo succesfully done.");
                    reShowTerritory();
                    Platform.runLater(()->roundsleft.setValue(gameEngine.getCurrRound()));
                } else {
                    System.out.println("No rounds played yet. Undo wasn't performed.");
                }
            } else {
                System.out.println("Start new game first.");
            }
        });
    }

    public void exit(){
        System.out.println("Exiting");
        //threads.forEach(thr->{if (thr.isAlive()) thr.;});
    }

    private void runCommand(Runnable command){
        Thread thr = new Thread(command);
        thr.start();
        threads.add(thr);
    }

    private void playerAction(){
        switch (actionFlag)
        {
            case DO_NOTHING: return;
            case BOOST: //reinforceUnit
                return;
            case CONQUER: // conquer
                return;
        }
    }

    private void getArmy(){
        Map<String,Integer> army = chooseArmy.getArmy();
        String result = new String();

        if (args.get(1).compareTo("lottery") == 0) {
            result = gameEngine.playerConquer(currTerritoryId, army, Player.AttackingMethod.LOTTERY);
        }
        else if (args.get(1).compareTo("determinist") == 0) {
            result = gameEngine.playerConquer(currTerritoryId, army, Player.AttackingMethod.CALCULATED);
        }
        inRoundAction.setValue(false);

        popMessage(result);
        nextPlayer();
    }

    private void noActionOnChooseWindow(){
        inRoundAction.setValue(false);
    }

    private void getBoostArmy(){
        Map<String,Integer> armyNew= boostArmy.getArmyNew();
        Map<String,Integer> armyBoost= boostArmy.getArmyBoost();
        armyBoost.keySet().forEach(un->gameEngine.addPower(gameEngine.getPlayerName(gameEngine.getCurrTurn()),currTerritoryId,un,armyBoost.get(un)));
        gameEngine.addNewUnits(gameEngine.getPlayerName(gameEngine.getCurrTurn()), currTerritoryId, armyNew);
        inRoundAction.setValue(false);
        nextPlayer();
    }

    private void nextPlayer() {
        if (!gameEngine.isGameOver()) {
            gameEngine.nextTurn();
            if (gameEngine.getCurrTurn() == gameEngine.getPlayers().size()) {
                currPlayerInfo.setValue("");
                inRound.setValue(false);
                roundsleft.setValue(gameEngine.getCurrRound());
            } else {
                currPlayerInfo.setValue(gameEngine.getPlayers().get(gameEngine.getCurrTurn()).toString());
            }
        }
        else{
            popMessage(gameEngine.getLeader());
        }
        reShowTerritory();
    }

    private void popMessage(String message){
        try {
            PopMessageUX messWindow = new PopMessageUX();
            messWindow.setMessageLabel(message);
            messWindow.launchWindow();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showArmy(){
        try {
            showArmyUX = new ShowArmyUX();
            ArrayList<ArrayList<String>> details = gameEngine.getArmyDetails(currTerritoryId);
            showArmyUX.setTable(details);
            showArmyUX.setTotalPowerLabel(gameEngine.getTeritoryTotalArmyPower(currTerritoryId));
            showArmyUX.setTuringsToRecoverLabel(gameEngine.getTeritoryMissingTuringsToRecover(currTerritoryId));
            showArmyUX.launchStage();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void forefit(){
        gameEngine.removePlayer();
        if (gameEngine.getPlayers().size() == 0){
            endGame();
        }
        nextPlayer();
    }

    private void endGame(){
        gameEngine.endGame();
        roundsleft.setValue(gameEngine.getCurrRound());
        currPlayerInfo.setValue("");
        inRound.setValue(false);
        popMessage(gameEngine.getLeader());
    }
}
