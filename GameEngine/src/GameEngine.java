import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


import generated.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import javax.xml.bind.JAXBException;

public class GameEngine implements Cloneable, Serializable {

    public static ArrayList<GameEngine> gameState = new ArrayList<>();

    private final Integer DEFAULT_TOTAL_CYCLES=20;
    private final Integer DEFAULT_ROWS=5;
    private final Integer DEFAULT_COLS=5;
    public static Color[] colors = {Color.BLUE,Color.RED,Color.GREEN,Color.YELLOW};

    private ArrayList<Player> players;
    private Integer currRound;
    private ArrayList<String> boardHistory;
    private AbstractMap.SimpleEntry<String, Integer> leader;
    protected GameBoard board;
    protected int initialFunds;
    protected ArrayList<ArmyUnit> army;
    protected ArrayList<TeritoryUnit> territories;
    protected Integer totalCycles;
    protected Integer defaultArmyThreshold;
    protected Integer defaultProfit;
    protected transient BooleanProperty valid;
    protected Boolean validBool;
    protected transient BooleanProperty gameSet;
    protected Boolean gameSetBool;
    protected Integer currTurn;
    protected final Integer numOfChecks = 31;
    protected Integer checks = 0;
    protected transient DoubleProperty loadProg;

    public GameEngine(){
        players=new ArrayList<>(2);
        currRound = -1;
        boardHistory = new ArrayList<>();
        board=new GameBoard();
        territories=new ArrayList<>();
        army=new ArrayList<>();
        valid = new SimpleBooleanProperty(false);
        validBool = false;
        gameSet = new SimpleBooleanProperty(false);
        gameSetBool = false;
        loadProg = new SimpleDoubleProperty(0);
        leader = new AbstractMap.SimpleEntry<>("", 0);
    }

    public GameBoard getBoard() {
        return board;
    }

    public ArrayList<Player> getPlayers(){
        return players;
    }

    public void setGame() {
        setBoard();
        currRound = totalCycles;
        gameSet.setValue(true);
        gameSetBool = true;
        boardHistory.add(board.toString());
        leader = new AbstractMap.SimpleEntry<>("", 0);
        currTurn = players.size();
        players.forEach(p-> {
            if (p!=null) p.getConqueredTeritories().clear();
            p.setTurings(initialFunds);
        });
        gameState.clear();
        gameState.add((GameEngine)this.clone());
    }

    public BooleanProperty getGameSet(){
        return gameSet;
    }

    public Boolean isGameSet(){return gameSet.getValue();}

    public int getInitialFunds() {
        return initialFunds;
    }

    public void setInitialFunds(int value) {
        this.initialFunds = value;
    }

    public Integer getTotalCycles() {
        return totalCycles;
    }

    public void setTotalCycles(Integer value) {
        this.totalCycles = value;
    }

    public Integer getCurrRound(){
        return currRound;
    }

    public ArrayList<ArmyUnit> getArmy(){
        return army;
    }

    public ArrayList<Integer> getTerritoriesIds(){
        ArrayList<Integer> terIds=new ArrayList<>();
        for (ArrayList<TeritoryUnit> terList:board.getBoard()){
            terList.forEach(t->terIds.add(t.getId()));
        }
        return terIds;
    }

    public Player findPlayer(String playerName){
        if (playerName!=null) {
            for (Player player : players) {
                if (player.getName().compareTo(playerName) == 0) {
                    return player;
                }
            }
        }
        return null;
    }

