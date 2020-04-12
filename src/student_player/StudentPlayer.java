package student_player;

import boardgame.Move;

import Saboteur.SaboteurPlayer;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurMap;

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
    
    private BoardState currentBoardState;	//Current Board State
    private BoardState lastBoardState;	//Record Last Board State so that we know what's opponent's move
    private ArrayList<SaboteurCard> playerCards; //hand of player
    private ArrayList<SaboteurCard> possibleLastPlayedCardByOpponent;
    private Map<String,Integer> playedCardstillLastTurn;
    private int playerNb;
    enum GameState {
    	  Opening,
    	  End
    }
    private GameState gameState;
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
    	if(boardState.getTurnNumber() < 2) {
    		this.playerNb = boardState.getTurnPlayer();
    		this.initializeInFirstTurn(this.playerNb == boardState.firstPlayer());
    		this.gameState = GameState.Opening;
    	}
    	
    	initializeBoardState(boardState);
    	ArrayList<SaboteurMove> list = boardState.getAllLegalMoves();
    	System.out.println("Legal Moves list size is "+list.size());
    	if(gameState == GameState.Opening) {
    		//If not nugget found and there's map card
    		ArrayList<SaboteurMove> mapmoves = getAllMapMoves(list);
    		if(!currentBoardState.isNuggetFound() && mapmoves.size()>0) {
    			//If left goal tile not revealed, then Reveal Left GoalTile
    			//If left goal tile is already revealed, then reveal middle goalTile
    			return selectMapMove(mapmoves);
    		}else {
    			int[] goalTilePos = new int[2];
    			if(currentBoardState.isNuggetFound()) {
    				int goalIndex = currentBoardState.getNuggetIndex();
    				goalTilePos[0] = BoardState.hiddenPos[goalIndex][0];
    				goalTilePos[1] = BoardState.hiddenPos[goalIndex][1];
    			}else {
    				goalTilePos[0] = BoardState.hiddenPos[1][0];
    				goalTilePos[1] = BoardState.hiddenPos[1][1];
    			}
    			for(SaboteurMove move: list) {
    				//Process Each Move
    				
    				//Clone the resulting board
    				
    				//Create a OR node
    			}
    			return boardState.getRandomMove();
    		}
    	}else {
    		return boardState.getRandomMove();
    	}
    	
    	
//    	int alpha = Integer.MIN_VALUE;
//    	int beta = Integer.MAX_VALUE;
//    	SaboteurMove finalmove = list.get(0);
//    	
//    	for(SaboteurMove move : list) {
//    		currentBoardState.processMove(move);
//    		
//    		int value = MyTools.alpha_beta_pruning(alpha,beta,1,currentBoardState);
//    		//TODO: Clear last move
//    		
//    		if(value > alpha) {
//    			alpha = value;
//    			finalmove = move;
//    		}
//    		
//    		if(beta <= alpha) {
//				break;
//			}
//    	}
//    	
//    	Move myMove = finalmove;
//    	//Store current board
//    	lastBoardState = currentBoardState;
//    	
//        // Return your move to be processed by the server.
//        return myMove;
    }
    
    public ArrayList<SaboteurMove> getAllMapMoves(ArrayList<SaboteurMove> list) {
    	ArrayList<SaboteurMove> mapList = new ArrayList<>();
    	for(SaboteurMove move: list) {
    		SaboteurCard card = move.getCardPlayed();
    		if(card instanceof SaboteurMap) {
    			mapList.add(move);
    		}
    	}
    	return mapList;
    	
    }
    
    public SaboteurMove selectMapMove(ArrayList<SaboteurMove> list) {
    	int minPosY = 10;
    	int minIndex = -1;
    	int counter= 0;
    	if(list.size() == 1) {
    		return list.get(0);
    	}else {
    		for(SaboteurMove move:list) {
    			int pos[] = move.getPosPlayed();
    			int index = (pos[1]-3)/2;
    			boolean revealed = (this.playerNb == 1)?currentBoardState.player1hiddenRevealed[index]
    					:currentBoardState.player2hiddenRevealed[index];
    			if(pos[1]< minPosY && !revealed) {
    				minPosY = pos[1];
    				minIndex = counter;
    			}
    			counter++;
    		}
    	}
    	return list.get(minIndex);
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
    	this.currentBoardState = new BoardState(turnNumber,turnPlayer);
    	this.currentBoardState.fillTileBoardFromOriginalBoard(boardState.getHiddenBoard());
    	this.currentBoardState.setNbMalus(boardState.getNbMalus(1), boardState.getNbMalus(0));
    	this.currentBoardState.addCurrentPlayerHandCard(boardState.getCurrentPlayerCards());
    	this.currentBoardState.updateHiddenRevealedArray();
    }
    
}