package student_player;

import java.util.ArrayList;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurTile;
import boardgame.Board;

public class MyTools {
	//TODO: Determine EXPLORE_DEPTH
	private static int EXPLORE_DEPTH = 4;
    public static double getSomething() {
        return Math.random();
    }
    
    public static int alpha_beta_pruning(int alpha, int beta,int depth ,BoardState state) {
    	
    	if(depth == EXPLORE_DEPTH || state.getWinner() != Board.NOBODY) {
    		 if (depth %2 == 0) {
    			 return GetHeuristic(state)/(depth+1);
    		 }
    		 return - GetHeuristic(state)/(depth+1);
    	}
    	
    	//Initialize best value
    	int bestVal;
    	if(depth%2 ==0) {	//Max Player
    		bestVal = Integer.MIN_VALUE;
    	}else{				//Min Player
    		bestVal = Integer.MAX_VALUE;
    	}
    	
    	ArrayList<SaboteurMove> list = state.getAllLegalMoves();
    	for(SaboteurMove move : list) {
    		state.processMove(move,true);
    		
    		int value = alpha_beta_pruning(alpha, beta, depth+1, state);
    		
    		if(depth%2 ==0) {		//Max Player
    			if(value > bestVal) {
    				bestVal = value;
    				alpha = bestVal;
    			}
    			if(beta <= alpha) {
    				break;
    			}
    		}
    		else {					//Min player
    			if(value < bestVal) {
    				bestVal = value;
    				beta = bestVal;
    			}
    			if(beta <= alpha) {
    				break;
    			}
    		}
    	}
    		
    	return bestVal;
    }
    
    
    //TODO: Observe the board state and calculate the heuristic value.
    public static int GetHeuristic(BoardState state) {
    	
    	int score = 0;
    	
    	int origin = 5;
    	int goal = 12;
    	int opponent = state.getTurnPlayer();
    	int self = 1-opponent;
    	SaboteurTile[][] board = state.board;
    	
    	//check winner state
    	if(state.getWinner() == opponent) {
    		return -100000;
    	}
    	if(state.getWinner() == self) {
    		return 100000;
    	}
    	
    	//Malus state
    	int nbmalusself = state.getNbMalus(self);
    	int nbmalusoppo = state.getNbMalus(opponent);
    	if(nbmalusself > 0) {
    		score -= 50;
    	}
    	if(nbmalusoppo > 0) {
    		score += 50;
    	}
    	
    	//Hidden Tile
    	boolean GoalHidden = true;
    	int[] nugget = new int[2];
    	if(board[goal][origin].getIdx().equals("nugget")) {
    		nugget[0] = goal;
    		nugget[1] = origin;
    		GoalHidden = false;
    	}
    	else if(board[goal][origin-2].getIdx().equals("nugget")) {
    		nugget[0] = goal;
    		nugget[1] = origin-2;
    		GoalHidden = false;
    	}
    	else if(board[goal][origin+2].getIdx().equals("nugget")) {
    		nugget[0] = goal;
    		nugget[1] = origin+2;
    		GoalHidden = false;
    	}
    	
    	int[][] tilemap = new int[board.length][board[0].length];
    	for(int i = 0;i<board.length;i++) {
    		for (int j = 0; j<board[0].length;j++) {
    			if(board[i][j] != null) {
    				tilemap[i][j] = 1;
    			}
    			else {
    				tilemap[i][j] = 0;
    			}
    		}
    	}
    	
    	PathTree pathTree = new PathTree(board, tilemap);
    	ArrayList<TileNode> leaves = pathTree.leaves;
    	//two mode
    	if(GoalHidden) {
    		int mindist = 100;
    		int mindistleft = 100;
    		int mindistright = 100;
    		for(TileNode leaf : leaves) {
    			int dist = checkDist(leaf.x(),leaf.y(),5, 12);
    			int dist2 = checkDist(leaf.x(),leaf.y(),3,12);
    			int dist3 = checkDist(leaf.x(),leaf.y(),7, 12);
    			boolean isDeadEnd = checkDeadEnd(leaf.tile);
    			if(dist < mindist && !isDeadEnd) {
    				mindist = dist;
    			}
    			if(dist2 < mindistleft && !isDeadEnd) {
    				mindistleft = dist2;
    			}
    			if(dist3 < mindistright && !isDeadEnd) {
    				mindistright = dist3;
    			}
    		}
    		score += 3/(mindist+mindistleft+mindistright);
    	}
    	else {
    		int mindist = 100;
    		for(TileNode leaf : leaves) {
    			int dist = checkDist(leaf.x(),leaf.y(),nugget[0], nugget[1]);
    			if(dist < mindist && !checkDeadEnd(leaf.tile)) {
    				mindist = dist;
    			}
    		}
    		score += 1/mindist;
    	}
    	
    	return score;
    }
    
