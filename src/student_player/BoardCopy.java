package student_player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.*;
import boardgame.Board;
import boardgame.BoardState;

public class BoardCopy extends BoardState {
	
	private SaboteurTile[][] board;
	private int[][] intBoard;
	private int myId;
	private Random random = new Random();
	
	private int turnPlayer;
	private ArrayList<SaboteurCard> myCards;
	private ArrayList<SaboteurCard> enemyCards; //The rest of the deck
	private int myMalus;
	private int enemyMalus;
	private boolean[] myHiddenRevealed = {false,false,false};
	private boolean[] hiddenRevealed = {false,false,false};
	private SaboteurTile[] hiddenCards = new SaboteurTile[3];
	private int winner = Board.NOBODY;
	private boolean foundNugget = false;

	public BoardCopy(SaboteurTile[][] board, int[][] intBoard, ArrayList<SaboteurCard> cards, int id) {
		super();
		this.board = board;
		this.intBoard = intBoard;
		getIntBoard();
		myId = id;
		myCards = cards;
		enemyCards = SaboteurCard.getDeck(); //TODO: get enemy's moves
		enemyCards.removeAll(myCards);
		enemyCards.removeAll(MyTools.discard);
        
        //Getting nugget tile
		for(int i = 0; i < 3; i++) {
			if(board[SaboteurBoardState.hiddenPos[i][0]][SaboteurBoardState.hiddenPos[i][1]].getIdx().equals("nugget")) {
				board[SaboteurBoardState.hiddenPos[i][0]][SaboteurBoardState.hiddenPos[i][1]] = new SaboteurTile("nugget");
				hiddenCards[i] = board[SaboteurBoardState.hiddenPos[i][0]][SaboteurBoardState.hiddenPos[i][1]];
				foundNugget = true;
				break;
			}
		}
		if(!foundNugget) {
			ArrayList<Integer> unknown = new ArrayList<>();
			for(int i = 0; i < 3; i++) {
				if(!board[SaboteurBoardState.hiddenPos[i][0]][SaboteurBoardState.hiddenPos[i][1]].getIdx().equals("hidden1") && !board[SaboteurBoardState.hiddenPos[i][0]][SaboteurBoardState.hiddenPos[i][1]].getIdx().equals("hidden2")) {
					unknown.add(i);
				}
			}
			int nugget = unknown.get(random.nextInt(unknown.size()));
			board[SaboteurBoardState.hiddenPos[nugget][0]][SaboteurBoardState.hiddenPos[nugget][1]] = new SaboteurTile("nugget");
			hiddenCards[nugget] = board[SaboteurBoardState.hiddenPos[nugget][0]][SaboteurBoardState.hiddenPos[nugget][1]];
		}
		String[] hidden = {"hidden1", "hidden2"};
		int index = 0;
		for(int i = 0; i < 3; i++) {
			if(hiddenCards[i] == null) {
				hiddenCards[i] = new SaboteurTile(hidden[index++]);
			}
		}
	}
	
