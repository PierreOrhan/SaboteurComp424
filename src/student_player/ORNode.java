package student_player;

import java.util.ArrayList;

import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurCard;

public class ORNode extends AndOrNode{
	public double heuristicVal;
	public SaboteurMove move;
	
	public ORNode(String name, BoardState boardState, ArrayList<SaboteurCard> playerHands, SaboteurMove move) {
		super(name,boardState,playerHands);
		this.move = move;
	}
	
	
	//Don't consider Probability
	//h(n) = w1 * (path linked to entrance's closest distance to goal tile) + w2 * (open paths from entrance)
	//Consider Probability
	//h(n) = w3 * (w1 * (distance to goal tile) + w2 * (open paths from entrance))
	public void calculateHeuristic() {
		
	}
	
}