    public String playerConquer(Integer territoryId,Map<String,Integer> attackingForce, Player.AttackingMethod attackingMethod){
        Player conqueror = players.get(currTurn);
        TeritoryUnit teritory = board.findObject(territoryId);
        ArrayList<ArmyUnit> attackingUnits = playerBuyUnits(conqueror.getName(),attackingForce);
        String retVal="";
        if (teritory != null && teritory.isConquered()){
            Player prevConqueror = findPlayer(teritory.getConqueror());
            retVal=conqueror.attackTeritory(teritory,attackingUnits, attackingMethod);
            if (teritory.getConqueror()==null || teritory.getConqueror().compareTo(prevConqueror.getName()) != 0){
                prevConqueror.removeConqueredUnit(teritory);
                if (teritory.getConqueror() == null){
                    prevConqueror.refundUnits(teritory.getArmyOnGround());
                    teritory.clearArmy();
                }
            }
        }
        else {
            retVal=conqueror.takeOverTeritory(teritory,attackingUnits);
        }
        return retVal;
    }

    public void finishRound(){
        --currRound;
        isGameOver();
        boardHistory.add(board.toString());
        setLeader();
        gameState.add((GameEngine) this.clone());
    }

    public ArrayList<Integer> getPlayerTeritorysIds(String playerName){
        Player player=findPlayer(playerName);
        return player.getConqueredTeritoriesIds();
    }

    public ArrayList<Integer> getPlayerTeritoryIds(Integer ind){
        return getPlayerTeritorysIds(players.get(ind).getName());
    }

    public Integer powerUnitMissingCost(String playerName,Integer teritoryToReinforce,String unitType){
        Player player=findPlayer(playerName);
        return player.getPowerUnitMissingCost(unitType,teritoryToReinforce);
    }

    public Map<String,Integer> turingsToRecvoerByType(Integer teritoryToReinforce){
        TeritoryUnit teritory = board.findObject(teritoryToReinforce);
        Map<String,Integer> turingsToRecoverMap = new HashMap<>(army.size());
        army.forEach(un->turingsToRecoverMap.put(un.getType(),teritory.getMissingTuringsToRecoverUnitType(un.getType())));
        return turingsToRecoverMap;
    }

    public Double getSinglePowerCost(String unitType){
        return ArmyUnit.findNextUnit(army,unitType).getSinglePowerCost();
    }

    public Integer getUnitCost(String unitType){
        return ArmyUnit.findNextUnit(army,unitType).getPurchase();
    }

    public void addPower(String playerName,Integer unitToReinforce,String unitType,Integer amountOfTurings){
        Player player=findPlayer(playerName);
        player.addPowerWidthwise(unitType,amountOfTurings,board.findObject(unitToReinforce));
    }

    public boolean isConquered(Integer terId){
        return (board.findObject(terId).isConquered());
    }

    public String getPlayerName(Integer playerInd){ // 1 to return player1Name, 2 to return player2Name
        return players.get(playerInd).getName();
    }

    public String getGroundInfo(Integer terId){
        TeritoryUnit ter=board.findObject(terId);
        if (ter!=null) {
            return ter.showDetails(army);
        }
        return "";
    }

    public Integer getPlayerAmountOfTurings(String playerName){
        Player player=findPlayer(playerName);
        return player.getTurings();
    }

    public Integer getPlayerAmountOfTurings(Integer ind){
        Player player = players.get(ind);
        return player.getTurings();
    }

    public Map<Integer, Boolean> getAvailableTeritoryToConquer(){
        Player player = players.get(currTurn);
        Map<Integer, Boolean> availableGrounds=new HashMap<>();
        ArrayList<TeritoryUnit> conqueredUnits=player.getConqueredTeritories();
        Map<Integer, Boolean> neighbours;
        if (!conqueredUnits.isEmpty()) {
            for (TeritoryUnit Teritory : conqueredUnits) {
                neighbours = findNeighboursTeritory(Teritory);
                if (neighbours.size() > 0) {
                    for (Map.Entry<Integer, Boolean> neigh : neighbours.entrySet()) {
                        //if (board.findObject(neigh.getKey()).getConqueror().equals(player.getName()) && board.findObject(neigh.getKey()).getConquerAble())
                        String conqueror = board.findObject(neigh.getKey()).getConqueror();
                        if (conqueror == null || !conqueror.equals(player.getName())) {
                            availableGrounds.put(neigh.getKey(), neigh.getValue());
                        }
                    }
                }
            }
        }
        else{
            for (int i=0;i<board.getColumns();++i){
                availableGrounds.put(board.getObject(0,i).getId(),board.getObject(0,i).isConquered());
                availableGrounds.put(board.getObject(board.getRows()-1,i).getId(),board.getObject(board.getRows()-1,i).isConquered());
            }
            for (int i=0;i<board.getRows();++i){
                availableGrounds.put(board.getObject(i,0).getId(),board.getObject(i,0).isConquered());
                availableGrounds.put(board.getObject(i,board.getColumns()-1).getId(),board.getObject(i,board.getColumns()-1).isConquered());
            }
        }
        return availableGrounds;
    }

