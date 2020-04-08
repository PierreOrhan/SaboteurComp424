package student_player;

import boardgame.Move;

import Saboteur.SaboteurPlayer;
import Saboteur.cardClasses.SaboteurCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    private BoardState currentBoardState;	//Current Board State
    private BoardState lastBoardState;	//Record Last Board State so that we know what's opponent's move
    private ArrayList<SaboteurCard> possibleLastPlayedCardByOpponent;
    private Map<String,Integer> playedCardstillLastTurn;
    private int playerNb;
   
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
        
    	//Initialize In First Turn...
    	this.playerNb = boardState.getTurnPlayer();
    	initializeBoardState(boardState);
    	
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
    
    public void initializeInFirstTurn(boolean isFirstPlayer) {
    	this.playedCardstillLastTurn = SaboteurCard.getDeckcomposition();
    	playedCardstillLastTurn.put("0",0);
    	playedCardstillLastTurn.put("1",0);
    	playedCardstillLastTurn.put("2",0);
    	playedCardstillLastTurn.put("3",0);
    	playedCardstillLastTurn.put("4",0);
    	playedCardstillLastTurn.put("5",0);
    	playedCardstillLastTurn.put("6",0);
    	playedCardstillLastTurn.put("7",0);
    	playedCardstillLastTurn.put("8",0);
    	playedCardstillLastTurn.put("9",0);
    	playedCardstillLastTurn.put("10",0);
    	playedCardstillLastTurn.put("11",0);
    	playedCardstillLastTurn.put("12",0);
    	playedCardstillLastTurn.put("13",0);
    	playedCardstillLastTurn.put("14",0);
    	playedCardstillLastTurn.put("15",0);
    	playedCardstillLastTurn.put("destroy",0);
    	playedCardstillLastTurn.put("malus",0);
    	playedCardstillLastTurn.put("bonus",0);
    	playedCardstillLastTurn.put("map",0);
    	if(!isFirstPlayer) {
    		String name = "";
    		if(this.currentBoardState.board[5][6]!=null) {
    			name = this.currentBoardState.board[5][6].getName();
    			playedCardstillLastTurn.put(name, 1);
    		}else if(this.currentBoardState.board[5][4]!=null) {
    			name = this.currentBoardState.board[5][4].getName();
    			playedCardstillLastTurn.put(name, 1);
    		}else if(this.currentBoardState.board[4][5]!=null) {
    			name = this.currentBoardState.board[4][5].getName();
    			playedCardstillLastTurn.put(name, 1);
    		}else if(this.currentBoardState.board[6][5]!=null) {
    			name = this.currentBoardState.board[6][5].getName();
    			playedCardstillLastTurn.put(name, 1);
    		}else if(this.currentBoardState.getNbMalus(1-playerNb) == 1) {
    			playedCardstillLastTurn.put("malus", 1);
    		}else if(this.currentBoardState.hiddenRevealed[0]
    				||this.currentBoardState.hiddenRevealed[1]||this.currentBoardState.hiddenRevealed[2]) {
    			playedCardstillLastTurn.put("map", 1);
    		}
    	}
    }
    
    //Not sure if this is necessary since we already added it to current board state...
    public void initializePlayerCards(SaboteurBoardState boardState) {
    	playerCards = (ArrayList<SaboteurCard>)boardState.getCurrentPlayerCards().clone();
    }
    
    public void initializeBoardState(SaboteurBoardState boardState) {
    	int turnPlayer = boardState.getTurnPlayer();
    	int turnNumber = boardState.getTurnNumber();
    	this.currentBoardState = new BoardState(turnPlayer,turnNumber);
    	this.currentBoardState.setNbMalus(boardState.getNbMalus(1), boardState.getNbMalus(0));
    	this.currentBoardState.fillTileBoardFromOriginalBoard(boardState.getHiddenBoard());
    	this.currentBoardState.addCurrentPlayerHandCard(boardState.getCurrentPlayerCards());
    }
    
}