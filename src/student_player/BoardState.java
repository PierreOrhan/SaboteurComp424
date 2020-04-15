package student_player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurBonus;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDestroy;
import Saboteur.cardClasses.SaboteurDrop;
import Saboteur.cardClasses.SaboteurMalus;
import Saboteur.cardClasses.SaboteurMap;
import Saboteur.cardClasses.SaboteurTile;
import boardgame.Board;

public class BoardState {
	public static final int BOARD_SIZE = 14;
    public static final int originPos = 5;

    public static final int EMPTY = -1;
    public static final int TUNNEL = 1;
    public static final int WALL = 0;

    private static int FIRST_PLAYER = 1;

    public SaboteurTile[][] board;
    public int[][] intBoard;
    //player variables:
    // Note: Player 1 is active when turnplayer is 1;
    public ArrayList<SaboteurCard> player1Cards; //hand of player 1
    public ArrayList<SaboteurCard> player2Cards; //hand of player 2
    private int player1nbMalus;
    private int player2nbMalus;
    public boolean[] player1hiddenRevealed = {false,false,false};
    public boolean[] player2hiddenRevealed = {false,false,false};

    private ArrayList<SaboteurCard> Deck; //deck form which player pick
    public int deckSize;
    public static final int[][] hiddenPos = {{originPos+7,originPos-2},{originPos+7,originPos},{originPos+7,originPos+2}};
    protected SaboteurTile[] hiddenCards = new SaboteurTile[3];
    public boolean[] hiddenRevealed = {false,false,false}; //whether hidden at pos1 is revealed, hidden at pos2 is revealed, hidden at pos3 is revealed.
    public Map<String,Integer> possibleDeckCards;
    private boolean existsAMapCard;
	private int nuggetIndex;


	private int turnPlayer;
    private int turnNumber;
    private int winner;
    private Random rand;

    BoardState(int turnNumber, int turnPlayer) {
        this.board = new SaboteurTile[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                this.board[i][j] = null;
            }
        }
        this.intBoard = new int[BOARD_SIZE*3][BOARD_SIZE*3];
        for (int i = 0; i < BOARD_SIZE*3; i++) {
            for (int j = 0; j < BOARD_SIZE*3; j++) {
                this.intBoard[i][j] = EMPTY;
            }
        }
        // initialize the hidden position:
 
        //initialize the entrance
        this.board[originPos][originPos] = new SaboteurTile("entrance");
        //TODO:initialize the deck.
        this.Deck = SaboteurCard.getDeck();
        //shuffle the deck.
        //initialize the player effects:
        player1nbMalus = 0;
        player2nbMalus = 0;
        //initialize the players hands:
        this.player1Cards = new ArrayList<SaboteurCard>();
        this.player2Cards = new ArrayList<SaboteurCard>();
        rand = new Random(2019);
        winner = Board.NOBODY;
        this.turnPlayer = turnPlayer;
        this.turnNumber = turnNumber;
        