    public Map<Integer,Boolean> findNeighboursTeritory(TeritoryUnit ter){
        Map<TeritoryUnit,GameBoard.Dirs> neighboursMap=board.findNeighbours(ter);
        Map<Integer,Boolean> neighboursIds=new HashMap<>();
        for (Map.Entry<TeritoryUnit,GameBoard.Dirs> unit:neighboursMap.entrySet()){
            neighboursIds.put(unit.getKey().getId(),unit.getKey().isConquered());
        }
        return neighboursIds;
    }

    public ArrayList<ArmyUnit> playerBuyUnits(String playerName,Map <String,Integer> unitsToBuy){
        Player player = findPlayer(playerName);
        ArrayList<ArmyUnit> cart = new ArrayList<>();
        for (String key:unitsToBuy.keySet()){
            for (int i=0;i<unitsToBuy.get(key);++i){
                cart.add(new ArmyUnit(ArmyUnit.findNextUnit(army,key)));
            }
        }
        return player.buyArmy(cart);
    }

    public Boolean isGameOver(){
        if (currRound <= 0){
            gameSet.setValue(false);
            gameSetBool = false;
            return true;
        }
        return false;
    }

    public String showWorldsMap(){
        return board.toString();
    }

    public ArrayList<String> getBoardHistory(){
        return boardHistory;
    }

    public void setTerritoriesInBoard() {
        for (TeritoryUnit ter:territories){
            ter.validateTeritory();
            board.setObject((TeritoryUnit)ter.clone(),(ter.getId()-1)/board.getColumns(),(ter.getId()-1)%board.getColumns());
        }
        while (!board.isBoardFull()){
            TeritoryUnit ter=new TeritoryUnit();
            ter.setArmyThreshold(defaultArmyThreshold);
            ter.setProfit(defaultProfit);
            ter.setId();
            ter.validateTeritory();
            territories.add(ter);
            //territories.sort();
            board.addObject((TeritoryUnit)ter.clone());
        }
    }

    public void setBoard() {
        board.setBoard();
        setTerritoriesInBoard();
    }

    public void setLeader(){
        int maxTotalProfit=-1;
        ArrayList<String> leaders = new ArrayList<>();
        String leaderString = new String();

        for (Player player:players){
            if (player.getTotalProfitFromConqueredUnits() > maxTotalProfit){
                leaders.clear();
                leaders.add(player.getName());
                maxTotalProfit = player.getTotalProfitFromConqueredUnits();
            }
            else if (player.getTotalProfitFromConqueredUnits() == maxTotalProfit){
                leaders.add(player.getName());
            }
        }
        if (leaders.size()>1){
            leaderString = "Tie between:";
            for(String le : leaders){
                leaderString +=  " " + le + ",";
            }
            leaderString = leaderString.substring(0, leaderString.length()-1);
        }
        else{
            leaderString = leaders.get(0);
        }

        leader = new AbstractMap.SimpleEntry<>(leaderString, maxTotalProfit);
    }

    public AbstractMap.SimpleEntry<String, Integer> getLeader(){
        if (leader.getKey().equals("")){
            setLeader();
        }
        return leader;
    }

