package Saboteur.cardClasses;

import Saboteur.cardClasses.SaboteurCard;

import java.util.Map;
import java.util.HashMap;

public class SaboteurTile extends SaboteurCard {
    private int[][] path;
    private String idx;
    public SaboteurTile(String idx){
        this.idx = idx;
        this.path = SaboteurTile.initializePath(this.idx);

    }
    public String getName(){
        return "Tile:"+this.idx;
    }
    public String getIdx() {return idx;}
    public SaboteurCard parseSaboteurCard(String name) {
        return new SaboteurTile(name.split(":")[1]);
    }
    public static int[][] initializePath(String idx){
        int[][] path;
        switch (idx){
            case "0":
                path= new int[][]{{0,0,0},{1,1,1},{0,0,0}};
                return path ;
            case "1":
                path= new int[][]{{0,0,0},{1,0,1},{0,0,0}};
                return path ;
            case "2":
                path= new int[][]{{0,0,0},{1,0,1},{0,1,0}};
                return path ;
            case "2_flip":
                path= new int[][]{{0,1,0},{1,0,1},{0,0,0}};
                return path ;
            case "3":
                path= new int[][]{{0,0,0},{0,0,1},{0,1,0}};
                return path ;
            case "3_flip":
                path= new int[][]{{0,1,0},{1,0,0},{0,0,0}};
                return path ;
            case "4":
               path= new int[][] {{0,0,0},{0,1,0},{0,1,0}};
                return path ;
            case "4_flip":
               path= new int[][] {{0,1,0},{0,1,0},{0,0,0}};
                return path ;
            case "5":
               path= new int[][] {{0,0,0},{1,1,0},{0,1,0}};
                return path ;
            case "5_flip":
               path= new int[][] {{0,1,0},{0,1,1},{0,0,0}};
                return path ;
            case "6":
               path= new int[][] {{0,1,0},{1,1,1},{0,0,0}};
                return path ;
            case "6_flip":
               path= new int[][] {{0,0,0},{1,1,1},{0,1,0}};
                return path ;
            case "7":
               path= new int[][] {{0,0,0},{0,1,1},{0,1,0}};
                return path ;
            case "7_flip":
               path= new int[][] {{0,1,0},{1,1,0},{0,0,0}};
                return path ;
            case "8":
               path= new int[][] {{0,1,0},{1,1,1},{0,1,0}};
                return path ;
            case "9":
               path= new int[][] {{0,1,0},{1,1,0},{0,1,0}};
                return path ;
            case "9_flip":
               path= new int[][] {{0,1,0},{0,1,1},{0,1,0}};
                return path ;
            case "10":
               path= new int[][] {{0,1,0},{0,1,0},{0,1,0}};
                return path ;
            case "11":
                path= new int[][] {{0,1,0},{1,0,0},{0,1,0}};
                return path ;
            case "11_flip":
               path= new int[][] {{0,1,0},{0,0,1},{0,1,0}};
                return path ;
            case "12":
               path= new int[][] {{0,0,0},{0,1,1},{0,0,0}};
                return path ;
            case "12_flip":
               path= new int[][] {{0,0,0},{1,1,0},{0,0,0}};
                return path ;
            case "13":
               path= new int[][] {{0,1,0},{1,0,1},{0,1,0}};
                return path ;
            case "14":
               path= new int[][] {{0,1,0},{0,0,1},{0,0,0}};
                return path ;
            case "14_flip":
               path= new int[][] {{0,0,0},{1,0,0},{0,1,0}};
                return path ;
            case "15":
               path= new int[][] {{0,1,0},{0,0,0},{0,1,0}};
                return path ;
            case "entrance":
               path= new int[][] {{0,1,0},{1,1,1},{0,1,0}};
                return path ;
            case "nugget":
               path= new int[][] {{0,1,0},{1,1,1},{0,1,0}};
                return path ;
            case "hidden1":
               path= new int[][] {{0,1,0},{1,1,1},{0,1,0}};
                return path ;
            case "hidden2":
               path= new int[][] {{0,1,0},{1,1,1},{0,1,0}};
                return path ;
            default:
                return null;
        }
    }
    public int[][] getPath(){
        return this.path;
    }
    public static boolean canBeFlipped(String idx){
        String[] flippable = {"2","2_flip","3","3_flip","4","4_flip","5","5_flip","6","6_flip","7","7_flip","9","9_flip","11","11_flip","12","12_flip","14","14_flip"};
        //String[] unFlippable = {"0","1","8","10","13","15","entrance","hidden1","hidden2","nugget"};
        for(String f : flippable){
            if(f.equals(idx)) return true;
        }
        return false;
    }
    public SaboteurTile getFlipped(){
        String[] flippable = {"2","2_flip","3","3_flip","4","4_flip","5","5_flip","6","6_flip","7","7_flip","9","9_flip","11","11_flip","12","12_flip","14","14_flip"};
        //String[] unFlippable = {"0","1","8","10","13","15","entrance","hidden1","hidden2","nugget"};
        for(String f : flippable){
            if(f.equals(idx)) {
                if (f.contains("flip"))
                    return new SaboteurTile(f.split("_flip")[0]);
                else
                    return new SaboteurTile(idx+"_flip");
            }
        }
        return this; //if not flippable, we simply return itself.
    }

}