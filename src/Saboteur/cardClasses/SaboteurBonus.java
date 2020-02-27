package Saboteur.cardClasses;

public class SaboteurBonus extends SaboteurCard {
    private int nbAtFirst = 4;
    public String getName(){
        return "Bonus";
    }
    public SaboteurCard parseSaboteurCard(String name) {
        return new SaboteurBonus();
    }
}
