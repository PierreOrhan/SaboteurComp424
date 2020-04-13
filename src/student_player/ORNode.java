package student_player;

import java.util.ArrayList;

import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurCard;

public class ORNode extends AndOrNode{
	public double heuristicVal;
	public SaboteurMove move;
	private static double W1 = 15;
	private static double W2 = -0.5;
	private static double W5 = 4;
	public ORNode(String name, BoardState boardState, ArrayList<SaboteurCard> playerHands, SaboteurMove move) {
		super(name,boardState,playerHands);
		this.move = move;
	}
	
	
	//Don't consider Probability
	//h(n) = w1 * (path linked to entrance's vertical distance to goal tile) + w5* (path linked to entrance's vertical distance to goal tile) 
	//- w2 * (open paths from entrance) - w3 * numberOfMaluses - w4 * numberOfBonuses
	//Consider Probability
	//h(n) = w3 * (w1 * (distance to goal tile) + w2 * (open paths from entrance))
	public void calculateHeuristic(int[] goalPos) {
		int goalPosX = goalPos[0];
		int goalPosY = goalPos[1];
		double minDist = Integer.MAX_VALUE;
		ArrayList<int[]> openEndPos = new ArrayList<>();
		int h1,h2,h3,h4;
		for(int i = 0 ; i < BoardState.BOARD_SIZE;i++)
			for(int j = 0; j < BoardState.BOARD_SIZE; j++) {
				if(boardState.board[i][j]!=null) {
					
					int originPos =5;
					int[] currentMiddlePoint1 = {3*i+2,3*j+1}; // row 3 block 2
					int[] currentMiddlePoint2 = {3*i+1,3*j}; //row 2 block 1
					int[] currentMiddlePoint3 = {3*i+1,3*j+2}; //row 2 block 3
					int[] currentMiddlePoint4 = {3*i+2,3*j+1};
					ArrayList<int[]> originTargets = new ArrayList<>();
					originTargets.add(new int[]{originPos*3+1, originPos*3+1});
                    originTargets.add(new int[]{originPos*3+1, originPos*3+2});
                    originTargets.add(new int[]{originPos*3+1, originPos*3});
                    originTargets.add(new int[]{originPos*3, originPos*3+1});
                    originTargets.add(new int[]{originPos*3+2, originPos*3+1});
					//If there's CardPath, update closest distance and open end list
                    //&&(boardState.cardPath(originTargets,currentMiddlePoint2,false)
    				//||boardState.cardPath(originTargets,currentMiddlePoint3,false)
    				//||boardState.cardPath(originTargets,currentMiddlePoint4,false))
					if(boardState.cardPath(originTargets,currentMiddlePoint1,false)
							&&(boardState.intBoard[currentMiddlePoint1[0]][currentMiddlePoint1[1]]==1
							||boardState.intBoard[currentMiddlePoint2[0]][currentMiddlePoint2[1]]==1
							||boardState.intBoard[currentMiddlePoint3[0]][currentMiddlePoint3[1]]==1)) {
						
						System.out.println("Accesible from origin i: "+i+"j: "+j);
						System.out.println("i/0 at currentIntPos 3*i+2: "+currentMiddlePoint1[0]
								+"3*j+1: "+currentMiddlePoint1[1]+" "+boardState.intBoard[currentMiddlePoint1[0]][currentMiddlePoint1[1]]);
						double curDist = W1*Math.abs(i-goalPosX)+W5*Math.abs(j-goalPosY);
						if(boardState.intBoard[currentMiddlePoint1[0]][currentMiddlePoint1[1]]==1) {
							curDist -= 10;
						}
						//Add Open Ends To Open End Lists
						int[][] moves = {{0, -1},{0, 1},{1, 0},{-1, 0}};
						for(int m= 0;m<4;m++) {
							int neighbourX = (3 * i + 1) + moves[m][0];
							int neighbourY = (3 * j + 1) + moves[m][1];
							if(boardState.intBoard[neighbourX][neighbourY]==1&&boardState.board[i+moves[m][0]][j+moves[m][1]]==null) {
								openEndPos.add(new int[]{neighbourX,neighbourY});
							}
						}
						
						if(minDist > curDist) {
							minDist = curDist;
							
						}
					}
				}
					
			}
	
		System.out.println(""+this.move.toPrettyString() + "minDist: "+minDist);
		h2 = openEndPos.size();
		this.heuristicVal = minDist + W2 * h2;
		System.out.println(""+this.move.toPrettyString() + "TotalDist: "+this.heuristicVal);
	}
	
}
