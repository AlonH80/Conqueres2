import generated.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class TeritoryUnit implements Cloneable, Serializable {
    public static final Integer DEFAULT_ARMY_THRESHOLD=100;
    public static final Integer DEFAULT_PROFIT=DEFAULT_ARMY_THRESHOLD/2;
    public static Set<Integer> otherUnitIds = new HashSet<>();
    private ArrayList<ArmyUnit> armyOnGround;
    private String conqueror;
    private Boolean conquerAble;
    protected Integer profit;
    protected Integer armyThreshold;
    protected Integer id;

    public TeritoryUnit() {
        armyOnGround=new ArrayList<>();
        conqueror = null;
        conquerAble = true;
    }

    public TeritoryUnit(Integer profit, Integer armyThreshold) {
        armyOnGround=new ArrayList<>();
        this.profit = profit;
        this.armyThreshold = armyThreshold;
        //setId(generateNextId());
        conqueror = null;
        conquerAble = true;
    }

    public TeritoryUnit(Integer id) {
        armyOnGround=new ArrayList<>();
        setId(id);
    }

    public TeritoryUnit(Teritory ter){
        armyOnGround=new ArrayList<>();
        setId(ter.getId().intValue());
        armyThreshold=ter.getArmyThreshold().intValue();
        profit=ter.getProfit().intValue();
        conquerAble=true;
        conqueror=null;
    }

    public void validateTeritory(){
        if (conquerAble==null)
            conquerAble=true;
        if (profit==null)
            profit=DEFAULT_PROFIT;
        if (armyThreshold==null)
            armyThreshold=DEFAULT_ARMY_THRESHOLD;
        if (id==null)
            setId();
    }

    private Integer generateNextId() {
        Integer tempId = 1;
        while (otherUnitIds.contains(tempId)) {
            ++tempId;
        }
        return tempId;
    }

    public int getProfit() {
        return profit;
    }

    public void setProfit(int value) {
        this.profit = value;
    }

    public int getArmyThreshold() {
        return armyThreshold;
    }

    public void setArmyThreshold(int value) {
        this.armyThreshold = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer value)  {
        if (otherUnitIds.contains(value)) {
            value = generateNextId();
        }
        this.id = value;
        otherUnitIds.add(value);
    }

    public void setId() {
        setId(generateNextId());
    }

    public boolean isConquered() {
        return conqueror != null;
    }

    public void setConqueror(Player conq) {
        /*if (conqueror != null) {
            conqueror.removeConqueredUnit(this);
        }*/
        if (conq != null) {
            conq.addConqueredUnit(this);
            conqueror = conq.getName();
        }
        else {
            conqueror = null;
        }
    }

    public String  getConqueror() {
        return conqueror;
    }

    public void setConquerAble(Boolean conquerAble) {
        this.conquerAble = conquerAble;
        if (!conquerAble) {
            armyThreshold = 0;
            //profit=DEFAULT_PROFIT;
        }
    }

    public Boolean getConquerAble() {
        return conquerAble;
    }

    public ArrayList<ArmyUnit> getArmyOnGround() {
        return armyOnGround;
    }

    public void setArmyOnGround(ArrayList<ArmyUnit> armyOnGround) {
        this.armyOnGround = armyOnGround;
    }

    public void clearArmy() {
        armyOnGround.clear();
    }

    public Integer totalArmyPower() {
        return ArmyUnit.getTotalArmyPower(armyOnGround);
    }

    public boolean isEnoughPowerToRule() {
        return totalArmyPower() >= armyThreshold;

    }

    public boolean isArmyUnitExist(ArmyUnit someUnit) {
        return armyOnGround.contains(someUnit);
    }

    public Integer addPowerToUnit(ArmyUnit someUnit, Integer powerToAdd) {
        Integer cost = 0;
        if (isArmyUnitExist(someUnit)) {
            if (someUnit.getPower() + powerToAdd > someUnit.getMaxFirePower()) {
                powerToAdd = someUnit.getMaxFirePower() - someUnit.getPower();
                someUnit.addPower(powerToAdd);
            } else {
                someUnit.addPower(powerToAdd);
            }
        } else {
            powerToAdd = 0;
        }
        cost += (int) ((double) powerToAdd * someUnit.getSinglePowerCost());
        return cost;
    }

    public Integer addNewUnit(ArmyUnit someUnit) {
        armyOnGround.add(someUnit);
        return someUnit.getPurchase();
    }

    public void addArmy(ArrayList<ArmyUnit> newArmy) {
        armyOnGround.addAll(newArmy);
    }

    public boolean isUnitAlive(ArmyUnit someUnit) {
        if (!isArmyUnitExist(someUnit)) {
            return false;
        }
        Boolean retVal = someUnit.isUnitAlive();
        if (!retVal)
            armyOnGround.remove(someUnit);
        return retVal;
    }

    public void reducePowerToAllUnits(Double fractionToDecrease) {
        ArmyUnit.reducePowerToAllUnits(armyOnGround,fractionToDecrease);
    }

    public void roundUp() {
        ArmyUnit.roundUpArmy(armyOnGround);
    }

    public String showDetails(Boolean showUnits, ArrayList<ArmyUnit> armyTypes) {
        String returnString = "Teritory ID: " + id +
                System.lineSeparator() + "Army threshold: " + armyThreshold +
                System.lineSeparator() + "Profit: " + profit + System.lineSeparator() + "Conqueror: ";
        if (isConquered())
            returnString += conqueror;
        else
            returnString += "None";
        returnString += System.lineSeparator();
        if (!conquerAble)
            returnString += "Not conquerable" + System.lineSeparator();
        if (showUnits)
            returnString += ArmyUnit.showArmyByType(armyOnGround,armyTypes);
        return returnString;
    }

    public Integer getMissingTuringsToRecoverUnitType(String unitType){
        return armyOnGround.stream().filter(un->un.getType().compareTo(unitType)==0).
                mapToInt(un->(int)((un.getMaxFirePower()-un.getPower())*un.getSinglePowerCost())).sum();

    }

    public Integer getMissingTuringsToRecoverArmy(ArrayList<ArmyUnit> possibleTypes){
        int totalTuringsMiss = 0;
        for (ArmyUnit unitType:possibleTypes){
            totalTuringsMiss += getMissingTuringsToRecoverUnitType(unitType.getType());
        }
        return totalTuringsMiss;
    }

    public static TeritoryUnit findTeritory(ArrayList<TeritoryUnit> teritories,Integer terId){
        for (TeritoryUnit ter:teritories){
            if (ter.getId()==terId.intValue()){
                return ter;
            }
        }
        return null;
    }

    public static ArrayList<Integer> findDuplicates(Territories ters){
        ArrayList<Integer> dups=new ArrayList<>();
        ArrayList<Integer> checked=new ArrayList<>();
        for (Teritory ter:ters.getTeritory()){
            if (checked.contains(ter.getId().intValue())){
                dups.add(ter.getId().intValue());
            }
            else{
                checked.add(ter.getId().intValue());
            }
        }
        return dups;
    }

    @Override
    public Object clone(){
        TeritoryUnit cloneTer = new TeritoryUnit();
        armyOnGround.forEach(un->cloneTer.armyOnGround.add((ArmyUnit)un.clone()));
        cloneTer.conquerAble = conquerAble;
        cloneTer.profit = profit;
        cloneTer.armyThreshold = armyThreshold;
        cloneTer.id = id;
        cloneTer.conqueror = null;
        cloneTer.conqueror = conqueror;
        return cloneTer;
    }

    @Override
    public boolean equals(Object ter){
        if (!(ter instanceof TeritoryUnit)){
            return false;
        }

        return id == ((TeritoryUnit) ter).getId();
    }
}
