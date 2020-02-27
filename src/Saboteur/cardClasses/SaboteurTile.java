package Saboteur.cardClasses;

import Saboteur.cardClasses.SaboteurCard;

public class SaboteurTile extends SaboteurCard {
    private int[] path;
    private String idx;
    public SaboteurTile(String idx){
        this.idx = idx;
    }
    public String getName(){
        return "Tile:"+idx+"";
    }
    public SaboteurCard parseSaboteurCard(String name) {
        return new SaboteurTile(name.split(":")[1]);
    }
}