        //Set current deck's size
        this.deckSize = (55 - 14) - this.turnNumber;
        this.possibleDeckCards = SaboteurCard.getDeckcomposition();
    }
    
    /**
     * Clone a board
     */
    public BoardState(BoardState boardState) {
    	this.board = new SaboteurTile[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                this.board[i][j] = null;
            }
        }
        this.possibleDeckCards = SaboteurCard.getDeckcomposition();
    	 fillTileBoardFromOriginalBoard(boardState.getHiddenBoard());
    	 this.intBoard = boardState.getIntBoard();
    	 this.turnNumber = boardState.getTurnNumber();
    	 this.turnPlayer = boardState.getTurnPlayer();
         winner = Board.NOBODY;
         //Set Player Maluses
         this.player1nbMalus = boardState.getNbMalus(1);
         this.player2nbMalus = boardState.getNbMalus(0);
         //Initialize Player Cards
         this.player1Cards = new ArrayList<SaboteurCard>();
         this.player2Cards = new ArrayList<SaboteurCard>();
         if(this.turnPlayer==1) {
        	 for(SaboteurCard card: boardState.player1Cards)
        		 this.player1Cards.add(card);
         }else {
        	 for(SaboteurCard card: boardState.player2Cards)
        		 this.player2Cards.add(card);
         }
         //Clone Player Hidden Arrays
         updatePlayerHiddenRevealedArray();
       //Set current deck's size
         this.deckSize = (55 - 14) - this.turnNumber;
         this.possibleDeckCards = new HashMap<String, Integer>();
         for(String key: boardState.possibleDeckCards.keySet()) {
        	 int num = boardState.possibleDeckCards.get(key);
        	 this.possibleDeckCards.put(key, num);
         }
    		 
    }
    
    /**
     * Update this.board from original saboteur board
     * @param board
     */
    public void fillTileBoardFromOriginalBoard(SaboteurTile[][] board) {
    	for(int i = 0; i < BOARD_SIZE; i++) {
    		for(int j = 0; j < BOARD_SIZE; j++) {
    			this.board[i][j] = board[i][j];
    			if(board[i][j]!=null&& !((i==originPos+7 && j == originPos+2)||(i==originPos+7 && j ==originPos)||(i==originPos+7 && j ==originPos-2))) {
    				String idx = board[i][j].getIdx();
    				if(idx.equals("8")||idx.equals("0")||idx.equals("1")||idx.equals("2")||idx.equals("3")||idx.equals("4")
    						||idx.equals("5")||idx.equals("6")||idx.equals("7")||idx.equals("9")||idx.equals("10")||
    						idx.equals("11")||idx.equals("12")||idx.equals("13")||idx.equals("14")||idx.equals("15")) {
    					int curNum = this.possibleDeckCards.get(idx);
        				this.possibleDeckCards.put(idx, curNum-1);
    				}
    				
    			}
    		}
    	}
    }
    
    
    /**
     * Remove last move
     */
    public void removeLastMove(SaboteurMove move) {
    	int x = move.getPosPlayed()[0];
    	int y = move.getPosPlayed()[1];
    	SaboteurCard card = move.getCardPlayed();
    	String idx = card.getName();
    	if(idx.charAt(0) == 'T') {
			idx = idx.substring(6,idx.length());
			this.board[x][y] = null;
		}else if(idx.equals("Malus")) {
			if(this.turnPlayer == 0)
				this.player2nbMalus--;
			else
				this.player1nbMalus--;
		}
		int curNum = this.possibleDeckCards.get(idx);
		this.possibleDeckCards.put(idx, curNum+1);
		this.turnPlayer = 1 - this.turnPlayer;
		this.turnNumber--;
		this.deckSize +=1;
		this.existsAMapCard = false;
    }
   
    public void updateHiddenRevealed() {
    	int originPos =5;
    	ArrayList<int[]> originTargets = new ArrayList<>();
		originTargets.add(new int[]{originPos*3+1, originPos*3+1});
        originTargets.add(new int[]{originPos*3+1, originPos*3+2});
        originTargets.add(new int[]{originPos*3+1, originPos*3});
        originTargets.add(new int[]{originPos*3, originPos*3+1});
        originTargets.add(new int[]{originPos*3+2, originPos*3+1});
        for(int h = 0;h<3;h++) {
        	int[] targetPos = new int[] {BoardState.hiddenPos[h][0],BoardState.hiddenPos[h][1]};
        	targetPos[0] = targetPos[0]*3+1;
        	targetPos[1] = targetPos[1]*3+1;
        	if(cardPath(originTargets,targetPos,false)) {
//        		System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n");
//        		System.out.println("hiddenReveal "+h+" is true");
        		hiddenRevealed[h] = true;
        	}
        }
    	
    }
    
    
    /**
     * Show if nugget is revealed by map card, if nugget is found then set the nuggetIndex
     * @return true if nugget found, false if not
     */
    public boolean isNuggetFound() {
    	//Nugget Revealed if map
    	if(this.board[originPos+7][originPos-2].getName().contains("nugget")){
    		nuggetIndex = 0;
    		return true;
    	}
    	if(this.board[originPos+7][originPos].getName().contains("nugget")) {
    		nuggetIndex = 1;
    		return true;
    	}
    	if(this.board[originPos+7][originPos+2].getName().contains("nugget")) {
    		nuggetIndex = 2;
    		return true;
    	}
    	//Nugget revealed by elminating options from playerRevealed
    	if(this.turnPlayer == 1) {
    		if(this.player1hiddenRevealed[0] && this.player1hiddenRevealed[1] && !this.player1hiddenRevealed[2]) {
    			nuggetIndex = 2;
    			return true;
    		}else if(this.player1hiddenRevealed[0] && !this.player1hiddenRevealed[1] && this.player1hiddenRevealed[2]) {
    			nuggetIndex = 1;
    			return true;
    		}else if(!this.player1hiddenRevealed[0] && this.player1hiddenRevealed[1] && this.player1hiddenRevealed[2]) {
    			nuggetIndex = 0;
    			return true;
    		}else if(this.player1hiddenRevealed[0] && this.hiddenRevealed[1] && !this.player1hiddenRevealed[2]) {
    			nuggetIndex = 2;
    			return true;
    		}else if(this.hiddenRevealed[0] && this.player1hiddenRevealed[1] && !this.player1hiddenRevealed[2]) {
    			nuggetIndex = 2;
    			return true;
    		}else if(this.hiddenRevealed[0] && this.player1hiddenRevealed[1] && !this.hiddenRevealed[2]) {
    			nuggetIndex = 2;
    			return true;
    		}else if(this.player1hiddenRevealed[0] && !this.player1hiddenRevealed[1] && this.hiddenRevealed[2]) {
    			nuggetIndex = 1;
    			return true;
    		}else if(this.hiddenRevealed[0] && !this.player1hiddenRevealed[1] && this.player1hiddenRevealed[2]) {
    			nuggetIndex = 1;
    			return true;
    		}else if(this.hiddenRevealed[0] && !this.player1hiddenRevealed[1] && this.hiddenRevealed[2]) {
    			nuggetIndex = 1;
    			return true;
    		}else if(!this.player1hiddenRevealed[0] && this.player1hiddenRevealed[1] && this.hiddenRevealed[2]) {
    			nuggetIndex = 0;
    			return true;
    		}else if(!this.player1hiddenRevealed[0] && this.hiddenRevealed[1] && this.player1hiddenRevealed[2]) {
    			nuggetIndex = 0;
    			return true;
    		}else if(!this.player1hiddenRevealed[0] && this.hiddenRevealed[1] && this.hiddenRevealed[2]) {
    			nuggetIndex = 0;
    			return true;
    		}
    		
    	}else{
    		if(this.player2hiddenRevealed[0] && this.player2hiddenRevealed[1] && !this.player2hiddenRevealed[2]) {
    			nuggetIndex = 2;
    			return true;
    		}else if(this.player2hiddenRevealed[0] && !this.player2hiddenRevealed[1] && this.player2hiddenRevealed[2]) {
    			nuggetIndex = 1;
    			return true;
    		}else if(!this.player2hiddenRevealed[0] && this.player2hiddenRevealed[1] && this.player2hiddenRevealed[2]) {
    			nuggetIndex = 0;
    			return true;
    		}else if(this.player2hiddenRevealed[0] && this.hiddenRevealed[1] && !this.player2hiddenRevealed[2]) {
    			nuggetIndex = 2;
    			return true;
    		}else if(this.hiddenRevealed[0] && this.player2hiddenRevealed[1] && !this.player2hiddenRevealed[2]) {
    			nuggetIndex = 2;
    			return true;
    		}else if(this.hiddenRevealed[0] && this.player2hiddenRevealed[1] && !this.hiddenRevealed[2]) {
    			nuggetIndex = 2;
    			return true;
    		}else if(this.player2hiddenRevealed[0] && !this.player2hiddenRevealed[1] && this.hiddenRevealed[2]) {
    			nuggetIndex = 1;
    			return true;
    		}else if(this.hiddenRevealed[0] && !this.player2hiddenRevealed[1] && this.player2hiddenRevealed[2]) {
    			nuggetIndex = 1;
    			return true;
    		}else if(this.hiddenRevealed[0] && !this.player2hiddenRevealed[1] && this.hiddenRevealed[2]) {
    			nuggetIndex = 1;
    			return true;
    		}else if(!this.player2hiddenRevealed[0] && this.player2hiddenRevealed[1] && this.hiddenRevealed[2]) {
    			nuggetIndex = 0;
    			return true;
    		}else if(!this.player2hiddenRevealed[0] && this.hiddenRevealed[1] && this.player2hiddenRevealed[2]) {
    			nuggetIndex = 0;
    			return true;
    		}else if(!this.player2hiddenRevealed[0] && this.hiddenRevealed[1] && this.hiddenRevealed[2]) {
    			nuggetIndex = 0;
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    
    /**
     * Show if any player has placed a tile at row > [originPos+3]
     * @return true if revealed, false if not
     */
    public boolean isRowBelowOriginPosPlus5Revealed() {
    	for(int j = 0; j < BOARD_SIZE; j++) {
    		if(this.board[originPos+5][j]!=null)
    			return true;
    	}
    	return false;
    }
    
    
    /**
     * Update Current Player Cards
     * @param playerCards
     */
    public void addCurrentPlayerHandCard(ArrayList<SaboteurCard> playerCards) {
    	if(this.turnPlayer == 1) {
    		for(SaboteurCard card: playerCards) {
        		this.player1Cards.add(card);
        		String idx = card.getName();
        		removeFromPossibleDeck(idx);
        	}
    	}else {
    		for(SaboteurCard card: playerCards) {
        		this.player2Cards.add(card);
        		String idx = card.getName();
        		removeFromPossibleDeck(idx);
        	}
    	}
    	
    }
    
    /**
     * Utility function for editing possibleDeckCard
     */
    public void removeFromPossibleDeck(String idx) {
    	if(idx.charAt(0) == 'T')
			idx = idx.substring(5,idx.length());
    	System.out.println("idx: "+idx);
		if(idx.equals("8")||idx.equals("0")||idx.equals("1")||idx.equals("2")||idx.equals("3")||idx.equals("4")
				||idx.equals("5")||idx.equals("6")||idx.equals("7")||idx.equals("9")||idx.equals("10")||
				idx.equals("11")||idx.equals("12")||idx.equals("13")||idx.equals("14")||idx.equals("15")
				) {
			int curNum = this.possibleDeckCards.get(idx);
			this.possibleDeckCards.put(idx, curNum-1);
		}
		if(idx.equals("Destroy")) {
			int curNum = this.possibleDeckCards.get("destroy");
			this.possibleDeckCards.put(idx, curNum-1);
		}
		if(idx.equals("Malus")) {
			int curNum = this.possibleDeckCards.get("malus");
			this.possibleDeckCards.put(idx, curNum-1);
		}
		if(idx.equals("Bonus")) {
			int curNum = this.possibleDeckCards.get("bonus");
			this.possibleDeckCards.put(idx, curNum-1);
		}
		if(idx.equals("Map")) {
			int curNum = this.possibleDeckCards.get("map");
			this.possibleDeckCards.put(idx, curNum-1);
		}
    }
    
    /**
     * Update playerHiddenReaveled Array
     */
    public void updatePlayerHiddenRevealedArray() {
    	for(int h=0;h<3;h++){
            if(!this.board[hiddenPos[h][0]][hiddenPos[h][1]].getName().contains("8")){
            	if(this.turnPlayer == 1) {
            		this.player1hiddenRevealed[h] = true;
            		if(board[hiddenPos[h][0]][hiddenPos[h][1]].getName().contains("nugget")) {
            			this.hiddenCards[h] = new SaboteurTile("nugget");
            		}else if(board[hiddenPos[h][0]][hiddenPos[h][1]].getName().contains("hidden1")){
            			this.hiddenCards[h] = new SaboteurTile("hidden1");
            		}else {
            			this.hiddenCards[h] = new SaboteurTile("hidden2");
            		}
            	}else {
            		this.player2hiddenRevealed[h] = true;
            		if(board[hiddenPos[h][0]][hiddenPos[h][1]].getName().contains("nugget")) {
            			this.hiddenCards[h] = new SaboteurTile("nugget");
            		}else if(board[hiddenPos[h][0]][hiddenPos[h][1]].getName().contains("hidden1")){
            			this.hiddenCards[h] = new SaboteurTile("hidden1");
            		}else {
            			this.hiddenCards[h] = new SaboteurTile("hidden2");
            		}
            	}
            }else {
            	this.hiddenCards[h] = new SaboteurTile("hidden"); //Notify pathToHidden this is not revealed yet
            }
        }
    }
    
    /**
     * Set Players' Num of Maluses
     * @param numMalus1
     * @param numMalus2
     */
    public void setNbMalus(int numMalus1, int numMalus2) {
        this.player1nbMalus = numMalus1;
        this.player2nbMalus = numMalus2;
        int curNum = this.possibleDeckCards.get("malus");
        this.possibleDeckCards.put("malus", curNum - numMalus1 - numMalus2);
    }
    
    
    //Fill the deck
    public void randomizeDeck() {
    	//Infer Cards from board, numOfMalus and studentRecord
    	
    	//Naively fill deck with possible deck cards
    
    	//If board tile not equal to empty, remove this from deck
    	
    	//infer opponents's hand
    	
    }
    
    public SaboteurTile[][] getHiddenBoard(){
        // returns the board in SaboteurTile format, where the objectives become the 8 tiles.
        // Note the inconsistency with the getHiddenIntBoard where the objectives become only -1
        // this is to stress that hidden cards are considered as empty cards which you can't either destroy or build on before they
        // are revealed.
        SaboteurTile[][] hiddenboard = new SaboteurTile[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(this.board[i], 0, hiddenboard[i], 0, BOARD_SIZE);
        }
        for(int h=0;h<3;h++){
            if(turnPlayer==1 && !player1hiddenRevealed[h] || turnPlayer==0 && !player2hiddenRevealed[h]){
                hiddenboard[hiddenPos[h][0]][hiddenPos[h][1]] = new SaboteurTile("8");
            }
        }
        return hiddenboard;
    }
    
    public int[][] getHiddenIntBoard() {
        //update the int board, and provide it to the player with the hidden objectives set at EMPTY.
        //Note that this function is available to the player.
        boolean[] listHiddenRevealed;
        if(turnPlayer==1) listHiddenRevealed= player1hiddenRevealed;
        else listHiddenRevealed = player2hiddenRevealed;
        int[][] intBoard = new int[BOARD_SIZE*3][BOARD_SIZE*3];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if(this.board[i][j] == null){
                    for (int k = 0; k < 3; k++) {
                        for (int h = 0; h < 3; h++) {
                            intBoard[i * 3 + k][j * 3 + h] = EMPTY;
                        }
                    }
                }
                else {
                    
                        int[][] path = this.board[i][j].getPath();
                        for (int k = 0; k < 3; k++) {
                            for (int h = 0; h < 3; h++) {
                                intBoard[i * 3 + k][j * 3 + h] = path[h][2-k];
                            }
                        }
                    
                }
            }
        }

        return intBoard; 
        }
    
    public void processMove(SaboteurMove m, boolean switchPlayer) throws IllegalArgumentException {
//
//        if(m.getFromBoard()){
//            this.initializeFromStringForInitialCopy(m.getBoardInit());
//            System.out.println("inititalized"+this.hashCode());
//            turnNumber++;
//            return;
//        }

        // Verify that a move is legal (if not throw an IllegalArgumentException)
        // And then execute the move.
        // Concerning the map observation, the player then has to check by himself the result of its observation.
        //Note: this method is ran in a BoardState ran by the server as well as in a BoardState ran by the player.
        if (!isLegal(m)) {
//            System.out.println("Found an invalid Move for player " + this.turnPlayer+" of board"+ this.hashCode());
//            ArrayList<SaboteurCard> hand = this.turnPlayer==1? this.player1Cards : this.player2Cards;
//            System.out.println("in hand:");
//            for(SaboteurCard card : hand) {
//                if (card instanceof SaboteurTile){
//                    System.out.println(card.getName());
//                }
//                else{
//                    System.out.println(card.getName());
//                }
//            }
//            throw new IllegalArgumentException("Invalid move. Move: " + m.toPrettyString());
        }

        SaboteurCard testCard = m.getCardPlayed();
        int[] pos = m.getPosPlayed();

        if(testCard instanceof SaboteurTile){
            this.board[pos[0]][pos[1]] = new SaboteurTile(((SaboteurTile) testCard).getIdx());
            if(turnPlayer==1){
                //Remove from the player card the card that was used.
                for(SaboteurCard card : this.player1Cards) {
                    if (card instanceof SaboteurTile) {
                        if (((SaboteurTile) card).getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                            this.player1Cards.remove(card);
                            break; //leave the loop....
                        }
                        else if(((SaboteurTile) card).getFlipped().getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                            this.player1Cards.remove(card);
                            break; //leave the loop....
                        }
                    }
                }
            }
            else {
                for (SaboteurCard card : this.player2Cards) {
                    if (card instanceof SaboteurTile) {
                        if (((SaboteurTile) card).getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                            this.player2Cards.remove(card);
                            break; //leave the loop....
                        }
                        else if(((SaboteurTile) card).getFlipped().getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                            this.player2Cards.remove(card);
                            break; //leave the loop....
                        }
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurBonus){
            if(turnPlayer==1){
                player1nbMalus --;
                for(SaboteurCard card : this.player1Cards) {
                    if (card instanceof SaboteurBonus) {
                        this.player1Cards.remove(card);
                        break; //leave the loop....
                    }
                }
            }
            else{
                player2nbMalus --;
                for(SaboteurCard card : this.player2Cards) {
                    if (card instanceof SaboteurBonus) {
                        this.player2Cards.remove(card);
                        break; //leave the loop....
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurMalus){
            if(turnPlayer==1){
                player2nbMalus ++;
                for(SaboteurCard card : this.player1Cards) {
                    if (card instanceof SaboteurMalus) {
                        this.player1Cards.remove(card);
                        break; //leave the loop....
                    }
                }
            }
            else{
                player1nbMalus ++;
                for(SaboteurCard card : this.player2Cards) {
                    if (card instanceof SaboteurMalus) {
                        this.player2Cards.remove(card);
                        break; //leave the loop....
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurMap){
            if(turnPlayer==1){
                for(SaboteurCard card : this.player1Cards) {
                    if (card instanceof SaboteurMap) {
                        this.player1Cards.remove(card);
                        int ph = 0;
                        for(int j=0;j<3;j++) {
                            if (pos[0] == hiddenPos[j][0] && pos[1] == hiddenPos[j][1]) ph=j;
                        }
                        this.player1hiddenRevealed[ph] = true;
                        break; //leave the loop....
                    }
                }
            }
            else{
                for(SaboteurCard card : this.player2Cards) {
                    if (card instanceof SaboteurMap) {
                        this.player2Cards.remove(card);
                        int ph = 0;
                        for(int j=0;j<3;j++) {
                            if (pos[0] == hiddenPos[j][0] && pos[1] == hiddenPos[j][1]) ph=j;
                        }
                        this.player2hiddenRevealed[ph] = true;
                        break; //leave the loop....
                    }
                }
            }
        }
        else if (testCard instanceof SaboteurDestroy) {
            int i = pos[0];
            int j = pos[1];
            if(turnPlayer==1){
                for(SaboteurCard card : this.player1Cards) {
                    if (card instanceof SaboteurDestroy) {
                        this.player1Cards.remove(card);
                        this.board[i][j] = null;
                        break; //leave the loop....
                    }
                }
            }
            else{
                for(SaboteurCard card : this.player2Cards) {
                    if (card instanceof SaboteurDestroy) {
                        this.player2Cards.remove(card);
                        this.board[i][j] = null;
                        break; //leave the loop....
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurDrop){
            if(turnPlayer==1) this.player1Cards.remove(pos[0]);
            else this.player2Cards.remove(pos[0]);
        }
        //TODO
        this.updateWinner();
        if(switchPlayer) {
        	this.draw();
        	turnPlayer = 1 - turnPlayer; // Swap player
        }
        turnNumber++;
    }
    
    /**
     * TODO:This part needs to be changed greatly to conform to uncertainty of Hidden Cards
     */
    private void updateWinner() {

        pathToHidden(new SaboteurTile[]{new SaboteurTile("nugget"),new SaboteurTile("hidden1"),new SaboteurTile("hidden2")});
        int nuggetIdx = -1;
        for(int i =0;i<3;i++){
            if(this.hiddenCards[i].getIdx().equals("nugget")){
                nuggetIdx = i;
                break;
            }
        }
        if(nuggetIdx != -1) {
        	boolean playerWin = this.hiddenRevealed[nuggetIdx];
            if (playerWin) { // Current player has won
                winner = turnPlayer;
            } else if (gameOver() && winner==Board.NOBODY) {
                winner = Board.DRAW;
            }
        }else {
        	winner = Board.NOBODY;
        }
        

    }
    
    public int getTurnPlayer() {
    	return this.turnPlayer;
    }
    
    public int getNbMalus(int playerNb){
        if(playerNb==1) return this.player1nbMalus;
        return this.player2nbMalus;
    }
    
    private void draw(){
        if(this.deckSize>0){
            if(turnPlayer==1){
                this.player1Cards.add(this.Deck.remove(0));
            }
            else{
                this.player2Cards.add(this.Deck.remove(0));
            }
            this.deckSize--;
        }
    }
    
    public int getWinner() { return winner; }
    
    private int[][] getIntBoard() {
        //update the int board.
        //Note that this tool is not available to the player.
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if(this.board[i][j] == null){
                    for (int k = 0; k < 3; k++) {
                        for (int h = 0; h < 3; h++) {
                            this.intBoard[i * 3 + k][j * 3 + h] = EMPTY;
                        }
                    }
                }
                else {
                    int[][] path = this.board[i][j].getPath();
                    for (int k = 0; k < 3; k++) {
                        for (int h = 0; h < 3; h++) {
                            this.intBoard[i * 3 + k][j * 3 + h] = path[h][2-k];
                        }
                    }
                }
            }
        }

        return this.intBoard; }
    
    
    public boolean pathToHidden(SaboteurTile[] objectives){
        /* This function look if a path is linking the starting point to the states among objectives.
            :return: if there exists one: true
                     if not: false
                     In Addition it changes each reached states hidden variable to true:  self.hidden[foundState] <- true
            Implementation details:
            For each hidden objectives:
                We verify there is a path of cards between the start and the hidden objectives.
                    If there is one, we do the same but with the 0-1s matrix!

            To verify a path, we use a simple search algorithm where we propagate a front of visited neighbor.
               TODO To speed up: The neighbor are added ranked on their distance to the origin... (simply use a PriorityQueue with a Comparator)
        */
        this.getIntBoard(); //update the int board.
        boolean atLeastOnefound = false;
        int counter = 0;
        for(SaboteurTile target : objectives){
            ArrayList<int[]> originTargets = new ArrayList<>();
            originTargets.add(new int[]{originPos,originPos}); //the starting points
            //get the target position
            int[] targetPos = {0,0};
            int currentTargetIdx = -1;
            for(int i =0;i<3;i++){
                if(this.hiddenCards[i].getIdx().equals(target.getIdx())){
                    targetPos = SaboteurBoardState.hiddenPos[i];
                    currentTargetIdx = i;
                    break;
                }
            }
            
            if(currentTargetIdx!=-1 && !this.hiddenRevealed[currentTargetIdx]) {  //verify that the current target has not been already discovered. Even if there is a destruction event, the target keeps being revealed!

                if (cardPath(originTargets, targetPos, true)) { //checks that there is a cardPath
                    //next: checks that there is a path of ones.
                    ArrayList<int[]> originTargets2 = new ArrayList<>();
                    //the starting points
                    originTargets2.add(new int[]{originPos*3+1, originPos*3+1});
                    originTargets2.add(new int[]{originPos*3+1, originPos*3+2});
                    originTargets2.add(new int[]{originPos*3+1, originPos*3});
                    originTargets2.add(new int[]{originPos*3, originPos*3+1});
                    originTargets2.add(new int[]{originPos*3+2, originPos*3+1});
                    //get the target position in 0-1 coordinate
                    int[] targetPos2 = {targetPos[0]*3+1, targetPos[1]*3+1};
                    if (cardPath(originTargets2, targetPos2, false)) {

                        this.hiddenRevealed[currentTargetIdx] = true;
                        this.player1hiddenRevealed[currentTargetIdx] = true;
                        this.player2hiddenRevealed[currentTargetIdx] = true;
                        
                        atLeastOnefound =true;
                    }
                    else{
                    }
                }
            }
            else{
                atLeastOnefound = true;
            }
        }
        return atLeastOnefound;
    }
    
    public Boolean cardPath(ArrayList<int[]> originTargets,int[] targetPos,Boolean usingCard){
        // the search algorithm, usingCard indicate weither we search a path of cards (true) or a path of ones (aka tunnel)(false).
        ArrayList<int[]> queue = new ArrayList<>(); //will store the current neighboring tile. Composed of position (int[]).
        ArrayList<int[]> visited = new ArrayList<int[]>(); //will store the visited tile with an Hash table where the key is the position the board.
        visited.add(targetPos);
        if(usingCard) addUnvisitedNeighborToQueue(targetPos,queue,visited,BOARD_SIZE,usingCard);
        else addUnvisitedNeighborToQueue(targetPos,queue,visited,BOARD_SIZE*3,usingCard);
        while(queue.size()>0){
            int[] visitingPos = queue.remove(0);
            if(containsIntArray(originTargets,visitingPos)){
                return true;
            }
            visited.add(visitingPos);
            if(usingCard) addUnvisitedNeighborToQueue(visitingPos,queue,visited,BOARD_SIZE,usingCard);
            else addUnvisitedNeighborToQueue(visitingPos,queue,visited,BOARD_SIZE*3,usingCard);
        }
        return false;
    }
    
    private void addUnvisitedNeighborToQueue(int[] pos,ArrayList<int[]> queue, ArrayList<int[]> visited,int maxSize,boolean usingCard){
        int[][] moves = {{0, -1},{0, 1},{1, 0},{-1, 0}};
        int i = pos[0];
        int j = pos[1];
        for (int m = 0; m < 4; m++) {
            if (0 <= i+moves[m][0] && i+moves[m][0] < maxSize && 0 <= j+moves[m][1] && j+moves[m][1] < maxSize) { //if the hypothetical neighbor is still inside the board
                int[] neighborPos = new int[]{i+moves[m][0],j+moves[m][1]};
                if(!containsIntArray(visited,neighborPos)){
                    if(usingCard && this.board[neighborPos[0]][neighborPos[1]]!=null) queue.add(neighborPos);
                    else if(!usingCard && this.intBoard[neighborPos[0]][neighborPos[1]]==1) queue.add(neighborPos);
                }
            }
        }
    }
    
    private boolean containsIntArray(ArrayList<int[]> a,int[] o){ //the .equals used in Arraylist.contains is not working between arrays..
        if (o == null) {
            for (int i = 0; i < a.size(); i++) {
                if (a.get(i) == null)
                    return true;
            }
        } else {
            for (int i = 0; i < a.size(); i++) {
                if (Arrays.equals(o, a.get(i)))
                    return true;
            }
        }
        return false;
    }
    
    public boolean gameOver() {
        return this.deckSize ==0 && this.player1Cards.size()==0 && this.player2Cards.size()==0 || winner != Board.NOBODY;
    }
    
    public ArrayList<SaboteurMove> getAllLegalMoves() {
        // Given the current player hand, gives back all legal moves he can play.
        ArrayList<SaboteurCard> hand;
        boolean isBlocked;
        if(turnPlayer == 1){
            hand = this.player1Cards;
            isBlocked= player1nbMalus > 0;
        }
        else {
            hand = this.player2Cards;
            isBlocked= player2nbMalus > 0;
        }

        ArrayList<SaboteurMove> legalMoves = new ArrayList<>();

        for(SaboteurCard card : hand){
            if( card instanceof SaboteurTile && !isBlocked) {
                ArrayList<int[]> allowedPositions = possiblePositions((SaboteurTile)card);
                for(int[] pos:allowedPositions){
                    legalMoves.add(new SaboteurMove(card,pos[0],pos[1],turnPlayer));
                }
                //if the card can be flipped, we also had legal moves where the card is flipped;
                if(SaboteurTile.canBeFlipped(((SaboteurTile)card).getIdx())){
                    SaboteurTile flippedCard = ((SaboteurTile)card).getFlipped();
                    ArrayList<int[]> allowedPositionsflipped = possiblePositions(flippedCard);
                    for(int[] pos:allowedPositionsflipped){
                        legalMoves.add(new SaboteurMove(flippedCard,pos[0],pos[1],turnPlayer));
                    }
                }
            }
            else if(card instanceof SaboteurBonus){
                if(turnPlayer ==1){
                    if(player1nbMalus > 0) legalMoves.add(new SaboteurMove(card,0,0,turnPlayer));
                }
                else if(player2nbMalus>0) legalMoves.add(new SaboteurMove(card,0,0,turnPlayer));
            }
            else if(card instanceof SaboteurMalus){
                legalMoves.add(new SaboteurMove(card,0,0,turnPlayer));
            }
            else if(card instanceof SaboteurMap){
            	this.existsAMapCard = true;
                for(int i =0;i<3;i++){ //for each hidden card that has not be revealed, we can still take a look at it.
                    if(! this.hiddenRevealed[i]) legalMoves.add(new SaboteurMove(card,hiddenPos[i][0],hiddenPos[i][1],turnPlayer));
                }
            }
            else if(card instanceof SaboteurDestroy){
                for (int i = 0; i < BOARD_SIZE; i++) {
                    for (int j = 0; j < BOARD_SIZE; j++) { //we can't destroy an empty tile, the starting, or final tiles.
                        if(this.board[i][j] != null && (i!=originPos || j!= originPos) && (i != hiddenPos[0][0] || j!=hiddenPos[0][1] )
                           && (i != hiddenPos[1][0] || j!=hiddenPos[1][1] ) && (i != hiddenPos[2][0] || j!=hiddenPos[2][1] ) ){
                            legalMoves.add(new SaboteurMove(card,i,j,turnPlayer));
                        }
                    }
                }
            }
        }
        // we can also drop any of the card in our hand
        for(int i=0;i<hand.size();i++) {
            legalMoves.add(new SaboteurMove(new SaboteurDrop(), i, 0, turnPlayer));
        }
        return legalMoves;
    }
    
    public ArrayList<int[]> possiblePositions(SaboteurTile card) {
        // Given a card, returns all the possiblePositions at which the card could be positioned in an ArrayList of int[];
        // Note that the card will not be flipped in this test, a test for the flipped card should be made by giving to the function the flipped card.
        ArrayList<int[]> possiblePos = new ArrayList<int[]>();
        int[][] moves = {{0, -1},{0, 1},{1, 0},{-1, 0}}; //to make the test faster, we simply verify around all already placed tiles.
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (this.board[i][j] != null) {
                    for (int m = 0; m < 4; m++) {
                        if (0 <= i+moves[m][0] && i+moves[m][0] < BOARD_SIZE && 0 <= j+moves[m][1] && j+moves[m][1] < BOARD_SIZE) {
                            if (this.verifyLegit(card.getPath(), new int[]{i + moves[m][0], j + moves[m][1]} )){
                                possiblePos.add(new int[]{i + moves[m][0], j +moves[m][1]});
                            }
                        }
                    }
                }
            }
        }
        return possiblePos;
    }
    
    public boolean isLegal(SaboteurMove m) {
        // For a move to be legal, the player must have the card in its hand
        // and then the game rules apply.
        // Note that we do not test the flipped version. To test it: use the flipped card in the SaboteurMove object.

        SaboteurCard testCard = m.getCardPlayed();
        int[] pos = m.getPosPlayed();
        int currentPlayer = m.getPlayerID();
        if (currentPlayer != turnPlayer) {
        	//System.out.println("This is why");
        	return false;}

        ArrayList<SaboteurCard> hand;
        boolean isBlocked;
        if(turnPlayer == 1){
            hand = this.player1Cards;
            isBlocked= player1nbMalus > 0;
        }
        else {
            hand = this.player2Cards;
            isBlocked= player2nbMalus > 0;
        }
        if(testCard instanceof SaboteurDrop){
            if(hand.size()>=pos[0]){
                return true;
            }
        }
        boolean legal = false;
        for(SaboteurCard card : hand){
            if (card instanceof SaboteurTile && testCard instanceof SaboteurTile && !isBlocked) {
                if(((SaboteurTile) card).getIdx().equals(((SaboteurTile) testCard).getIdx())){
                    return verifyLegit(((SaboteurTile) card).getPath(),pos);
                }
                else if(((SaboteurTile) card).getFlipped().getIdx().equals(((SaboteurTile) testCard).getIdx())){
                    return verifyLegit(((SaboteurTile) card).getFlipped().getPath(),pos);
                }
            }
            else if (card instanceof SaboteurBonus && testCard instanceof SaboteurBonus) {
                if (turnPlayer == 1) {
                    if (player1nbMalus > 0) return true;
                } else if (player2nbMalus > 0) return true;
                return false;
            }
            else if (card instanceof SaboteurMalus && testCard instanceof SaboteurMalus ) {
                return true;
            }
            else if (card instanceof SaboteurMap && testCard instanceof SaboteurMap) {
                int ph = 0;
                for(int j=0;j<3;j++) {
                    if (pos[0] == hiddenPos[j][0] && pos[1] == hiddenPos[j][1]) ph=j;
                }
                if (!this.hiddenRevealed[ph])
                    return true;
            }
            else if (card instanceof SaboteurDestroy && testCard instanceof SaboteurDestroy) {
                int i = pos[0];
                int j = pos[1];
                if (this.board[i][j] != null && (i != originPos || j != originPos) && (i != hiddenPos[0][0] || j != hiddenPos[0][1])
                        && (i != hiddenPos[1][0] || j != hiddenPos[1][1]) && (i != hiddenPos[2][0] || j != hiddenPos[2][1])) {
                    return true;
                }
            }
        }
        return legal;
    }
    
    public boolean verifyLegit(int[][] path,int[] pos){
        // Given a tile's path, and a position to put this path, verify that it respects the rule of positionning;
        if (!(0 <= pos[0] && pos[0] < BOARD_SIZE && 0 <= pos[1] && pos[1] < BOARD_SIZE)) {
            return false;
        }
        if(board[pos[0]][pos[1]] != null) return false;

        //the following integer are used to make sure that at least one path exists between the possible new tile to be added and existing tiles.
        // There are 2 cases:  a tile can't be placed near an hidden objective and a tile can't be connected only by a wall to another tile.
        int requiredEmptyAround=4;
        int numberOfEmptyAround=0;

        ArrayList<SaboteurTile> objHiddenList=new ArrayList<>();
        for(int i=0;i<3;i++) {
            if (!hiddenRevealed[i]){
                objHiddenList.add(this.board[hiddenPos[i][0]][hiddenPos[i][1]]);
            }
        }
        //verify left side:
        if(pos[1]>0) {
            SaboteurTile neighborCard = this.board[pos[0]][pos[1] - 1];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                if (path[0][0] != neighborPath[2][0] || path[0][1] != neighborPath[2][1] || path[0][2] != neighborPath[2][2] ) return false;
                else if(path[0][0] == 0 && path[0][1]== 0 && path[0][2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify right side
        if(pos[1]<BOARD_SIZE-1) {
            SaboteurTile neighborCard = this.board[pos[0]][pos[1] + 1];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                if (path[2][0] != neighborPath[0][0] || path[2][1] != neighborPath[0][1] || path[2][2] != neighborPath[0][2]) return false;
                else if(path[2][0] == 0 && path[2][1]== 0 && path[2][2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify upper side
        if(pos[0]>0) {
            SaboteurTile neighborCard = this.board[pos[0]-1][pos[1]];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                int[] p={path[0][2],path[1][2],path[2][2]};
                int[] np={neighborPath[0][0],neighborPath[1][0],neighborPath[2][0]};
                if (p[0] != np[0] || p[1] != np[1] || p[2] != np[2]) return false;
                else if(p[0] == 0 && p[1]== 0 && p[2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify bottom side:
        if(pos[0]<BOARD_SIZE-1) {
            SaboteurTile neighborCard = this.board[pos[0]+1][pos[1]];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                int[] p={path[0][0],path[1][0],path[2][0]};
                int[] np={neighborPath[0][2],neighborPath[1][2],neighborPath[2][2]};
                if (p[0] != np[0] || p[1] != np[1] || p[2] != np[2]) return false;
                else if(p[0] == 0 && p[1]== 0 && p[2] ==0 ) numberOfEmptyAround +=1; //we are touching by a wall
            }
        }
        else numberOfEmptyAround+=1;

        if(numberOfEmptyAround==requiredEmptyAround)  return false;

        return true;
    }
    
    public ArrayList<SaboteurCard> getCurrentPlayerCards(){
        if(turnPlayer==1){
            ArrayList<SaboteurCard> p1Cards = new ArrayList<SaboteurCard>();
            for(int i=0;i<this.player1Cards.size();i++){
                p1Cards.add(i,SaboteurCard.copyACard(this.player1Cards.get(i).getName()));
            }
            return p1Cards;
        }
        else{
            ArrayList<SaboteurCard> p2Cards = new ArrayList<SaboteurCard>();
            for(int i=0;i<this.player2Cards.size();i++){
                p2Cards.add(i,SaboteurCard.copyACard(this.player2Cards.get(i).getName()));
            }
            return p2Cards;
        }
    }
    public boolean existsAMapCard() {
		return existsAMapCard;
	}
    
    public int getNuggetIndex() {
		return nuggetIndex;
	}
    
    public int getTurnNumber() {
		return this.turnNumber;
	}
}
