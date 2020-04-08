package student_player;

import boardgame.Move;

import Saboteur.SaboteurPlayer;
import Saboteur.cardClasses.SaboteurCard;

import java.util.ArrayList;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;


/** A player file submitted by a student. */
public class StudentPlayer extends SaboteurPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("Linda");
    }
    private ArrayList<SaboteurCard> playerCards; //hand of player
    private BoardState myBoardState;
    private ArrayList<SaboteurCard> possibleLatPlayedCardByOpponent;
    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(SaboteurBoardState boardState) {
        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...
        //MyTools.getSomething();

        // Is random the best you can do?
        //Move myMove = boardState.getRandomMove();
        
    	int val = -1000000;
    	ArrayList<SaboteurMove> list = boardState.getAllLegalMoves();
    	SaboteurMove finalmove = list.get(0);
    	
    	for(SaboteurMove move : list) {
    		SaboteurBoardState clone = (SaboteurBoardState) boardState.clone();
    		clone.processMove(move);
    		
    		int value = MyTools.alpha_beta_pruning(1000000,val,0,boardState);
    		
    		if(value > val) {
    			finalmove = move;
    			val = value;
    		}
    	}
    	
    	Move myMove = finalmove;
        // Return your move to be processed by the server.
        return myMove;
    }
    
    public void initializePlayerCards(SaboteurBoardState boardState) {
    	playerCards = (ArrayList<SaboteurCard>)boardState.getCurrentPlayerCards().clone();
    }
    
    public void initializeBoardState(SaboteurBoardState boardState) {
    	int turnPlayer = boardState.getTurnPlayer();
    	int turnNumber = boardState.getTurnNumber();
    	this.myBoardState = new BoardState(turnPlayer,turnNumber);
    	this.myBoardState.setNbMalus(boardState.getNbMalus(1), boardState.getNbMalus(0));
    	this.myBoardState.fillTileBoardFromOriginalBoard(boardState.getHiddenBoard());
    	this.myBoardState.addCurrentPlayerHandCard(boardState.getCurrentPlayerCards());
    }
    
}