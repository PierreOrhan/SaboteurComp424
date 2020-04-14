package student_player;

import java.util.ArrayList;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurTile;

public class AndOrNode {
	public BoardState boardState;
	public ArrayList<SaboteurCard> player1Hands;
	public ArrayList<SaboteurCard> player2Hands;
	public String name;
	
	public AndOrNode(String name, BoardState boardState) {
		this.name = name;
		this.boardState = boardState;
	}
	
	//Dir: 0 Up; 1 Right; 2 Down; 3 Left
	public boolean checkOpenEnd(int[][] intBoard,SaboteurTile[][] tileBoard,int i,int j,int dir) {
		switch(dir){
		case 0:
			if(intBoard[i][j]== 1&& i/3 == 0) return true;
			if(intBoard[i][j]==1&&tileBoard[i/3-1][j/3]==null)
				return true;
			break;
		case 1:
			if(intBoard[i][j]== 1&& j/3 == 13) return true;
			if(intBoard[i][j]==1&&tileBoard[i/3][j/3 + 1]==null)
				return true;
			break;
		case 2:
			if(intBoard[i][j]== 1&&i/3 == 13) return true;
			if(intBoard[i][j]==1&&tileBoard[i/3+1][j/3]==null)
				return true;
			break;
		case 3:
			if(intBoard[i][j]== 1&&j/3 == 0) return true;
			if(intBoard[i][j]==1&&tileBoard[i/3][j/3-1]==null)
				return true;
			break;
		}
		return false;
	}
	
	
}
