package student_player;

import java.util.ArrayList;

import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurTile;

public class ORNode extends AndOrNode{
	public double heuristicVal;
	public SaboteurMove move;
	private static double W1 = 100;
	private static double W2 = -5;
	private static double W3 = 5000;
	private static double W4 = 10;
	private static double W5_2 = 20;
	private static double W5_1 = 20;
	private boolean maxPlayer;
	public ORNode(String name, BoardState boardState,SaboteurMove move) {
		super(name,boardState);
		this.move = move;
	}
	
	
	//Don't consider Probability
	//h(n) = w1 * (path linked to entrance's vertical distance to goal tile) + w5* (path linked to entrance's horizontal
	//distance to goal tile) + w2 * (open paths from entrance) + w3 * numberOfMaluses + w4 * numberOfGoodTilesAbove5
	//Consider Probability
	//h(n) = 
	public void calculateHeuristic(int[] goalPos) {
		int goalPosX = goalPos[0];
		int goalPosY = goalPos[1];
		int originPos =5;
		ArrayList<int[]> originTargets = new ArrayList<>();
		originTargets.add(new int[]{originPos*3+1, originPos*3+1});
        originTargets.add(new int[]{originPos*3+1, originPos*3+2});
        originTargets.add(new int[]{originPos*3+1, originPos*3});
        originTargets.add(new int[]{originPos*3, originPos*3+1});
        originTargets.add(new int[]{originPos*3+2, originPos*3+1});
        
        int[] goalPosInInt = new int[2];
        goalPosInInt[0] = goalPosX*3+1;
        goalPosInInt[1] = goalPosY*3+1;

        if(boardState.cardPath(originTargets,goalPosInInt,false)){
        	
        	System.out.println("Found move to success!");
        	this.heuristicVal = Integer.MIN_VALUE;
        	return;
        }
		double minDist = Integer.MAX_VALUE;
		ArrayList<int[]> openEndPos = new ArrayList<>();
		double h1,h2,h3,h4;
		int numOfGoodTilesAboveRow5=0;
		for(int i = 0 ; i < BoardState.BOARD_SIZE;i++)
			for(int j = 0; j < BoardState.BOARD_SIZE; j++) {
				if(boardState.board[i][j]!=null) {
					//Increment Good Tiles Num
					if(i<5&&isGoodTile(boardState.board[i][j])) {
						numOfGoodTilesAboveRow5++;
					}
					
					int[] currentMiddlePoint1 = {3*i+2,3*j+1}; // row 3 block 2
					int[] currentMiddlePoint2 = {3*i+1,3*j}; //row 2 block 1
					int[] currentMiddlePoint3 = {3*i+1,3*j+2}; //row 2 block 3
					int[] currentMiddlePoint4 = {3*i+2,3*j+1};
					
					//If there's CardPath from entrance to current position, update closest distance and open end list
					if(boardState.cardPath(originTargets,currentMiddlePoint1,false)
							&&(checkOpenEnd(boardState.intBoard,boardState.board,currentMiddlePoint1[0],currentMiddlePoint1[1],2)
							||checkOpenEnd(boardState.intBoard,boardState.board,currentMiddlePoint2[0],currentMiddlePoint2[1],1)
							||checkOpenEnd(boardState.intBoard,boardState.board,currentMiddlePoint3[0],currentMiddlePoint3[1],3))) {
//						
//						System.out.println("Accesible from origin i: "+i+"j: "+j);
//						System.out.println("i/0 at currentIntPos 3*i+2: "+currentMiddlePoint1[0]
//								+"3*j+1: "+currentMiddlePoint1[1]+" "+boardState.intBoard[currentMiddlePoint1[0]][currentMiddlePoint1[1]]);
						double curDist = W1*Math.abs(i-goalPosX)+W5_1*Math.abs(j-goalPosY);
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
	
		//System.out.println(""+this.move.toPrettyString() + "minDist: "+minDist);
		h1 = minDist;
		h2 = openEndPos.size();
		h3 = numOfGoodTilesAboveRow5;
		this.heuristicVal = h1 + W2 * h2 + W4 * numOfGoodTilesAboveRow5;
		//System.out.println(""+this.move.toPrettyString() + "TotalDist: "+this.heuristicVal);
	}
	
	public void calculateHeuristic2(int[] goalPos) {
		int goalPosX = goalPos[0];
		int goalPosY = goalPos[1];
		int originPos =5;
		ArrayList<int[]> originTargets = new ArrayList<>();
		originTargets.add(new int[]{originPos*3+1, originPos*3+1});
        originTargets.add(new int[]{originPos*3+1, originPos*3+2});
        originTargets.add(new int[]{originPos*3+1, originPos*3});
        originTargets.add(new int[]{originPos*3, originPos*3+1});
        originTargets.add(new int[]{originPos*3+2, originPos*3+1});
        
        int[] goalPosInInt = new int[2];
        goalPosInInt[0] = goalPosX*3+1;
        goalPosInInt[1] = goalPosY*3+1;
        
        if(boardState.cardPath(originTargets,goalPosInInt,false)){
        	this.heuristicVal = Integer.MIN_VALUE;
        	return;
        }
		double minDist = Integer.MAX_VALUE;
		ArrayList<int[]> openEndPos = new ArrayList<>();
		int h1,h2,h3,h4;
		for(int i = 0 ; i < BoardState.BOARD_SIZE;i++)
			for(int j = 0; j < BoardState.BOARD_SIZE; j++) {
				if(boardState.board[i][j]!=null) {
					
					
					int[] currentMiddlePoint1 = {3*i+2,3*j+1}; // row 3 block 2
					int[] currentMiddlePoint2 = {3*i+1,3*j}; //row 2 block 1
					int[] currentMiddlePoint3 = {3*i+1,3*j+2}; //row 2 block 3
					int[] currentMiddlePoint4 = {3*i+2,3*j+1};
					
					//If there's CardPath, update closest distance and open end list
                    //&&(boardState.cardPath(originTargets,currentMiddlePoint2,false)
    				//||boardState.cardPath(originTargets,currentMiddlePoint3,false)
    				//||boardState.cardPath(originTargets,currentMiddlePoint4,false))
					if(boardState.cardPath(originTargets,currentMiddlePoint1,false)
							&&(checkOpenEnd(boardState.intBoard,boardState.board,currentMiddlePoint1[0],currentMiddlePoint1[1],2)
							||checkOpenEnd(boardState.intBoard,boardState.board,currentMiddlePoint2[0],currentMiddlePoint2[1],1)
							||checkOpenEnd(boardState.intBoard,boardState.board,currentMiddlePoint3[0],currentMiddlePoint3[1],3))) {
//						
//						System.out.println("Accesible from origin i: "+i+"j: "+j);
//						System.out.println("i/0 at currentIntPos 3*i+2: "+currentMiddlePoint1[0]
//								+"3*j+1: "+currentMiddlePoint1[1]+" "+boardState.intBoard[currentMiddlePoint1[0]][currentMiddlePoint1[1]]);
						double curDist = W1*Math.abs(i-goalPosX)+W5_2*Math.abs(j-goalPosY);
						if(boardState.intBoard[currentMiddlePoint1[0]][currentMiddlePoint1[1]]==1) {
							curDist -= 10;
						}
						//Add Open Ends To Open End Lists
						int[][] moves = {{0, -1},{0, 1},{1, 0},{-1, 0}};
						for(int m= 0;m<4;m++) {
							int neighbourX = (3 * i + 1) + moves[m][0];
							int neighbourY = (3 * j + 1) + moves[m][1];
							if(0 <= neighbourX && neighbourX<14 && 0 <= neighbourY && neighbourY<14) {
								if(boardState.intBoard[neighbourX][neighbourY]==1&&boardState.board[i+moves[m][0]][j+moves[m][1]]==null) {
									openEndPos.add(new int[]{neighbourX,neighbourY});
								}
							}
						}
						
						if(minDist > curDist) {
							minDist = curDist;
							
						}
					}
				}
					
			}
	
		//System.out.println(""+this.move.toPrettyString() + "minDist: "+minDist);
		h2 = openEndPos.size();
		this.heuristicVal = minDist + W2 * h2;
		//System.out.println(""+this.move.toPrettyString() + "TotalDist: "+this.heuristicVal);
	}
	
	/*
	 * Return true if tile idx
	 */
	public boolean isGoodTile(SaboteurTile tile) {
		if(tile.getIdx().equals("0")||tile.getIdx().equals("5_flip")||tile.getIdx().equals("5")||tile.getIdx().equals("6_flip")||
				tile.getIdx().equals("6_flip")||tile.getIdx().equals("7_flip")||tile.getIdx().equals("8")||tile.getIdx().equals("9")
			||tile.getIdx().equals("9_flip")||tile.getIdx().equals("10")) {
			return true;
		}
		return false;
	}
	
}
