import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

public class GameController implements Observer {

    enum RoundAction {NO_ACTION, DO_NOTHING, BOOST, CONQUER}
    private GameUX gameUX;
    private GameEngine gameEngine;
    private StringProperty currPlayerInfo;
    private StringProperty currTerritoryInfo;
    private ArrayList<String> args;
    private RoundAction actionFlag = RoundAction.NO_ACTION;
    private ArrayList<Thread> threads;
    private DoubleProperty loadProgress;

    public GameController(){
        gameUX = new GameUX();
        gameEngine = new GameEngine();
        gameUX.setContoller(this);
        currPlayerInfo = new SimpleStringProperty("");
        currTerritoryInfo = new SimpleStringProperty("");
        gameUX.bindPlayerInfo(currPlayerInfo);
        gameUX.bindTeritoryInfo(currTerritoryInfo);
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
        runCommand(()->{gameEngine = fileHandling.fileToObj(args.get(1));});
    }

    public void loadXML() throws Exception {
        runCommand(()->gameEngine.loadFile(args.get(1)));
        FileLoaderUX loaderScene = new FileLoaderUX();
        loaderScene.bindProgressBar(gameEngine.getLoadProg());
        loaderScene.launchLoader();
    }

    public void startNewGame(){
        System.out.println("Starting new game");
        runCommand(()->gameEngine.setGame("Player1","Player2"));
        Platform.runLater(()->gameUX.setGameBoard(gameEngine.getBoard().getRows(),gameEngine.getBoard().getColumns()));
        Platform.runLater(()->gameUX.setLoaders(gameEngine.isGameSet()));
    }

    public void territory(){
        Platform.runLater(()->currTerritoryInfo.setValue(gameEngine.showTeriritoryInfo(Integer.parseInt(args.get(1)))));
    }

    public void saveGame(){
        String saveDir = args.get(1);
        System.out.println("Saving in "+saveDir);
        runCommand(()->fileHandling.objToFile(gameEngine,saveDir));
    }

    public void startNewRound(){
        Platform.runLater(()->gameUX.activateRoundAction());
        Platform.runLater(()->currPlayerInfo.setValue(gameEngine.getPlayers().get(gameEngine.getCurrTurn()).toString()));
    }

    public void doNothing(){
        actionFlag = RoundAction.DO_NOTHING;
        nextPlayer();
    }

    public void boost(){
        actionFlag = RoundAction.BOOST;
        nextPlayer();
    }

    public void conquer(){
        actionFlag = RoundAction.CONQUER;
        nextPlayer();

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
        threads.forEach(thr->{if (thr.isAlive()) thr.stop();});
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

    private void nextPlayer() {
        if (!gameEngine.isGameOver()) {
            gameEngine.nextTurn();
            if (gameEngine.getCurrTurn() == 0) {
                currPlayerInfo.setValue("");
                Platform.runLater(() -> gameUX.disableRoundAction());
                if (gameEngine.isGameOver()){
                    Platform.runLater(()->gameUX.setLoaders(gameEngine.isGameOver()));
                }
            } else {
                currPlayerInfo.setValue(gameEngine.getPlayers().get(gameEngine.getCurrTurn()).toString());
            }
        }
    }

}
