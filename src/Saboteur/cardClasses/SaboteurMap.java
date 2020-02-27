package Saboteur.cardClasses;

public class SaboteurMap extends SaboteurCard {
    public String getName(){
        return "Map";
    }
    public SaboteurCard parseSaboteurCard(String name) {
        return new SaboteurMap();
    }
}

