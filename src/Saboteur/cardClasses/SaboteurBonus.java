package Saboteur.cardClasses;

public class SaboteurBonus extends SaboteurCard {
    public String getName(){
        return "Bonus";
    }
    public SaboteurCard parseSaboteurCard(String name) {
        return new SaboteurBonus();
    }
}
