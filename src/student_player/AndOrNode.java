package student_player;

import java.util.ArrayList;
import Saboteur.cardClasses.SaboteurCard;

public class AndOrNode {
	public BoardState boardState;
	public ArrayList<SaboteurCard> playerHands;
	public String name;
	
	public AndOrNode(String name, BoardState boardState, ArrayList<SaboteurCard> playerHands) {
		this.name = name;
		this.boardState = boardState;
		this.playerHands = playerHands;
	}
	
	
	
	
}
