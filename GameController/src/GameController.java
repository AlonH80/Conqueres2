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
    private ReplayUX replayUX;
    private GameEngine gameEngine;
    private StringProperty currPlayerInfo;
    private StringProperty currTerritoryInfo;
    private IntegerProperty roundsleft;
    private Integer currTerritoryId = 0;
    private BooleanProperty inRound;
    private BooleanProperty inRoundAction;
    private BooleanProperty replayEnable;
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
        replayEnable = new SimpleBooleanProperty(false);
        roundsleft = new SimpleIntegerProperty(0);
        bindProperties();
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
        runCommand(()->{
            gameEngine = FileHandling.fileToObj(args.get(1));
            gameEngine.setPlayersColors();
            gameEngine.setPropertiesVars();
            bindProperties();
            if (gameEngine.getGameSet().getValue()){
                Platform.runLater(()->gameUX.setGameBoard(gameEngine.getBoard().getRows(),gameEngine.getBoard().getColumns()));
                Platform.runLater(()->gameEngine.getBoard().getBoard().forEach(row->{
                    row.forEach(cell->{gameUX.setTeritoryColor(cell.getId(), Color.WHITE, gameEngine.getTeritoryColor(cell.getId()), false);});
                }));
                Platform.runLater(()->gameUX.clearPlayersBox());
                Platform.runLater(()->gameEngine.getPlayers().forEach(pla->gameUX.addPlayerToVbox(pla.getName())));
                Platform.runLater(()->replayEnable.setValue(false));
                Platform.runLater(()->nextPlayer());
            }
        });
    }

    public void loadXML() throws Exception {
        FileLoaderUX loaderScene = new FileLoaderUX();
        StringProperty engineMessage = new SimpleStringProperty();
        loaderScene.bindProgressBar(gameEngine.getLoadProgress());
        loaderScene.bindMessageLabel(engineMessage);
        runCommand(()->{String loadMessage = gameEngine.loadFile(args.get(1));Platform.runLater(()->{engineMessage.setValue(loadMessage);loaderScene.enableFinishLoader();});});
        loaderScene.launchLoader();
    }

    public void startNewGame(){
        System.out.println("Starting new game");
        runCommand(()->{
                        gameEngine.setGame();
                        Integer roundsSet = gameEngine.getCurrRound();
                        Platform.runLater(()->roundsleft.setValue(roundsSet));
                        Platform.runLater(()->{if (currTerritoryId != 0) reShowTerritory(false, Color.WHITE);});
        });
        Platform.runLater(()->gameUX.setGameBoard(gameEngine.getBoard().getRows(),gameEngine.getBoard().getColumns()));
        Platform.runLater(()->gameUX.clearPlayersBox());
        Platform.runLater(()->gameEngine.getPlayers().forEach(pla->gameUX.addPlayerToVbox(pla.getName())));
        Platform.runLater(()->replayEnable.setValue(false));
    }

    public void territory(){
        currTerritoryId = Integer.parseInt(args.get(1));
        showTerritory(false, Color.WHITE);
    }

    private void reShowTerritory(Boolean animate, Color prev){
        if (currTerritoryId!=0) {
            showTerritory(animate, prev);
        }
    }

    private void showTerritory(Boolean animate, Color prev){
        Platform.runLater(() -> currTerritoryInfo.setValue(gameEngine.getGroundInfo(currTerritoryId)));
        Color terColor = gameEngine.getTeritoryColor(currTerritoryId);
        gameUX.setTeritoryColor(currTerritoryId, prev, terColor, animate);

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
            Map<String, Integer> unitsRecTotal = gameEngine.turingsToRecvoerByType(currTerritoryId);
            Map<String, Integer> unitsNew = new HashMap<>();
            gameEngine.getArmy().forEach(un->unitsNew.put(un.getType(),un.getPurchase()));
            Map<String, Double> unitsRecEach = new HashMap<>();
            gameEngine.getArmy().forEach(un->unitsRecEach.put(un.getType(),un.getSinglePowerCost()));
            boostArmy = new BoostArmyUX();
            boostArmy.setNotifier(this);
            boostArmy.setRecoverSpinners(unitsRecTotal, unitsRecEach, gameEngine.getPlayerAmountOfTurings(gameEngine.getCurrTurn()));
            boostArmy.setNewUnitsSpinners(unitsNew, gameEngine.getPlayerAmountOfTurings(gameEngine.getCurrTurn()));
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
        else if(currTerritoryId == 0){
            popMessage("Press one of the territories first!");
        }
        else {
            if (gameEngine.getPlayers().get(gameEngine.getCurrTurn()).getConqueredTeritoriesIds().size() == 0){
                popMessage("Must select one of the outer territories!");
            }
            else{
                popMessage("This territory is not neighboured to one of your territories! ");
            }
        }
    }

    public void undo(){
        runCommand(()-> {
            if (gameEngine.isGameSet()) {
                if (GameEngine.gameState.size() > 0) {
                    //gameEngine = (GameEngine) GameEngine.getLastGameState().clone();
                    gameEngine = (GameEngine) GameEngine.gameState.get(GameEngine.gameState.size() - 2).clone();
                    GameEngine.gameState.remove(GameEngine.gameState.size() - 1);
                    //GameEngine.gameState.remove(GameEngine.gameState.size() - 2);
                    Platform.runLater(() -> reShowTerritory(false, Color.WHITE));
                    Platform.runLater(() -> reShowPlayerInfo());
                    Platform.runLater(() -> roundsleft.setValue(gameEngine.getCurrRound()));
                    Platform.runLater(() -> replacePlayersButtons());
                } else {
                    popMessage("No rounds played yet. Undo wasn't performed.");
                }
            } else {
                popMessage("Must start new game first.");
            }
        });
        popMessage("Undo succesfully done.");
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

    private void getArmy() throws InterruptedException{
        if(chooseArmy.getTotalTurings() <= gameEngine.getPlayerAmountOfTurings(gameEngine.getCurrTurn())) {
            Map<String, Integer> army = chooseArmy.getArmy();
            StringBuilder result = new StringBuilder();
            Color prev = gameEngine.getTeritoryColor(currTerritoryId);

            if (args.get(1).compareTo("lottery") == 0) {
                result.append(gameEngine.playerConquer(currTerritoryId, army, Player.AttackingMethod.LOTTERY));
            } else if (args.get(1).compareTo("determinist") == 0) {
                result.append(gameEngine.playerConquer(currTerritoryId, army, Player.AttackingMethod.CALCULATED));
            }
            inRoundAction.setValue(false);
            runCommand(() -> {
                if (gameUX.getEnableAnimate()) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                }
                reShowTerritory(true, prev);
                if (gameUX.getEnableAnimate()) {
                    try {
                        Thread.sleep(2000);
                    } catch (Exception e) {
                    }
                }
                Platform.runLater(() -> nextPlayer());
                Platform.runLater(() -> popMessage(result.toString()));
            });
        }
        else{
            popMessage("Player don't have enough turings. Purchase wasn't made");
            inRoundAction.setValue(false);
            nextPlayer();
        }
    }

    private void noActionOnChooseWindow(){
        inRoundAction.setValue(false);
    }

    private void getBoostArmy(){
        if (boostArmy.getTotalTurings() <= gameEngine.getPlayerAmountOfTurings(gameEngine.getCurrTurn())) {
            Map<String, Integer> armyNew = boostArmy.getArmyNew();
            Map<String, Integer> armyBoost = boostArmy.getArmyBoost();
            armyBoost.keySet().forEach(un -> gameEngine.addPower(gameEngine.getPlayerName(gameEngine.getCurrTurn()), currTerritoryId, un, armyBoost.get(un)));
            gameEngine.addNewUnits(gameEngine.getPlayerName(gameEngine.getCurrTurn()), currTerritoryId, armyNew);
            inRoundAction.setValue(false);
        }
        else {
            popMessage("Player don't have enough turings. Purchase wasn't made");
        }

        nextPlayer();
    }

    private void nextPlayer() {
        if (!gameEngine.isGameOver()) {
            gameEngine.nextTurn();
            if (gameEngine.getCurrTurn() == gameEngine.getPlayers().size()) {
                gameUX.clearPlayersButtonsColor();
                inRound.setValue(false);
                roundsleft.setValue(gameEngine.getCurrRound());
                if (gameEngine.isGameOver()) endGame();
            } else {
                gameUX.paintPlayerButton(gameEngine.getCurrTurn(), gameEngine.getPlayerColor(gameEngine.getCurrTurn()));
                inRound.setValue(true);
            }
        }
        else{
            announceWinner();
        }
        reShowTerritory(false, Color.WHITE);
        reShowPlayerInfo();
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

    private void replayShowArmy(){
        try {
            GameEngine currGameEngine = getState();
            showArmyUX = new ShowArmyUX();
            ArrayList<ArrayList<String>> details = currGameEngine.getArmyDetails(replayUX.getCurrTerritory());
            showArmyUX.setTable(details);
            showArmyUX.setTotalPowerLabel(currGameEngine.getTeritoryTotalArmyPower(replayUX.getCurrTerritory()));
            showArmyUX.setTuringsToRecoverLabel(currGameEngine.getTeritoryMissingTuringsToRecover(replayUX.getCurrTerritory()));
            showArmyUX.launchStage();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void forefit(){
        gameEngine.getPlayerTeritoryIds(gameEngine.getCurrTurn()).forEach(terId->gameUX.setTeritoryColor(terId, Color.WHITE, Color.WHITE, false));
        gameEngine.removePlayer();
        if (gameEngine.getPlayers().size() == 1){
            endGame();
        }

        replacePlayersButtons();
        nextPlayer();
    }

    private void endGame(){
        gameEngine.endGame();
        roundsleft.setValue(gameEngine.getCurrRound());
        inRound.setValue(false);
        replayEnable.setValue(true);
        gameUX.clearPlayersButtonsColor();
        announceWinner();
    }

    private void announceWinner(){
        StringBuilder announcment = new StringBuilder("Match results: ");
        if (gameEngine.getPlayers().size() == 1){
            announcment.append("Technical win to " + gameEngine.getPlayers().get(0).getName());
        }
        else {
            if (gameEngine.getLeader().getKey().substring(0, 3).equals("Tie")) {
                announcment.append(gameEngine.getLeader().getKey());
            } else {
                announcment.append(gameEngine.getLeader().getKey() + " won.");
            }
            announcment.append(System.lineSeparator());
            announcment.append("Total profit from conquered territories: " + gameEngine.getLeader().getValue().toString());
        }

        runCommand(() -> {
            if (!gameEngine.getLeader().getKey().substring(0, 3).equals("Tie")) {
                gameUX.glowWinnersTerritories(gameEngine.findPlayer(gameEngine.getLeader().getKey()).getConqueredTeritoriesIds());
                if (gameUX.getEnableAnimate()) {
                    try {
                        Thread.sleep(2000);
                    } catch (Exception e) {
                    }
                }
            }
            Platform.runLater(() -> popMessage(announcment.toString()));
        });
    }

    private void playerInfo(){
        //String playerName = args.get(1);
        //currPlayerInfo.setValue(gameEngine.findPlayer(playerName).toString());
        currPlayerInfo.setValue(gameEngine.findPlayer(args.get(1)).toString());
    }

    private void reShowPlayerInfo(){
        try {
            String playerName = currPlayerInfo.getValue().substring(0, currPlayerInfo.getValue().indexOf(System.lineSeparator()));
            currPlayerInfo.setValue(gameEngine.findPlayer(playerName).toString());
        }
        catch (NullPointerException|StringIndexOutOfBoundsException e){
            currPlayerInfo.setValue("");
        }
    }

    private void replay() throws Exception{
        replayUX = new ReplayUX(GameEngine.gameState.size());
        replayUX.setObserver(this);
        replayUX.setNextButton(false);
        getState().getPlayers().forEach(pla->replayUX.addPlayerToVbox(pla.getName(), pla.getColor()));
        replayUX.setGameBoard(gameEngine.getBoard().getRows(), gameEngine.getBoard().getColumns());
        paintTerritoriesAccordingToState();
        replayUX.launchLoader();
    }

    private GameEngine getState(){
        return GameEngine.getState(replayUX.getCurrState());
    }

    private void changeState(){
        if (getState() != null) {
            paintTerritoriesAccordingToState();
            replayPlayerInfo();
            replayTerritory();
            replayUX.clearPlayersVbox();
            getState().getPlayers().forEach(pla -> replayUX.addPlayerToVbox(pla.getName(), pla.getColor()));
        }

    }

    private void paintTerritoriesAccordingToState(){
        GameEngine currEngine = getState();
        currEngine.getBoard().getBoard().forEach(row->{
            row.forEach(cell->{replayUX.paintCell(currEngine.getTeritoryColor(cell.getId()), cell.getId());});
        });
    }

    private void replayPlayerInfo(){
        if (replayUX.getCurrPlayerName() != null) {
            GameEngine currState = getState();
            replayUX.setPlayerInfo(currState.findPlayer(replayUX.getCurrPlayerName()).toString());
        }
    }

    private void replayTerritory(){
        if (replayUX.getCurrTerritory() != null) {
            GameEngine currState = getState();
            replayUX.setTerritoryInfo(currState.getGroundInfo(replayUX.getCurrTerritory()));
        }
    }

    private void replacePlayersButtons(){
        gameUX.clearPlayersBox();
        gameEngine.getPlayers().forEach(pla->gameUX.addPlayerToVbox(pla.getName()));
    }

    private void bindProperties(){
        gameUX.bindPlayerInfo(currPlayerInfo);
        gameUX.bindTeritoryInfo(currTerritoryInfo);
        gameUX.bindInGameButtonsAccess(gameEngine.getValid().not().or(gameEngine.getGameSet()));
        gameUX.bindLoadMenuButtonsAccess(gameEngine.getGameSet().and(gameEngine.getGameSet()));
        gameUX.bindInRoundButton(gameEngine.getGameSet().not().or(inRound));
        gameUX.bindRoundActionButtons(inRound.not().or(inRoundAction));
        gameUX.bindRoundsLeftLabel(roundsleft.add(0));
        gameUX.bindToDisableForefitButton(inRound.not());
        gameUX.bindEndGameButton(gameEngine.getGameSet().not());
        gameUX.bindDisableReplay(replayEnable.not());
    }
}
