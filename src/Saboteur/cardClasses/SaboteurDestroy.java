package Saboteur.cardClasses;

public class SaboteurDestroy extends SaboteurCard {
    public String getName(){
        return "Destroy";
    }
    public SaboteurCard parseSaboteurCard(String name) {
        return new SaboteurDestroy();
    }
}
