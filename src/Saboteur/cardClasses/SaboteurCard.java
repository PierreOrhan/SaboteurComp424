package Saboteur.cardClasses;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/**
 * Class that abstracts the different card types
 * @author Pierre
 */
abstract public class SaboteurCard {
    private String name;
    abstract public String getName();
    public static Map<String,Integer> getDeckcomposition(){
        Map<String,Integer> compo =new HashMap<String, Integer>();
        compo.put("0",4);
        compo.put("1",1);
        compo.put("2",1);
        compo.put("3",1);
        compo.put("4",1);
        compo.put("5",4);
        compo.put("6",5);
        compo.put("7",5);
        compo.put("8",5);
        compo.put("9",5);
        compo.put("10",3);
        compo.put("11",1);
        compo.put("12",1);
        compo.put("13",1);
        compo.put("14",1);
        compo.put("15",1);
        compo.put("destroy",3);
        compo.put("malus",2);
        compo.put("bonus",4);
        compo.put("map",6);
        return compo;
    }
    public static ArrayList<SaboteurCard> getDeck(){
        //returns an unshuffled deck
        Map<String,Integer> compo = SaboteurCard.getDeckcomposition();
        ArrayList<SaboteurCard> deck =new ArrayList<SaboteurCard>();
        String[] tiles ={"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15"};
        for(int i=0;i<tiles.length;i++){
            for(int j=0;j<compo.get(tiles[i]);j++){
                deck.add(new SaboteurTile(tiles[i]));
            }
        }
        deck.add(new SaboteurDestroy());
        deck.add(new SaboteurDestroy());
        deck.add(new SaboteurDestroy());

        deck.add(new SaboteurMalus());
        deck.add(new SaboteurMalus());

        deck.add(new SaboteurBonus());
        deck.add(new SaboteurBonus());
        deck.add(new SaboteurBonus());
        deck.add(new SaboteurBonus());

        deck.add(new SaboteurMap());
        deck.add(new SaboteurMap());
        deck.add(new SaboteurMap());
        deck.add(new SaboteurMap());
        // Correction 22/03/2020: 2 maps were missing in the deck
        deck.add(new SaboteurMap());
        deck.add(new SaboteurMap());

        return deck;
    }
    public static SaboteurCard copyACard(String name){
        switch (name.split(":")[0]){
            case "Tile": return new SaboteurTile(name.split(":")[1]);
            case "Map": return new SaboteurMap();
            case "Malus": return new SaboteurMalus();
            case "Bonus": return new SaboteurBonus();
            case "Destroy": return new SaboteurDestroy();
        }
        return new SaboteurDrop();
    }
}


