
import generated.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class GameBoard implements Cloneable, Serializable {
    public static enum Dirs {UP,DOWN,LEFT,RIGHT};
    public static final Integer MAX_LINES_IN_BLOCK=8;
    public static final Integer MAX_CHARS_IN_BLOCK=25;
    public static final Character COL_DELIMETER='|';
    public static final Character ROW_DELIMETER='=';

    private Integer nextEmptyRow;
    private Integer nextEmptyCol;
    ArrayList<ArrayList<TeritoryUnit>> board;

    protected Integer rows;
    protected Integer columns;

    public GameBoard(){ }

    public GameBoard(Integer rows,Integer columns){
        setRows(rows);
        setColumns(columns);
        nextEmptyCol=0;
        nextEmptyRow=0;
        setBoard();
    }

    public void setBoard(){
        if (board!=null){
            board.clear();
        }
        board=new ArrayList<>(rows);
        nextEmptyCol=0;
        nextEmptyRow=0;
        for (int i=0;i<rows.intValue();++i){
            board.add(new ArrayList<>(columns.intValue()));
            for (int j=0;j<columns.intValue();++j){
                board.get(i).add(null);
            }
        }
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer value) {
        this.rows = value;
    }

    public Integer getColumns() {
        return columns;
    }

    public void setColumns(Integer value) {
        this.columns = value;
    }

    public TeritoryUnit getObject(Integer row,Integer col){
        if (row<rows.intValue() && row >= 0 && col<columns.intValue() && col >= 0)
            return board.get(row).get(col);
        return null;
    }

    public Boolean hasObject(Integer row,Integer col){
        return getObject(row,col)!=null;
    }

    public void setObject(TeritoryUnit obj,Integer row,Integer col){
        if (row<rows.intValue() && col<columns.intValue()) {
            board.get(row).set(col, obj);
            if (nextEmptyRow==row && nextEmptyCol==col)
                nextEmpty();
        }
    }

    public void addObject(TeritoryUnit obj){
        if (!isBoardFull()) {
            setObject(obj, nextEmptyRow, nextEmptyCol);
            //nextEmpty();
        }
    }

    public void nextEmpty(){
        if (!isBoardFull()){
            ++nextEmptyCol;
            if (nextEmptyCol==columns.intValue()){
                if (nextEmptyRow<rows.intValue()-1){
                    ++nextEmptyRow;
                    nextEmptyCol=0;
                }
                else if(nextEmptyRow==rows.intValue()-1){
                    ++nextEmptyRow;
                }
            }
        }
        if (nextEmptyCol<columns.intValue() && nextEmptyRow<rows.intValue()) {
            if (getObject(nextEmptyRow, nextEmptyCol) != null)
                nextEmpty();
        }
    }

    public Boolean isBoardFull(){
        return nextEmptyRow==rows.intValue() && nextEmptyCol==columns.intValue();
    }

    public Integer findObjRow(TeritoryUnit obj){
        int i=0;
        while (i<rows.intValue()){
            if (board.get(i).contains(obj))
                return i;
            ++i;
        }
        return -1;
    }

    public Integer findObjCol(TeritoryUnit obj){
        int i=0,j;
        while (i<rows.intValue()){
            j=0;
            while (j<columns.intValue()) {
                if (board.get(i).get(j).equals(obj))
                    return j;
                ++j;
            }
            ++i;
        }
        return -1;
    }

    public Map<TeritoryUnit,Dirs> findNeighbours(TeritoryUnit obj){
        Integer objRow=findObjRow(obj);
        Integer objCol=findObjCol(obj);
        Map<TeritoryUnit,Dirs> neighboursDict=new HashMap<>();
        if (objCol<columns.intValue()-1)
            neighboursDict.put(getObject(objRow,objCol+1),Dirs.RIGHT);
        if (objRow<rows.intValue()-1)
            neighboursDict.put(getObject(objRow+1,objCol),Dirs.DOWN);
        if (objCol>0)
            neighboursDict.put(getObject(objRow,objCol-1),Dirs.LEFT);
        if (objRow>0)
            neighboursDict.put(getObject(objRow-1,objCol),Dirs.UP);
        return neighboursDict;
    }

    public ArrayList<ArrayList<TeritoryUnit>> getBoard(){
        return board;
    }

    public TeritoryUnit findObject(TeritoryUnit ter){
        Integer row=findObjRow(ter);
        Integer col=findObjRow(ter);
        return getObject(row,col);
    }

    public TeritoryUnit findObject(Integer terId){
        for (int i=0;i<rows.intValue();++i){
            for (int j=0;j<columns.intValue();++j){
                if (getObject(i,j).getId() == terId.intValue())
                    return getObject(i,j);
            }
        }
        return null;
    }

    @Override
    public String toString(){
        ArrayList<ArrayList<String>> objStrings=new ArrayList<>();
        for (int i=0;i<rows.intValue();++i){
            objStrings.add(new ArrayList<>());
            for (int j=0;j<columns.intValue();++j){
                objStrings.get(i).add(board.get(i).get(j).toString());
            }
        }
        ArrayList<ArrayList<String>> outStrings=new ArrayList<>();
        for (int i=0;i<rows.intValue();++i){
            for (int t=0;t<MAX_LINES_IN_BLOCK;++t){
                outStrings.add(new ArrayList<>());
            }
            for (int j=0;j<columns.intValue();++j){
                String[] splitted=objStrings.get(i).get(j).split(System.lineSeparator());
                for (int k=0;k<splitted.length && k<MAX_LINES_IN_BLOCK;++k){
                    outStrings.get(MAX_LINES_IN_BLOCK * i + k).add(splitted[k]);
                }
                if (splitted.length<MAX_LINES_IN_BLOCK){
                    for (int k=splitted.length;k<MAX_LINES_IN_BLOCK;++k){
                        outStrings.get(MAX_LINES_IN_BLOCK * i + k).add("");
                    }
                }
            }
        }

        String finalStr=new String();
        String currStr=new String();
        finalStr += System.lineSeparator();
        for (int j = 0; j < MAX_CHARS_IN_BLOCK * columns.intValue() + columns.intValue() * 2; ++j) {
            finalStr += ROW_DELIMETER;
        }
        finalStr+=System.lineSeparator();
        for (int i=0;i<MAX_LINES_IN_BLOCK*rows.intValue();++i){
            for (int j=0;j<columns.intValue();++j){
                currStr=outStrings.get(i).get(j).substring(0,Math.min(outStrings.get(i).get(j).length(),MAX_CHARS_IN_BLOCK));
                while (currStr.length()<MAX_CHARS_IN_BLOCK){
                    currStr+=' ';
                }
                finalStr+=COL_DELIMETER+currStr+COL_DELIMETER;
            }
            if ((i+1)%MAX_LINES_IN_BLOCK==0) {
                finalStr += System.lineSeparator();
                for (int j = 0; j < MAX_CHARS_IN_BLOCK * columns.intValue() + columns.intValue() * 2; ++j) {
                    finalStr += ROW_DELIMETER;
                }
            }
            finalStr+=System.lineSeparator();
        }
        return finalStr;
    }

    @Override
    public Object clone(){
        GameBoard cloneBoard = new GameBoard();
        cloneBoard.rows = rows.intValue();
        cloneBoard.columns = columns.intValue();

        cloneBoard.setBoard();
        for (int i = 0; i < board.size(); ++i){
            for (int j = 0; j < board.get(i).size(); ++j){
                cloneBoard.setObject((TeritoryUnit)getObject(i,j).clone(),i,j);
            }
        }
        cloneBoard.nextEmptyRow = nextEmptyRow;
        cloneBoard.nextEmptyCol = nextEmptyCol;

        return cloneBoard;
    }
}
