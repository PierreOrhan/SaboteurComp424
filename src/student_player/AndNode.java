package student_player;

import java.util.ArrayList;
import Saboteur.cardClasses.SaboteurCard;

public class AndNode extends AndOrNode{
	public SaboteurCard dealedCard;
	public double dealedCardProb;
	public ORNode parent;
	public ArrayList<ORNode> succesors;
	
	public AndNode(String name, BoardState boardState, ArrayList<SaboteurCard> playerHands, SaboteurCard dealedCard, double dealedCardProb) {
		super(name,boardState,playerHands);
		this.dealedCard = dealedCard;
		this.dealedCardProb = dealedCardProb;
	}
	
	
}
