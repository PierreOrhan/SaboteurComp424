package student_player;

import java.util.ArrayList;

import Saboteur.cardClasses.SaboteurTile;

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
	
    public boolean checkDeadEnd(SaboteurTile tile) {
    	String idx = tile.getIdx();
    	if(idx.equals("1")||idx.equals("2")||idx.equals("2_flip")||idx.equals("3")||idx.equals("3_flip")||
    			idx.equals("4")||idx.equals("4_flip")||idx.equals("11")||idx.equals("11_flip")||
    			idx.equals("12")||idx.equals("12_flip")||idx.equals("13")||idx.equals("14")||idx.equals("14_flip")) {
    		return true;
    	}
    	return false;
    }
	
}
	