    public ArrayList<SaboteurMove> getAllLegalMoves() {
        // Given the current player hand, gives back all legal moves he can play.
        ArrayList<SaboteurCard> hand;
        boolean isBlocked;
        if(turnPlayer == myId){
            hand = myCards;
            isBlocked= myMalus > 0;
        }
        else {
            hand = enemyCards;
            isBlocked= enemyMalus > 0;
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
                if(turnPlayer ==myId){
                    if(myMalus > 0) legalMoves.add(new SaboteurMove(card,0,0,turnPlayer));
                }
                else if(enemyMalus>0) legalMoves.add(new SaboteurMove(card,0,0,turnPlayer));
            }
            else if(card instanceof SaboteurMalus){
                legalMoves.add(new SaboteurMove(card,0,0,turnPlayer));
            }
            else if(card instanceof SaboteurMap){
                for(int i =0;i<3;i++){ //for each hidden card that has not be revealed, we can still take a look at it.
                    if(! this.hiddenRevealed[i]) legalMoves.add(new SaboteurMove(card,SaboteurBoardState.hiddenPos[i][0],SaboteurBoardState.hiddenPos[i][1],turnPlayer));
                }
            }
            else if(card instanceof SaboteurDestroy){
                for (int i = 0; i < SaboteurBoardState.BOARD_SIZE; i++) {
                    for (int j = 0; j < SaboteurBoardState.BOARD_SIZE; j++) { //we can't destroy an empty tile, the starting, or final tiles.
                        if(this.board[i][j] != null && (i!=SaboteurBoardState.originPos || j!= SaboteurBoardState.originPos) && (i != SaboteurBoardState.hiddenPos[0][0] || j!=SaboteurBoardState.hiddenPos[0][1] )
                           && (i != SaboteurBoardState.hiddenPos[1][0] || j!=SaboteurBoardState.hiddenPos[1][1] ) && (i != SaboteurBoardState.hiddenPos[2][0] || j!=SaboteurBoardState.hiddenPos[2][1] ) ){
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
        for (int i = 0; i < SaboteurBoardState.BOARD_SIZE; i++) {
            for (int j = 0; j < SaboteurBoardState.BOARD_SIZE; j++) {
                if (this.board[i][j] != null) {
                    for (int m = 0; m < 4; m++) {
                        if (0 <= i+moves[m][0] && i+moves[m][0] < SaboteurBoardState.BOARD_SIZE && 0 <= j+moves[m][1] && j+moves[m][1] < SaboteurBoardState.BOARD_SIZE) {
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
    
    public boolean verifyLegit(int[][] path,int[] pos){
        // Given a tile's path, and a position to put this path, verify that it respects the rule of positionning;
        if (!(0 <= pos[0] && pos[0] < SaboteurBoardState.BOARD_SIZE && 0 <= pos[1] && pos[1] < SaboteurBoardState.BOARD_SIZE)) {
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
                objHiddenList.add(this.board[SaboteurBoardState.hiddenPos[i][0]][SaboteurBoardState.hiddenPos[i][1]]);
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
        if(pos[1]<SaboteurBoardState.BOARD_SIZE-1) {
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
        if(pos[0]<SaboteurBoardState.BOARD_SIZE-1) {
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
	
	public void processMove(SaboteurMove m) {
        //m != null only on the first move of the run
		if(m == null) {
			m = getRandomMove();
		}

        SaboteurCard testCard = m.getCardPlayed();
        int[] pos = m.getPosPlayed();

        if(testCard instanceof SaboteurTile){
            this.board[pos[0]][pos[1]] = new SaboteurTile(((SaboteurTile) testCard).getIdx());
            if(turnPlayer==myId){
                //Remove from the player card the card that was used.
                for(SaboteurCard card : myCards) {
                    if (card instanceof SaboteurTile) {
                        if (((SaboteurTile) card).getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                            myCards.remove(card);
                            break; //leave the loop....
                        }
                        else if(((SaboteurTile) card).getFlipped().getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                            myCards.remove(card);
                            break; //leave the loop....
                        }
                    }
                }
            }
            else { 
                for (SaboteurCard card : enemyCards) {
                    if (card instanceof SaboteurTile) {
                        if (((SaboteurTile) card).getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                        	enemyCards.remove(card);
                            break; //leave the loop....
                        }
                        else if(((SaboteurTile) card).getFlipped().getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                        	enemyCards.remove(card);
                            break; //leave the loop....
                        }
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurBonus){
            if(turnPlayer==myId){
                myMalus --;
                for(SaboteurCard card : myCards) {
                    if (card instanceof SaboteurBonus) {
                        myCards.remove(card);
                        break; //leave the loop....
                    }
                }
            }
            else{
                enemyMalus --;
                for(SaboteurCard card : enemyCards) {
                    if (card instanceof SaboteurBonus) {
                        enemyCards.remove(card);
                        break; //leave the loop....
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurMalus){
            if(turnPlayer==myId){
                enemyMalus ++;
                for(SaboteurCard card : myCards) {
                    if (card instanceof SaboteurMalus) {
                        myCards.remove(card);
                        break; //leave the loop....
                    }
                }
            }
            else{
                myMalus ++;
                for(SaboteurCard card : enemyCards) {
                    if (card instanceof SaboteurMalus) {
                        enemyCards.remove(card);
                        break; //leave the loop....
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurMap){
            if(turnPlayer==myId){
                for(SaboteurCard card : myCards) {
                    if (card instanceof SaboteurMap) {
                        myCards.remove(card);
                        int ph = 0;
                        for(int j=0;j<3;j++) {
                            if (pos[0] == SaboteurBoardState.hiddenPos[j][0] && pos[1] == SaboteurBoardState.hiddenPos[j][1]) ph=j;
                        }
                        myHiddenRevealed[ph] = true;
                        break; //leave the loop....
                    }
                }
            }
            else{
                for(SaboteurCard card : enemyCards) {
                    if (card instanceof SaboteurMap) {
                        enemyCards.remove(card);
                        int ph = 0;
                        for(int j=0;j<3;j++) {
                            if (pos[0] == SaboteurBoardState.hiddenPos[j][0] && pos[1] == SaboteurBoardState.hiddenPos[j][1]) ph=j;
                        }
                        //hiddenRevealed[ph] = true;
                        break; //leave the loop....
                    }
                }
            }
        }
        else if (testCard instanceof SaboteurDestroy) {
            int i = pos[0];
            int j = pos[1];
            if(turnPlayer==myId){
                for(SaboteurCard card : myCards) {
                    if (card instanceof SaboteurDestroy) {
                        myCards.remove(card);
                        this.board[i][j] = null;
                        break; //leave the loop....
                    }
                }
            }
            else{
                for(SaboteurCard card : enemyCards) {
                    if (card instanceof SaboteurDestroy) {
                        enemyCards.remove(card);
                        this.board[i][j] = null;
                        break; //leave the loop....
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurDrop){
            if(turnPlayer==myId) myCards.remove(pos[0]);
            else enemyCards.remove(pos[0]);
        }
        this.draw();
        this.updateWinner();
        turnPlayer = 1 - turnPlayer; // Swap player
        //turnNumber++;
    }
	
	private void draw(){
        if(enemyCards.size()>7){
            if(turnPlayer==myId){
                myCards.add(enemyCards.remove(random.nextInt(enemyCards.size())));
            }
        }
    }
	
	private void updateWinner() {

        pathToHidden(new SaboteurTile[]{new SaboteurTile("nugget"),new SaboteurTile("hidden1"),new SaboteurTile("hidden2")});
        int nuggetIdx = -1;
        for(int i =0;i<3;i++){
            if(hiddenCards[i].getIdx().equals("nugget")){
                nuggetIdx = i;
                break;
            }
        }
        boolean playerWin = hiddenRevealed[nuggetIdx];
        if (playerWin) { // Current player has won
            winner = turnPlayer;
        } else if (gameOver() && winner==Board.NOBODY) {
            winner = Board.DRAW;
        }
    }
	
	private Boolean cardPath(ArrayList<int[]> originTargets,int[] targetPos,Boolean usingCard){
        // the search algorithm, usingCard indicate weither we search a path of cards (true) or a path of ones (aka tunnel)(false).
        ArrayList<int[]> queue = new ArrayList<>(); //will store the current neighboring tile. Composed of position (int[]).
        ArrayList<int[]> visited = new ArrayList<int[]>(); //will store the visited tile with an Hash table where the key is the position the board.
        visited.add(targetPos);
        if(usingCard) addUnvisitedNeighborToQueue(targetPos,queue,visited,SaboteurBoardState.BOARD_SIZE,usingCard);
        else addUnvisitedNeighborToQueue(targetPos,queue,visited,SaboteurBoardState.BOARD_SIZE*3,usingCard);
        while(queue.size()>0){
            int[] visitingPos = queue.remove(0);
            if(containsIntArray(originTargets,visitingPos)){
                return true;
            }
            visited.add(visitingPos);
            if(usingCard) addUnvisitedNeighborToQueue(visitingPos,queue,visited,SaboteurBoardState.BOARD_SIZE,usingCard);
            else addUnvisitedNeighborToQueue(visitingPos,queue,visited,SaboteurBoardState.BOARD_SIZE*3,usingCard);
        }
        return false;
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
	
	private int[][] getIntBoard() {
        //update the int board.
        //Note that this tool is not available to the player.
        for (int i = 0; i < SaboteurBoardState.BOARD_SIZE; i++) {
            for (int j = 0; j < SaboteurBoardState.BOARD_SIZE; j++) {
                if(this.board[i][j] == null){
                    for (int k = 0; k < 3; k++) {
                        for (int h = 0; h < 3; h++) {
                            this.intBoard[i * 3 + k][j * 3 + h] = SaboteurBoardState.EMPTY;
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
        return this.intBoard; 
    }
	
	private boolean pathToHidden(SaboteurTile[] objectives){
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
        for(SaboteurTile target : objectives){
            ArrayList<int[]> originTargets = new ArrayList<>();
            originTargets.add(new int[]{SaboteurBoardState.originPos,SaboteurBoardState.originPos}); //the starting points
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
            if(!this.hiddenRevealed[currentTargetIdx]) {  //verify that the current target has not been already discovered. Even if there is a destruction event, the target keeps being revealed!

                if (cardPath(originTargets, targetPos, true)) { //checks that there is a cardPath
                    //next: checks that there is a path of ones.
                    ArrayList<int[]> originTargets2 = new ArrayList<>();
                    //the starting points
                    originTargets2.add(new int[]{SaboteurBoardState.originPos*3+1, SaboteurBoardState.originPos*3+1});
                    originTargets2.add(new int[]{SaboteurBoardState.originPos*3+1, SaboteurBoardState.originPos*3+2});
                    originTargets2.add(new int[]{SaboteurBoardState.originPos*3+1, SaboteurBoardState.originPos*3});
                    originTargets2.add(new int[]{SaboteurBoardState.originPos*3, SaboteurBoardState.originPos*3+1});
                    originTargets2.add(new int[]{SaboteurBoardState.originPos*3+2, SaboteurBoardState.originPos*3+1});
                    //get the target position in 0-1 coordinate
                    int[] targetPos2 = {targetPos[0]*3+1, targetPos[1]*3+1};
                    if (cardPath(originTargets2, targetPos2, false)) {
                        this.hiddenRevealed[currentTargetIdx] = true;
                        myHiddenRevealed[currentTargetIdx] = true;
                        atLeastOnefound =true;
                    }
                }
            }
            else{
                atLeastOnefound = true;
            }
        }
        return atLeastOnefound;
    }
	
	@Override
	public boolean isInitialized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getTurnPlayer() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTurnNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWinner() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setWinner(int winner) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int firstPlayer() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean gameOver() {
		return enemyCards.size()==0 && myCards.size()==0 || winner != Board.NOBODY;
	}

	@Override
    public SaboteurMove getRandomMove() {
        ArrayList<SaboteurMove> moves = getAllLegalMoves();
        return moves.get(random.nextInt(moves.size()));
    }

	public int run() {
		int id = 1 - myId; //Start with enemy turn
        while(winner == Board.NOBODY) {
            processMove(null);
            id = 1 - id;
        }
        if(winner == myId) {
        	return 1;
        }
        else {
        	return 0;
        }
	}
}
