import generated.*;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class Player implements Cloneable, Serializable {
    public static final Double UNDERDOG_WINNING_FACTOR=0.5;
    public static Integer numOfPlayers=0;

    private Integer id;
    private String name;
    private ArrayList<TeritoryUnit> conqueredTeritories;
    private Integer turings;
    private Color color;

    public Player(){
        conqueredTeritories=new ArrayList<>();
    }

    public Player(Integer ID,String name, Color color){
        conqueredTeritories=new ArrayList<>();
        setName(name);
        setColor(color);
    }

    public void setId(Integer id){
        this.id = id;
    }

    public Integer getId(){
        return id;
    }

    public void setName(String userName){
        ++numOfPlayers;
        name=new String();
        if (userName.length()>=1) {
            this.name = userName;
        }
        else {
            setName();
        }
    }

    public void setName(){
        name="Player"+((numOfPlayers).toString());
    }

    public String getName(){
        return name;
    }

    public void setColor(Color color){
        this.color = color;
    }

    public Color getColor(){
        return color;
    }

    public ArrayList<TeritoryUnit> getConqueredTeritories(){
        return conqueredTeritories;
    }

    public ArrayList<Integer> getConqueredTeritoriesIds(){
        ArrayList<Integer> ids=new ArrayList<>();
        conqueredTeritories.forEach(t->ids.add(t.getId()));
        return ids;
    }

    public void setTurings(Integer turings){
        this.turings=turings;
    }

    public Integer getTurings(){
        return turings;
    }

    public boolean addDefenceUnits(TeritoryUnit groundUnit,ArrayList<ArmyUnit> units){
        if (conqueredTeritories.contains(groundUnit)) {
            Integer cost = units.stream().mapToInt(ArmyUnit::getPurchase).sum();
            if (cost <= turings) {
                for (ArmyUnit unit : units) {
                    turings -= groundUnit.addNewUnit(unit);
                }
                return true;
            }
        }
        return false;
    }

    public boolean powerUpUnit(ArmyUnit unit,Integer powerToGive){
        if (unit.getPower()+powerToGive>unit.getMaxFirePower()) {
            powerToGive = unit.getMaxFirePower() - unit.getPower();
        }
        if (powerToGive*unit.getSinglePowerCost()<=turings && powerToGive>0) {
            unit.addPower(powerToGive);
            turings -= (int)((double)powerToGive*unit.getSinglePowerCost());
            return true;
        }
        return false;
    }

    public String takeOverTeritory(TeritoryUnit ter,ArrayList<ArmyUnit> defGroundUnits){
        if (ArmyUnit.getTotalArmyPower(defGroundUnits) >= ter.getArmyThreshold()) {
            ter.setConqueror(this);
            ter.setArmyOnGround(defGroundUnits);
            return (name+" conquered territory "+ter.getId());
        }
        else{
            refundUnits(defGroundUnits);
        }
        return (name+" wasn't able to conquer territory "+ter.getId()+" (not enough power: need at least "+ter.getArmyThreshold()+", has "+ArmyUnit.getTotalArmyPower(defGroundUnits)+").");
    }

    public String attackTeritory(TeritoryUnit teritory,ArrayList<ArmyUnit> attackUnit){
        Integer teritoryArmyPower=teritory.totalArmyPower();
        Integer attackPower=ArmyUnit.getTotalArmyPower(attackUnit);
        Integer lottery=new Random().nextInt(teritoryArmyPower+attackPower);
        String result=new String();
        result="Teritory unit power: "+teritoryArmyPower+", Attack power: "+attackPower+", Lottery: "+lottery+System.lineSeparator();
        if (lottery-teritoryArmyPower>=0){  // Attacker won
            teritory.clearArmy();
            result+="Attacker won.";
            if (attackPower>teritoryArmyPower) {
                ArmyUnit.reducePowerToAllUnits(attackUnit,((double)teritoryArmyPower)/attackPower);
            }
            else{
                ArmyUnit.reducePowerToAllUnits(attackUnit,UNDERDOG_WINNING_FACTOR);
            }

            if (ArmyUnit.getTotalArmyPower(attackUnit)>=teritory.getArmyThreshold()){
                teritory.setConqueror(this);
                teritory.setArmyOnGround(attackUnit);

            }
            else{
                teritory.setConqueror(null);
                result+=" however, his army doesn't have enough power to rule.";
                refundUnits(attackUnit);
            }
        }
        else{ // Ground unit won
            result+="The army in the teritory succeed to defend it! ";
            if (teritoryArmyPower>attackPower) {
                teritory.reducePowerToAllUnits(((double)attackPower / teritoryArmyPower));
            }
            else{
                teritory.reducePowerToAllUnits(UNDERDOG_WINNING_FACTOR);
            }
            if(!teritory.isEnoughPowerToRule()){
                teritory.setConqueror(null);
                result+="However, the army doesn't have enough power to rule.";
            }
        }
        return result;
    }

    public Integer refundUnits(ArrayList<ArmyUnit> units){
        Integer turingRefunds=0;
        for (ArmyUnit unit:units){
            turings+=(int)((double)unit.getPower()*unit.getSinglePowerCost());
            turingRefunds+=(int)((double)unit.getPower()*unit.getSinglePowerCost());
        }
        return turingRefunds;
    }

    public void roundUp(){
        ArrayList<TeritoryUnit> tersToRemove=new ArrayList<>();
        conqueredTeritories.forEach(ter->{turings+=ter.getProfit(); ter.roundUp();
            if (!ter.isEnoughPowerToRule()) {
                tersToRemove.add(ter);
            }});
        for (TeritoryUnit ter:tersToRemove){
            ter.setConqueror(null);
            ter.clearArmy();
            conqueredTeritories.remove(ter);
        }
    }

    public Integer getPowerUnitMissingCost(String unitType,Integer teritoryToReinforceId){
        Integer totalMissing=0;
        TeritoryUnit ter=findTeritory(teritoryToReinforceId);
        for (ArmyUnit unit:ter.getArmyOnGround()){
            if (unit.getType().compareTo(unitType)==0){
                totalMissing+=(int)((unit.getMaxFirePower()-unit.getPower())*unit.getSinglePowerCost());
            }
        }
        return totalMissing;
    }

    public void addPowerWidthwise(String unitType,Integer amountOfTurings,TeritoryUnit ter){
        Boolean noChange=false;
        Integer reduceAmount;
        while (!noChange) {
            noChange=true;
            for (ArmyUnit unit : ter.getArmyOnGround()) {
                if (amountOfTurings > 0  && unit.getType().equals(unitType)) {
                    reduceAmount = ter.addPowerToUnit(unit, 2);
                    amountOfTurings-=reduceAmount;
                    turings -= reduceAmount;
                    if (reduceAmount>0)
                        noChange=false;
                }
            }
        }
    }

    public static Integer calculateUnitsPower(ArrayList<ArmyUnit> units){
        /*Integer totPower=0;
        for (ArmyUnit unit:units){
            totPower+=unit.getPower();
        }
        return totPower;*/
        Integer totPower = units.stream().mapToInt(ArmyUnit::getPower).sum();
        return totPower;
    }

    @Override
    public String toString() {
        StringBuilder retString  = new StringBuilder(new String());
        retString.append(name+System.lineSeparator());
        retString.append("Turings: "+turings.toString()+System.lineSeparator());
        retString.append("Conquered territories: "+System.lineSeparator());
        conqueredTeritories.forEach(ter->retString.append(ter.getId().toString()+" "));
        retString.append(System.lineSeparator());
        return retString.toString();
    }

    /*public String showDetails(){
        StringBuilder returnString = new StringBuilder(new String());
        returnString.append("### "+name+" ###"+System.lineSeparator());
        returnString.append("Turings before this round: ").append(GameEngine.getLastGameState().getPlayerAmountOfTurings(name).toString()).append(System.lineSeparator());
        returnString.append("Turings after get profit from conquered units: ").append(getTurings().toString()).append(System.lineSeparator());
        returnString.append("Territories:"+System.lineSeparator());
        if (conqueredTeritories.size()>0) {
            for (TeritoryUnit ter : conqueredTeritories) {
                returnString.append(ter.showDetailsForConqueror());
            }
        }
        else{
            returnString.append("Not ruling any territory."+System.lineSeparator());
        }
        return returnString.toString();
    }*/

    /*public String showDetailsAfterAction(){
        StringBuilder returnString = new StringBuilder(new String());
        returnString.append("### "+name+" ###"+System.lineSeparator());
        returnString.append("Turings: ").append(getTurings().toString()).append(System.lineSeparator());
        returnString.append("Territories:"+System.lineSeparator());
        if (conqueredTeritories.size()>0) {
            for (TeritoryUnit ter : conqueredTeritories) {
                returnString.append(ter.showDetailsForConqueror());
            }
        }
        else{
            returnString.append("Not ruling any territory."+System.lineSeparator());
        }
        return returnString.toString();
    }*/

    // For debugging
    /*public String showDetailsGameDescriptor(){
        StringBuilder retString = new StringBuilder(new String());
        retString.append("### "+name+" ###"+System.lineSeparator());
        retString.append("Number of conquered territories: "+conqueredTeritories.size());
        retString.append(System.lineSeparator()+"Amount of turings: "+turings);
        return retString.toString();
    }*/

    private TeritoryUnit findTeritory(Integer terId) {
        return TeritoryUnit.findTeritory(conqueredTeritories,terId);
    }

    public void addConqueredUnit(TeritoryUnit ter){
        conqueredTeritories.add(ter);
    }

    public void removeConqueredUnit(TeritoryUnit ter){
        conqueredTeritories.remove(ter);
    }

    public ArrayList<ArmyUnit> buyArmy(ArrayList<ArmyUnit> cart){
        cart.forEach(un->turings-=un.getPurchase());
        return cart;
    }

    public Integer getTotalProfitFromConqueredUnits(){
        Integer totalProfit=0;
        for (TeritoryUnit conqTer:conqueredTeritories){
            totalProfit+=conqTer.getProfit();
        }
        return totalProfit;
    }

    public static ArrayList<Integer> findDuplicates(Players plas){
        ArrayList<Integer> dups=new ArrayList<>();
        ArrayList<Integer> checked=new ArrayList<>();
        for (generated.Player pla:plas.getPlayer()){
            if (checked.contains(pla.getId().intValue())){
                dups.add(pla.getId().intValue());
            }
            else{
                checked.add(pla.getId().intValue());
            }
        }
        return dups;
    }

    @Override
    public Object clone() {
        Player clonePlayer = new Player();
        clonePlayer.name = name;
        clonePlayer.turings = turings;
        //conqueredTeritories.forEach(ter->clonePlayer.conqueredTeritories.add(((TeritoryUnit)ter.clone())));
        return clonePlayer;
    }

    @Override
    public boolean equals(Object player){
        if (!(player instanceof Player)){
            return false;
        }

        return name.equals(((Player) player).getName());
    }
}