    public String loadFile(String fileName){
        GameDescriptor gameDescriptor;
        if (fileName.length() > 3 && fileName.substring(fileName.length()-3).compareTo("xml")!=0){
            return "Not an xml file";
        }
        try {
            gameDescriptor = FileHandling.fileToGame(fileName);
            checkValid(gameDescriptor);
            return "File loaded successfully.";
        }
        catch(InvalidXMLException e){
            return "Invalid XML file entered: "+e.toString();
        }
        catch(FileNotFoundException e){
            return "File name wasn't found.";
        }
        catch (JAXBException e){
            return "Invald XML file entered: Invalid XML syntax."+System.lineSeparator()+
                    " XML error: "+e.getLinkedException().toString();
        }
        catch (Exception e){
            e.printStackTrace();
            return "Invalid XML file entered.";
        }
    }

    public void checkValid(GameDescriptor gameDescriptor) throws InvalidXMLException{
        checks = 0;
        updateProgress(0);
        if (gameDescriptor.getGame().getBoard().getRows() == null ||
                gameDescriptor.getGame().getBoard().getRows().intValue()<2){
            throw new InvalidXMLException("Too few rows (minimum 2).");
        }
        updateProgress(1);
        if (gameDescriptor.getGame().getBoard().getColumns() == null ||
                gameDescriptor.getGame().getBoard().getColumns().intValue()<3){
            throw new InvalidXMLException("Too few columns (minimum 3).");
        }
        updateProgress(1);
        if (gameDescriptor.getGame().getBoard().getRows().intValue()>30){
            throw new InvalidXMLException("Too much rows (maximum 30).");
        }
        updateProgress(1);
        if (gameDescriptor.getGame().getBoard().getColumns().intValue()>30){
            throw new InvalidXMLException("Too much columns (maximum 30).");
        }
        updateProgress(1);
        if (gameDescriptor.getGame().getTotalCycles()==null){
            throw new InvalidXMLException("Total cycles wasn't set.");
        }
        updateProgress(1);
        if (gameDescriptor.getGame().getTotalCycles().intValue()<=0){
            throw new InvalidXMLException("Non-positive total cycles.");
        }
        updateProgress(1);
        if (gameDescriptor.getGame().getInitialFunds()==null){
            throw new InvalidXMLException("Initial funds weren't set.");
        }
        updateProgress(1);
        if (gameDescriptor.getGame().getInitialFunds().intValue() <= 0){
            throw new InvalidXMLException("Non positive initial funds.");
        }
        updateProgress(1);
        checkValidArmy(gameDescriptor);
        updateProgress(7);
        checkValidTerritories(gameDescriptor);
        updateProgress(5);
        checkValidPlayers(gameDescriptor);
        updateProgress(2);
        board.setRows(gameDescriptor.getGame().getBoard().getRows().intValue());
        updateProgress(1);
        board.setColumns(gameDescriptor.getGame().getBoard().getColumns().intValue());
        updateProgress(1);
        totalCycles=gameDescriptor.getGame().getTotalCycles().intValue();
        updateProgress(1);
        initialFunds=gameDescriptor.getGame().getInitialFunds().intValue();
        updateProgress(1);
        defaultArmyThreshold = gameDescriptor.getGame().getTerritories().getDefaultArmyThreshold().intValue();
        updateProgress(1);
        defaultProfit = gameDescriptor.getGame().getTerritories().getDefaultProfit().intValue();
        updateProgress(1);
        army.clear();
        for(Unit un:gameDescriptor.getGame().getArmy().getUnit()){
            army.add(new ArmyUnit(un));
        }
        updateProgress(1);
        TeritoryUnit.otherUnitIds.clear();
        for (Teritory ter:gameDescriptor.getGame().getTerritories().getTeritory()){
            territories.add(new TeritoryUnit(ter));
        }
        updateProgress(1);
        Integer col = 0;
        players.clear();
        for (generated.Player pla:gameDescriptor.getPlayers().getPlayer()){
            players.add(new Player(pla.getId().intValue(),pla.getName(),colors[col++]));
            players.get(players.size()-1).setTurings(initialFunds);
        }
        updateProgress(1);
        currTurn = players.size();
        valid.setValue(true);
        validBool = true;
        gameSet.setValue(false);
        gameSetBool = false;
    }

