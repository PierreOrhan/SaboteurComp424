package student_player;

import java.util.ArrayList;

import Saboteur.cardClasses.SaboteurTile;


public class TileNode{
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