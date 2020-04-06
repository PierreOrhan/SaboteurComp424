package student_player;

import java.util.ArrayList;

import Saboteur.SaboteurBoardState;
import Saboteur.cardClasses.SaboteurCard;

public class MyTools {
	
	public static ArrayList<SaboteurCard> discard = new ArrayList<SaboteurCard>();
	
    public static double getSomething() {
        return Math.random();
    }
    
    public static int distanceToNearestGoal(int[] pos) {
    	int distance = Integer.MAX_VALUE;
    	for(int i = 0; i < 3; i++) {
    		int dx = Math.abs(pos[0] - SaboteurBoardState.hiddenPos[i][0]);
    		int dy = Math.abs(pos[1] - SaboteurBoardState.hiddenPos[i][1]);
    		if(dx + dy < distance)
    			distance = dx + dy;
    	}
    	return distance;
    }
}