    public void checkValidArmy(GameDescriptor gameDescriptor) throws InvalidXMLException{
        if (gameDescriptor.getGame().getArmy() == null){
            throw new InvalidXMLException("Army wasn't set.");
        }

        for (Unit un:gameDescriptor.getGame().getArmy().getUnit()){
            if (un.getType() == null) {
                throw new InvalidXMLException("Army unit type wasn't set for all units.");
            }
            if (un.getRank() == 0) {
                throw new InvalidXMLException(un.getType() + " rank wasn't set.");
            }
            if (un.getPurchase() == null) {
                throw new InvalidXMLException(un.getType() + " purchase wasn't set.");
            }
            if (un.getPurchase().intValue()<0){
                throw new InvalidXMLException(un.getType() + " has negative purchase.");
            }
            if (un.getCompetenceReduction() == null) {
                throw new InvalidXMLException(un.getType() + " competence reduction wasn't set");
            }
            if (un.getCompetenceReduction().intValue() < 0){
                throw new InvalidXMLException(un.getType() + " has negative competence reduction.");
            }
            if (un.getMaxFirePower() == null) {
                throw new InvalidXMLException(un.getType() + " max fire power wasn't set");
            }
            if (un.getMaxFirePower().intValue() < 1){
                throw new InvalidXMLException(un.getType() + " has non-positive max fire power.");
            }
        }

        ArrayList<Integer> dupIds = ArmyUnit.findDuplicatesId(gameDescriptor.getGame().getArmy());
        if (dupIds.size() > 0){
            throw new InvalidXMLException("Duplicate army rank: "+dupIds.toString());
        }

        ArrayList<String> dupNames = ArmyUnit.findDuplicatesName(gameDescriptor.getGame().getArmy());
        if (dupNames.size() > 0){
            throw new InvalidXMLException("Duplicate army names: "+dupNames.toString());
        }

        Integer missingRank = ArmyUnit.verifyInOrder(gameDescriptor.getGame().getArmy());
        if (missingRank != -1){
            throw new InvalidXMLException("Missing rank: "+missingRank.toString());
        }

    }

    public void checkValidTerritories(GameDescriptor gameDescriptor) throws InvalidXMLException{
        if (gameDescriptor.getGame().getTerritories() == null){
            throw new InvalidXMLException("Territories weren't set");
        }
        if (gameDescriptor.getGame().getTerritories().getDefaultProfit()==null){
            throw new InvalidXMLException("Default profit wasn't set.");
        }
        if (gameDescriptor.getGame().getTerritories().getDefaultProfit().intValue() < 0){
            throw new InvalidXMLException("Negative default profit.");
        }
        if (gameDescriptor.getGame().getTerritories().getDefaultArmyThreshold()==null){
            throw new InvalidXMLException("Default army threshold wasn't set.");
        }
        if (gameDescriptor.getGame().getTerritories().getDefaultArmyThreshold().intValue() < 0){
            throw new InvalidXMLException("Negative default army threshold.");
        }


        for (Teritory un:gameDescriptor.getGame().getTerritories().getTeritory()){
            if (un.getId() == null) {
                throw new InvalidXMLException("Not all teritorries ID were set.");
            }
            if (un.getId().intValue() > gameDescriptor.getGame().getBoard().getRows().intValue()*gameDescriptor.getGame().getBoard().getColumns().intValue()){
                throw new InvalidXMLException("Territory ID not in range (max: "+gameDescriptor.getGame().getBoard().getRows().intValue()*gameDescriptor.getGame().getBoard().getColumns().intValue()+
                    ", input: "+un.getId().intValue()+").");
            }
            if (un.getProfit() == null) {
                throw new InvalidXMLException("Teritory "+un.getId().toString() + " profit wasn't set.");
            }
            if (un.getProfit().intValue() < 0){
                throw new InvalidXMLException("Teritory "+un.getId().toString()+ " has negative profit.");
            }
            if (un.getArmyThreshold() == null) {
                throw new InvalidXMLException("Teritory "+un.getId().toString() + " army threshold wasn't set");
            }
            if (un.getArmyThreshold().intValue() < 0){
                throw new InvalidXMLException("Teritory "+un.getId().toString() + "has negative army threshold.");
            }
        }

        if (TeritoryUnit.findDuplicates(gameDescriptor.getGame().getTerritories()).size()>0){
            valid.setValue(false);
            validBool = false;
            throw new InvalidXMLException("Duplicate teritory IDs: "+TeritoryUnit.findDuplicates(gameDescriptor.getGame().getTerritories()).toString());
        }
    }