    public static int checkDist(int i, int j, int i2, int j2) {
    	return Math.abs(i-i2) + Math.abs(j-j2);
    }
    
    public static boolean checkDeadEnd(SaboteurTile tile) {
    	String idx = tile.getIdx();
    	if(idx.equals("1")||idx.equals("2")||idx.equals("2_flip")||idx.equals("3")||idx.equals("3_flip")||
    			idx.equals("4")||idx.equals("4_flip")||idx.equals("11")||idx.equals("11_flip")||
    			idx.equals("12")||idx.equals("12_flip")||idx.equals("13")||idx.equals("14")||idx.equals("14_flip")) {
    		return true;
    	}
    	return false;
    }
    
    /*public class TileNode{
    	public SaboteurTile tile;
    	public int[] position = new int[2];
    	public int depth;
    	public ArrayList<TileNode> children = new ArrayList<TileNode>();
    	
    	public TileNode(SaboteurTile t, int i, int j, int d){
    		tile = t;
    		position[0] = i;
    		position[1] = j;
    		depth = d;
    	}
    	
    	public void Addchild(TileNode t) {
    		children.add(t);
    	}
    	
    	public int x() {
    		return position[0];
    	}
    	
    	public int y() {
    		return position[1];
    	}
    	
    }
    
    public class PathTree{
    	public TileNode root;
    	public ArrayList<TileNode> leaves = new ArrayList<TileNode>();
    	public int length;
    	
    	//Init
    	public PathTree(SaboteurTile[][] board, int[][] map) {
    		int depth = 1;
    		//root = origin
    		root = new TileNode(board[5][5],5,5,0);
    		//visited map
    		boolean[][] visited = new boolean[board.length][board[0].length];
    		visited[5][5] = true; 
    		
    		//parents list
    		ArrayList<TileNode> parents = new ArrayList<TileNode>();
    		parents.add(root);
    		
    		while(parents.size() != 0) {
    			//children list
    			ArrayList<TileNode> children = new ArrayList<TileNode>();
    			
    			for(TileNode p : parents) {
    				//if p is a dead end, then there is no child
    				if(checkDeadEnd(p.tile)) {
    					leaves.add(p);
    				}
    				
    				else {
	    				int[][] neighbors = {{p.x()+1,p.y()},{p.x()-1,p.y()},{p.x(),p.y()+1},{p.x(),p.y()-1}};
	    				int nbChild = 0;
	    				//check children
	    				for(int[] nei : neighbors) {
	    					//check if the position exist on the board and either there is a tile(not a null object)
	    					if(nei[0] < 14 && nei[0] > 0 && nei[1] < 14 && nei[1] > 0) {
	    						if(map[nei[0]][nei[1]] == 1) {
	    							SaboteurTile neighbor = board[nei[0]][nei[1]];
	    							
	    							//check if connected
	    							boolean connected = false;
	    							if(nei[0]-p.x() == p.tile.getPath()[1][2]) connected = true;
	    							else if(nei[0]-p.x() == -p.tile.getPath()[1][0]) connected = true;
	    							else if(nei[1]-p.y() == -p.tile.getPath()[0][1]) connected = true;
	    							else if(nei[1]-p.y() == p.tile.getPath()[2][1]) connected = true;
	    							
	    							//if the node is connected and not visited
	    							if(connected && !visited[nei[0]][nei[1]]) {
	    								TileNode child = new TileNode(neighbor, nei[0], nei[1], depth);
	    								p.Addchild(child);
	    								children.add(child);
	    								visited[nei[0]][nei[1]] = true;
	    								nbChild++;
	    							}
	    						}
	    					}
	    				}
	    				
	    				//no child , becomes a leaf
	    				if(nbChild == 0) {
	    					leaves.add(p);
	    				}
    				}
    			}
    			
    			//children become next parents
    			parents = children;
    			depth++;
    		}
    		
    		length = depth;
    	}
    	
    }*/
}