package Saboteur.cardClasses;

public class SaboteurMalus extends SaboteurCard {
    public String getName(){
        return "Malus";
    }
    public SaboteurCard parseSaboteurCard(String name) {
        return new SaboteurMalus();
    }
}