    public void checkValidPlayers(GameDescriptor gameDescriptor) throws InvalidXMLException{
        if (gameDescriptor.getPlayers().getPlayer().size()<2 || gameDescriptor.getPlayers().getPlayer().size()>4){
            throw new InvalidXMLException("Invalid number of players ("+gameDescriptor.getPlayers().getPlayer().size()+"), should be between 2 and 4");
        }
        ArrayList<Integer> dups = Player.findDuplicates(gameDescriptor.getPlayers());
        if (dups.size()>0){
            throw new InvalidXMLException("Duplicate player IDs: "+dups.toString());
        }
    }

    public BooleanProperty getValid(){
        return valid;
    }

    public Boolean isValid(){return valid.getValue();}

    public static GameEngine getLastGameState(){
        if (gameState.size()>0) {
            return gameState.get(gameState.size() - 1);
        }
        return null;
    }

    public Integer getArmyThreshold(Integer groundId){
        return board.findObject(groundId).getArmyThreshold();
    }

    public void addNewUnits(String playerName,Integer unitId,Map<String,Integer> unitsToBuy){
        board.findObject(unitId).addArmy(playerBuyUnits(playerName,unitsToBuy));
    }

    @Override
    public Object clone(){
        GameEngine cloneGame = new GameEngine();
        cloneGame.board = (GameBoard)board.clone();
        players.forEach(pla->cloneGame.players.add((Player)pla.clone()));
        players.forEach(pla->
                pla.getConqueredTeritories().forEach(ter->cloneGame.findPlayer(pla.getName()).addConqueredUnit(cloneGame.board.findObject(ter.getId()))));
        cloneGame.currRound = getCurrRound();
        boardHistory.forEach(bh->cloneGame.boardHistory.add(bh));
        cloneGame.leader = getLeader();
        cloneGame.initialFunds = getInitialFunds();
        army.forEach(un->cloneGame.army.add((ArmyUnit) un.clone()));
        territories.forEach(ter->cloneGame.territories.add((TeritoryUnit)ter.clone()));
        cloneGame.totalCycles = getTotalCycles();
        cloneGame.valid.setValue(isValid());
        cloneGame.validBool = this.validBool;
        cloneGame.gameSet.setValue(isGameSet());
        cloneGame.gameSetBool = gameSetBool;
        cloneGame.currTurn = this.currTurn;
        return cloneGame;
    }

    public Integer getCurrTurn(){return currTurn;}

    public void nextTurn(){
        if (!isGameOver()){
            ++currTurn;
            currTurn %= (players.size()+1);
            if (currTurn==players.size()) {
                finishRound();
                //roundUp();
            }
            else {
                if (currTurn == 0) {
                    //currTurn = players.size();
                    //gameState.add((GameEngine) this.clone());
                    currTurn = 0;
                }
                players.get(currTurn).roundUp();
            }
        }
    }

