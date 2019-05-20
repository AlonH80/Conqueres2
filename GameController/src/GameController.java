import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class GameController implements Observer {

    enum RoundAction {NO_ACTION, DO_NOTHING, BOOST, CONQUER}
    private GameUX gameUX;
    private ChooseArmyUX chooseArmy;
    private BoostArmyUX boostArmy;
    private GameEngine gameEngine;
    private StringProperty currPlayerInfo;
    private StringProperty currTerritoryInfo;
    private Integer currTerritoryId = 0;
    private BooleanProperty inRound;
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
        gameUX.bindPlayerInfo(currPlayerInfo);
        gameUX.bindTeritoryInfo(currTerritoryInfo);
        gameUX.bindInGameButtonsAccess(gameEngine.getValid().not().or(gameEngine.getGameSet()));
        gameUX.bindLoadMenuButtonsAccess(gameEngine.getGameSet().and(gameEngine.getGameSet()));
        gameUX.bindInRoundButton(gameEngine.getGameSet().not().or(inRound));
        gameUX.bindRoundActionButtons(inRound.not().and(inRound.not()));
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
        runCommand(()->gameEngine.setGame());
        Platform.runLater(()->gameUX.setGameBoard(gameEngine.getBoard().getRows(),gameEngine.getBoard().getColumns()));
        //Platform.runLater(()->gameUX.setLoaders(gameEngine.isGameSet()));
    }

    public void territory(){
        Platform.runLater(()->currTerritoryInfo.setValue(gameEngine.getGroundInfo(Integer.parseInt(args.get(1)))));
        currTerritoryId = Integer.parseInt(args.get(1));
    }

    private void reShowTerritory(){
        Platform.runLater(()->currTerritoryInfo.setValue(gameEngine.getGroundInfo(currTerritoryId)));
    }

    public void saveGame(){
        String saveDir = args.get(1);
        System.out.println("Saving in "+saveDir);
        runCommand(()->FileHandling.objToFile(gameEngine,saveDir));
    }

    public void startNewRound(){
        //Platform.runLater(()->gameUX.activateRoundAction());
        inRound.setValue(true);
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
            inRound.setValue(false);
            Map<String, Integer> unitsRec = gameEngine.turingsToRecvoerByType(currTerritoryId);
            Map<String, Integer> unitsNew = new HashMap<>();
            gameEngine.getArmy().forEach(un->unitsNew.put(un.getType(),gameEngine.getPlayerAmountOfTurings(gameEngine.getCurrTurn())/un.getPurchase()));
            boostArmy = new BoostArmyUX();
            boostArmy.setNotifier(this);
            boostArmy.setRecoverSpinners(unitsRec);
            boostArmy.setNewUnitsSpinners(unitsNew);
            /*boostArmy.bindToTuringsLabel(new SimpleIntegerProperty(boostArmy.getSpinners().getChildren().stream().
                    mapToInt(k->((Spinner<Integer>)(((HBox)k).getChildren().get(1))).getValue()).sum()));*/
            boostArmy.launchLoader();
        }
        nextPlayer();
    }

    public void conquer() throws Exception{
        actionFlag = RoundAction.CONQUER;
        Map<Integer, Boolean> availableTers = gameEngine.getAvailableTeritoryToConquer();
        if (currTerritoryId != 0 && availableTers.keySet().contains(currTerritoryId)) {
            inRound.setValue(false);
            Map<String, Integer> units = new HashMap<>();
            ArrayList<ArmyUnit> army = gameEngine.getArmy();
            army.forEach(un -> units.put(un.getType(), un.getPurchase()));
            chooseArmy = new ChooseArmyUX();
            chooseArmy.setNotifier(this);
            chooseArmy.setArmyUnitsSpinners(units);
            chooseArmy.bindToTuringsLabel(new SimpleIntegerProperty(chooseArmy.getSpinners().getChildren().stream().
                    mapToInt(k->((Spinner<Integer>)(((HBox)k).getChildren().get(1))).getValue()).sum()));
            chooseArmy.launchLoader();
        }
    }

    public void undo(){
        runCommand(()-> {
            if (gameEngine.isGameSet()) {
                if (GameEngine.gameState.size() > 0) {
                    gameEngine = (GameEngine) GameEngine.getLastGameState().clone();
                    GameEngine.gameState.remove(GameEngine.gameState.size() - 1);
                    System.out.println("Undo succesfully done.");
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

    private void playRound(){
        actionFlag = RoundAction.NO_ACTION;
        runCommand(()->
        {
        for (Player player:gameEngine.getPlayers()){
            Platform.runLater(()->currPlayerInfo.setValue(player.toString()));
            while (actionFlag == RoundAction.NO_ACTION) {}
            System.out.println(actionFlag.toString());
            actionFlag = RoundAction.NO_ACTION;
        }
        Platform.runLater(()->currPlayerInfo.setValue(""));
        });
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
        System.out.println(gameEngine.playerConquer(currTerritoryId,army));
        inRound.setValue(true);
        nextPlayer();
    }

    private void getBoostArmy(){
        Map<String,Integer> armyNew= boostArmy.getArmyNew();
        Map<String,Integer> armyBoost= boostArmy.getArmyBoost();
        armyBoost.keySet().forEach(un->gameEngine.addPower(gameEngine.getPlayerName(gameEngine.getCurrTurn()),currTerritoryId,un,armyBoost.get(un)));
        gameEngine.addNewUnits(gameEngine.getPlayerName(gameEngine.getCurrTurn()), currTerritoryId, armyNew);
        inRound.setValue(true);
        nextPlayer();
    }

    private void nextPlayer() {
        reShowTerritory();
        if (!gameEngine.isGameOver()) {
            gameEngine.nextTurn();
            if (gameEngine.getCurrTurn() == 0) {
                currPlayerInfo.setValue("");
                inRound.setValue(false);
            } else {
                currPlayerInfo.setValue(gameEngine.getPlayers().get(gameEngine.getCurrTurn()).toString());
            }
        }
    }

}
