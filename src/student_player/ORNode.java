package student_player;

import java.util.ArrayList;
import java.util.Set;

import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurBonus;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDestroy;
import Saboteur.cardClasses.SaboteurMalus;
import Saboteur.cardClasses.SaboteurMap;
import Saboteur.cardClasses.SaboteurTile;

public class ORNode extends AndOrNode{
	public double heuristicVal;
	public SaboteurMove move;
	private static double W1 = 1000;
	private static double W2 = -50;
	private static double W3 = -50000;
	private static double W6 = -50000;
	private static double W4 = 100;
	private static double W5_2 = 200;
	private static double W5_1 = 1000;
	private boolean maxPlayer;
	public ArrayList<AndNode> children;
	public AndNode parent;
	public ORNode(String name, BoardState boardState,SaboteurMove move) {
		super(name,boardState);
		this.move = move;
		this.children = new ArrayList<>();
	}
	
	public double getExpectedMinHeuistic(int[] goalPos, int depth, int maxDepth) {
		calculateHeuristic(goalPos);
		if(depth == maxDepth || heuristicVal == Integer.MIN_VALUE) {
			return this.heuristicVal;
		}else {
			//Get all possible dealed cards
			
			int totalPossibleCardsSize = boardState.possibleDeckCards.size();
			Set<String> possibleCardNames = boardState.possibleDeckCards.keySet();
			
			//Create every possible AndNode and add it to list
			int counter = 0;
			for(String cardName:possibleCardNames) {
				SaboteurCard card = null;
				BoardState newBoard = new BoardState(boardState);
				int size = newBoard.possibleDeckCards.get(cardName);
				if(size>0) {
					if(isATileCard(cardName)) {
						card = new SaboteurTile(cardName);
					}else if(cardName.equals("destroy")) {
						card = new SaboteurDestroy();
					}else if(cardName.equals("malus")) {
						card = new SaboteurMalus();
					}else if(cardName.equals("bonus")) {
						card = new SaboteurBonus();
					}else if(cardName.equals("map")) {
						card = new SaboteurMap();
					}
					newBoard.possibleDeckCards.put(cardName, size-1);
					//Calculate probabilities
					double prob = ((double)size)/(double)totalPossibleCardsSize;
					String name = "AND,"+depth+","+counter;
					if(card != null) {
						AndNode node = new AndNode(name,newBoard,card,prob);
						node.parent = this;
						node.depth = depth;
						this.children.add(node);
					}
				}
				counter++;
				
			}
			
			//Iterate through all children AndNodes, calculate p(AndNode) * getMinVal(AndNode) to get an expected value
			double expectedValue = 0;
			for(AndNode child: children) {
				expectedValue += child.prob * child.getMinHeuristicVal(goalPos, depth, maxDepth);
			}
			return expectedValue;
		}
		
	}
	
	public boolean isATileCard(String cardName) {
		String[] tiles =new String[]{"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15"};
		for(int i=0;i<tiles.length;i++) {
			if(tiles[i].contentEquals(cardName))
				return true;
		}
		return false;
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
		int turnPlayer = this.boardState.getTurnPlayer();
		int opponentMaluses;
		int selfMaluses = 0;
		if(turnPlayer == 1) {
			opponentMaluses = this.boardState.getNbMalus(0);
		}else {
			opponentMaluses = this.boardState.getNbMalus(1);
		}
		if(turnPlayer == 1) {
			for(SaboteurCard card:this.boardState.player1Cards)
				if(card instanceof SaboteurMalus)
					selfMaluses++;
		}else {
			for(SaboteurCard card:this.boardState.player2Cards)
				if(card instanceof SaboteurMalus)
					selfMaluses++;
		}
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
							curDist -= 300;
						}
						//Add Open Ends To Open End Lists
						int[][] moves = {{0, -1},{0, 1},{1, 0},{-1, 0}};
						for(int m= 0;m<4;m++) {
							int neighbourX = (3 * i + 1) + moves[m][0];
							int neighbourY = (3 * j + 1) + moves[m][1];
							if(0 <= neighbourX && neighbourX < BoardState.BOARD_SIZE*3
							   && 0 <= neighbourY && neighbourY < BoardState.BOARD_SIZE*3
							   && 0 <= i+moves[m][0] && i+moves[m][0] < BoardState.BOARD_SIZE
							   && 0 <= j+moves[m][1] && j+moves[m][1] < BoardState.BOARD_SIZE) {
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
		h1 = minDist;
		h2 = openEndPos.size();
		h3 = numOfGoodTilesAboveRow5;
		this.heuristicVal = h1 + W2 * h2 + W4 * numOfGoodTilesAboveRow5 + W6 * selfMaluses ;
		//System.out.println(""+this.move.toPrettyString() + "TotalDist: "+this.heuristicVal);
	}
	
	public void calculateHeuristic2(int[] goalPos) {
		int goalPosX = goalPos[0];
		int goalPosY = goalPos[1];
		int originPos =5;
		int turnPlayer = this.boardState.getTurnPlayer();
		int opponentMaluses;
		int selfMaluses;
		if(turnPlayer == 1) {
			opponentMaluses = this.boardState.getNbMalus(0);
		}else {
			opponentMaluses = this.boardState.getNbMalus(1);
		}
		if(turnPlayer == 1) {
			selfMaluses = this.boardState.getNbMalus(0);
		}else {
			selfMaluses = this.boardState.getNbMalus(1);
		}
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
							curDist -= 300;
						}
						//Add Open Ends To Open End Lists
						int[][] moves = {{0, -1},{0, 1},{1, 0},{-1, 0}};
						for(int m= 0;m<4;m++) {
							int neighbourX = (3 * i + 1) + moves[m][0];
							int neighbourY = (3 * j + 1) + moves[m][1];
							if(0 <= neighbourX && neighbourX < BoardState.BOARD_SIZE*3
							   && 0 <= neighbourY && neighbourY < BoardState.BOARD_SIZE*3
							   && 0 <= i+moves[m][0] && i+moves[m][0] < BoardState.BOARD_SIZE
							   && 0 <= j+moves[m][1] && j+moves[m][1] < BoardState.BOARD_SIZE) {
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
		h1 = minDist;
		h2 = openEndPos.size();
		h3 = numOfGoodTilesAboveRow5;
		this.heuristicVal = h1 + W2 * h2 + W4 * numOfGoodTilesAboveRow5 + W6 * opponentMaluses ;
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