    private void updateProgress(Integer checksMade){
        checks += checksMade;
        loadProg.setValue(((double)checks)/numOfChecks);
    }

    public DoubleProperty getLoadProgress(){
        return loadProg;
    }

    public Color getPlayerColor(Integer playerInd){
        return players.get(playerInd).getColor();

    }

    public Color getTeritoryColor(Integer teritoryId){
        String conqueror = board.findObject(teritoryId).getConqueror();
        if (conqueror !=null) {
            Player player = findPlayer(conqueror);
            if (player != null) {
                return player.getColor();
            }
        }
        return Color.WHITE;
    }

    public ArrayList<ArrayList<String>> getArmyDetails(Integer terId){
        ArrayList<ArrayList<String>> details = new ArrayList<>();
        Map<String,ArrayList<ArmyUnit>> mapArmy = ArmyUnit.getArmyByType(board.findObject(terId).getArmyOnGround());
        mapArmy.keySet().forEach(k->{
            ArrayList<String> detailLine = new ArrayList<>();
            detailLine.add(mapArmy.get(k).get(0).getType());
            detailLine.add(mapArmy.get(k).get(0).getRank().toString());
            detailLine.add(mapArmy.get(k).get(0).getPurchase().toString());
            detailLine.add(mapArmy.get(k).get(0).getMaxFirePower().toString());
            detailLine.add(mapArmy.get(k).get(0).getCompetenceReduction().toString());
            detailLine.add(mapArmy.get(k).get(0).getSinglePowerCost().toString());
            detailLine.add(getAllUnitInBoardOfPlayer(
                    findPlayer(board.findObject(terId).getConqueror()),k
            ).toString());
            detailLine.add(Integer.valueOf(mapArmy.get(k).stream().mapToInt(ArmyUnit::getPower).sum()).toString());
            details.add(detailLine);
        });

        return details;
    }

    public Integer getAllUnitInBoardOfPlayer(Player player, String unitType){
        Integer totUnits = 0;
        totUnits = player.getConqueredTeritories().stream().mapToInt(
                ter -> {
                    if (ArmyUnit.getArmyByType(ter.getArmyOnGround()).get(unitType)!=null) return ArmyUnit.getArmyByType(ter.getArmyOnGround()).get(unitType).size();
                    return 0;
                }
        ).sum();
        return totUnits;
    }

    public Integer getTeritoryTotalArmyPower(Integer terId){
        return ArmyUnit.getTotalArmyPower(board.findObject(terId).getArmyOnGround());
    }

    public Integer getTeritoryMissingTuringsToRecover(Integer terId){
        return board.findObject(terId).getMissingTuringsToRecoverArmy(army);
    }

    public void removePlayer(){
        Player playerToRemove = players.get((int)currTurn);
        playerToRemove.getConqueredTeritoriesIds().forEach(terId -> {
            TeritoryUnit ter = board.findObject(terId);
            ter.setConqueror(null);
            ter.clearArmy();
        });
        players.remove((int)currTurn);
        --currTurn;
    }

    public void endGame(){
        if (currRound > 0){
            gameState.add((GameEngine)this.clone());
        }
        currRound = 0;
        isGameOver();
    }

    public static GameEngine getState(Integer ind){
        if (ind >= gameState.size()){
            return null;
        }

        return gameState.get(ind);
    }

    public void setPlayersColors(){
        for (int i = 0; i < players.size(); ++i){
            players.get(i).setColor(colors[i]);
        }
    }

    public void setPropertiesVars(){
        gameSet = new SimpleBooleanProperty(gameSetBool);
        valid = new SimpleBooleanProperty(validBool);
        if (currTurn == players.size()) {
            --totalCycles;
        }
        --currTurn;
    }
}


