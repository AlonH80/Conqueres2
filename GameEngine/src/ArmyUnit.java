import generated.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import generated.*;

public class ArmyUnit implements Cloneable, Serializable {
    public static final double DEFAULT_POWER_COST=2;
    public static final Integer DEFAULT_MAX_POWER=100;
    public static final Integer DEFAULT_PURCHASE=50;
    public static final Integer DEFAULT_REDUCTION=10;
    public static ArrayList<Integer> unitIds=new ArrayList<>();

    protected Double singlePowerCost;
    protected Integer power;
    protected Integer unitId;
    //public static String[] possibleTypes={"Soldier"};

    protected int purchase;
    protected int maxFirePower;
    protected int competenceReduction;
    protected String type;
    protected Integer rank;

    public ArmyUnit(){
        unitId=generateId();
        singlePowerCost=DEFAULT_POWER_COST;
        maxFirePower=DEFAULT_MAX_POWER;
        competenceReduction=DEFAULT_REDUCTION;
        power=DEFAULT_MAX_POWER;
        purchase=DEFAULT_PURCHASE;
        rank=1;
    }

    public ArmyUnit(ArmyUnit otherUnit){
        this.purchase=otherUnit.purchase;
        this.maxFirePower=otherUnit.maxFirePower;
        this.competenceReduction=otherUnit.competenceReduction;
        this.type=otherUnit.type;
        this.rank=otherUnit.rank;
        this.singlePowerCost= (double)this.purchase /this.maxFirePower;
        this.power=this.maxFirePower;
        unitId=generateId();
    }

    public ArmyUnit(Unit unit){
        this.maxFirePower=unit.getMaxFirePower().intValue();
        this.purchase=unit.getPurchase().intValue();
        this.competenceReduction=unit.getCompetenceReduction().intValue();
        this.type=unit.getType();
        this.rank= Integer.valueOf(unit.getRank());
        setSinglePowerCost((double)maxFirePower/purchase);
    }

    public Integer generateId(){
        Integer id=1;
        while (unitIds.contains(id)){
            ++id;
        }
        unitIds.add(id);
        return id;
    }

    public Double getSinglePowerCost() {
        return Math.round(singlePowerCost*100)/100.0d;
    }

    public void setSinglePowerCost(Double value){
        singlePowerCost=value;
    }

    public Integer getPower() {
        return power;
    }

    public void setPower(Integer value){
        power=value;
    }

    public Integer getUnitId(){
        return unitId;
    }

    public void setUnitId(Integer value){
        this.unitId=value;
    }

    public Integer getPurchase() {
        return purchase;
    }

    public void setPurchase(int value) {
        this.purchase = value;
    }

    public Integer getMaxFirePower() {
        return maxFirePower;
    }

    public void setMaxFirePower(int value) {
        this.maxFirePower = value;
    }

    public Integer getCompetenceReduction() {
        return competenceReduction;
    }

    public void setCompetenceReduction(int value) {
        this.competenceReduction = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer value) {
        this.rank = value;
    }

    public Boolean isUnitAlive(){
        return power>0;
    }

    public void roundUp(){
        power-=competenceReduction;
    }

    public void decreasePower(Integer pow){
        power-=pow;
    }

    public void addPower(Integer pow){
        power+=pow;
        power=Math.min(power,maxFirePower);
    }

    /*@Override
    public String toString(){
        return ("Unit ID: "+unitId+", Type: "+type+", Rank: "+rank+", Power: "+power+" (from "+maxFirePower+")");
    }*/

    @Override
    public Object clone(){
        ArmyUnit cloneUnit = new ArmyUnit();
        cloneUnit.singlePowerCost = singlePowerCost;
        cloneUnit.power = power;
        cloneUnit.unitId = unitId;
        cloneUnit.purchase = purchase;
        cloneUnit.maxFirePower = maxFirePower;
        cloneUnit.competenceReduction = competenceReduction;
        cloneUnit.type = type;
        cloneUnit.rank = rank;
        return cloneUnit;
    }

    @Override
    public boolean equals(Object unit){
        if (!(unit instanceof  ArmyUnit)){
            return false;
        }

        return unitId == ((ArmyUnit)unit).getUnitId();
    }

    public static int getTotalArmyPower(ArrayList<ArmyUnit> army){
        return army.stream().mapToInt(ArmyUnit::getPower).sum();
    }

