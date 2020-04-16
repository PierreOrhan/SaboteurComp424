package student_player;

import java.util.ArrayList;

import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurTile;

public class AndNode extends AndOrNode{
	public SaboteurCard dealedCard;
	public double prob;
	public ORNode parent;
	public ArrayList<ORNode> succesors;
	public int depth;
	
	public AndNode(String name, BoardState boardState,SaboteurCard dealedCard, double dealedCardProb) {
		super(name,boardState);
		this.dealedCard = dealedCard;
		if(boardState.getTurnPlayer() == 1) {
			boardState.player1Cards.add(dealedCard);
		}else {
			boardState.player2Cards.add(dealedCard);
		}
		this.prob = dealedCardProb;
		ArrayList<SaboteurMove> moves = boardState.getAllLegalMoves();
		int counter = 0;
		this.succesors = new ArrayList<>();
		for(SaboteurMove move: moves) {
			
			String nodeName = "OR,"+(depth+1)+","+counter;
			
			BoardState nodeBoardState = new BoardState(boardState);
			nodeBoardState.processMove(move, false);
			ORNode node = new ORNode(nodeName,nodeBoardState,move);
			node.parent = this;
			succesors.add(node);
		}
	}
	
	public double getMinHeuristicVal(int[] goalPos,int depth, int maxDepth) {
		double minHeuristicVal = Integer.MAX_VALUE;
		for(ORNode node: succesors) {
			if(node.move.getCardPlayed() instanceof SaboteurTile) {
				if(!isGoodTile((SaboteurTile)node.move.getCardPlayed()))continue;
			}
			double curVal = node.getExpectedMinHeuistic(goalPos,depth+1, maxDepth);
			if(curVal < minHeuristicVal) {
				minHeuristicVal = curVal;
			}
			if(minHeuristicVal == Integer.MIN_VALUE)
				break;
		}
		return minHeuristicVal;
	}
	
	public boolean isGoodTile(SaboteurTile tile) {
		if(tile.getIdx().equals("0")||tile.getIdx().equals("5_flip")||tile.getIdx().equals("5")||tile.getIdx().equals("6")||
				tile.getIdx().equals("6_flip")||tile.getIdx().equals("7_flip")||tile.getIdx().equals("7")
				||tile.getIdx().equals("8")||tile.getIdx().equals("9")
			||tile.getIdx().equals("9_flip")||tile.getIdx().equals("10")) {
			return true;
		}
		return false;
	}
	
}