    public static void reducePowerToAllUnits(ArrayList<ArmyUnit> army,Double fractionToDecrease){
        ArrayList<ArmyUnit> toRemove = new ArrayList<>();
        for (ArmyUnit un:army){
            Integer powerToDec=(int)Math.ceil(fractionToDecrease*un.getPower());
            un.decreasePower(powerToDec);
            if (!un.isUnitAlive()){
               toRemove.add(un);
            }
        }

        for (ArmyUnit un  : toRemove){
            if (army.contains(un)) {
                army.remove(un);
            }
        }
    }

    public static void roundUpArmy(ArrayList<ArmyUnit> army){
        army.forEach(ArmyUnit::roundUp);
        ArrayList<ArmyUnit> unitsToRemove=new ArrayList<>();
        for (ArmyUnit un:army){
            if (!un.isUnitAlive()){
                unitsToRemove.add(un);
            }
        }
        unitsToRemove.forEach(army::remove);
    }

    public static ArmyUnit findNextUnit(ArrayList<ArmyUnit> army,String unitType){
        for (ArmyUnit un:army){
            if (un.getType().compareTo(unitType)==0){
                return un;
            }
        }
        return null;
    }

    public static String showArmyByType(ArrayList<ArmyUnit> army, ArrayList<ArmyUnit> possibleTypes){
        String retString = new String();
        for (ArmyUnit type:possibleTypes) {
            retString += "  " + type.getType() +" - ";
            retString += "Amount of units: " + army.stream().filter(un -> un.getType().compareTo(type.getType()) == 0).count();
            retString += ", Total power: " + army.stream().filter(un -> un.getType().compareTo(type.getType()) == 0).mapToInt(un -> un.getPower()).sum();
            retString += System.lineSeparator();
        }
        return retString;
    }

    public static ArrayList<String> findDuplicatesName(generated.Army army){
        ArrayList<String> dups=new ArrayList<>();
        ArrayList<String> checked=new ArrayList<>();
        for (generated.Unit un:army.getUnit()){
            if (checked.contains(un.getType())){
                dups.add(un.getType());
            }
            else{
                checked.add(un.getType());
            }
        }
        return dups;
    }

    public static ArrayList<Integer> findDuplicatesId(generated.Army army){
        ArrayList<Integer> dups=new ArrayList<>();
        ArrayList<Integer> checked=new ArrayList<>();
        for (generated.Unit un:army.getUnit()){
            if (checked.contains(Integer.valueOf(un.getRank()))){
                dups.add(Integer.valueOf(un.getRank()));
            }
            else{
                checked.add(Integer.valueOf(un.getRank()));
            }
        }
        return dups;
    }

    public static Integer verifyInOrder(generated.Army army){
        ArrayList<Integer> ranks = new ArrayList<>();
        army.getUnit().forEach(un->ranks.add(Integer.valueOf(un.getRank())));
        ranks.sort(Integer::compareTo);
        for (int i = 0;i<ranks.size();++i){
            if (ranks.get(i) != (i+1)){
                return (i+1);
            }
        }
        return -1;
    }

    public static Map<String,ArrayList<ArmyUnit>> getArmyByType(ArrayList<ArmyUnit> army){
        Map<String, ArrayList<ArmyUnit>> armyMap = new HashMap<>();
        army.forEach(un->{
            if (!armyMap.keySet().contains(un.getType())){
                armyMap.put(un.getType(), new ArrayList<>());
            }
            armyMap.get(un.getType()).add(un);
        });

        return armyMap;
    }

    public static String findStrongestUnitType(ArrayList<ArmyUnit> army){
        Integer maxRank = 0;
        String unitName = new String();
        for (ArmyUnit unit:army){
            if (unit.getRank() > maxRank){
                maxRank = unit.getRank();
                unitName = unit.getType();
            }
        }

        return unitName;
    }

    public static String findStrongestUnitType(Map<String, ArrayList<ArmyUnit>> army){
        Integer maxRank = 0;
        String unitName = new String();
        for(String key:army.keySet()){
            if (army.get(key).get(0).getRank() > maxRank){
                unitName = key;
                maxRank = army.get(key).get(0).getRank();
            }
        }

        return unitName;
    }

    public static String showArmyDetails(Map<String, ArrayList<ArmyUnit>> army){
        StringBuilder stringBuilder = new StringBuilder();
        army.keySet().forEach(k->{
            stringBuilder.append(k + " - Number of units: " + army.get(k).size());
            stringBuilder.append(", total power: ");
            Integer totalPower = army.get(k).stream().mapToInt(v->v.getPower()).sum();
            stringBuilder.append(totalPower.toString());
            stringBuilder.append(System.lineSeparator());
        });

        return stringBuilder.toString();
    }
}
