package Saboteur;

import Saboteur.cardClasses.*;
import boardgame.Board;
import boardgame.BoardState;
import boardgame.Move;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * @author Pierre, adapted from mgrenander work on pentagoswap.
 */
public class SaboteurBoardState extends BoardState {
    public static final int BOARD_SIZE = 29;
    public static final int originPos = 14;

    public static final int EMPTY = -1;
    public static final int TUNNEL = 1;
    public static final int WALL = 0;

    private static int FIRST_PLAYER = 1;

    private SaboteurTile[][] board;
    private int[][] intBoard;
    //player variables:
    // Note: Player 1 is active when turnplayer is 1;
    private ArrayList<SaboteurCard> player1Cards; //hand of player 1
    private ArrayList<SaboteurCard> player2Cards; //hand of player 2
    private int player1nbMalus;
    private int player2nbMalus;
    private boolean[] player1hiddenRevealed = {false,false,false};
    private boolean[] player2hiddenRevealed = {false,false,false};

    private ArrayList<SaboteurCard> Deck; //deck form which player pick
    public static final int[][] hiddenPos = {{originPos+7,originPos-2},{originPos+7,originPos},{originPos+7,originPos+2}};
    private SaboteurTile[] hiddenCards = new SaboteurTile[3];
    private boolean[] hiddenRevealed = {false,false,false}; //weither hidden at pos1 is revealed, hidden at pos2 is revealed, hidden at pos3 is revealed.


    private int turnPlayer;
    private int turnNumber;
    private int winner;
    private Random rand;

    SaboteurBoardState() {
        super();
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
        // initialize the starting position:
        ArrayList<String> list =new ArrayList<String>();
        list.add("hidden1");
        list.add("hidden2");
        list.add("nugget");
        Random startRand = new Random();
        for(int i = 0; i < 3; i++){
            int idx = startRand.nextInt(list.size());
            this.board[hiddenPos[i][0]][hiddenPos[i][1]] = new SaboteurTile(list.remove(idx));
            this.hiddenCards[i] = this.board[hiddenPos[i][0]][hiddenPos[i][1]];
        }
        //initialize the deck.
        Deck = SaboteurCard.getDeck();
        //shuffle the deck.
        //TO DO: shuffle so that every player will get at least a forward tile during the game...
        Collections.shuffle(Deck);

        //initialize the player effects:
        player1nbMalus = 0;
        player2nbMalus = 0;
        //initialize the players hands:
        turnPlayer = FIRST_PLAYER;
        for(int i=0;i<7;i++){
            this.draw();
            turnPlayer = 1-turnPlayer;
            this.draw();
            turnPlayer = 1-turnPlayer;
        }
        rand = new Random(2019);
        winner = Board.NOBODY;
        turnPlayer = FIRST_PLAYER;
        turnNumber = 0;
    }

    // For cloning
    private SaboteurBoardState(SaboteurBoardState pbs) {
        super();
        this.board = new SaboteurTile[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(pbs.board[i], 0, this.board[i], 0, BOARD_SIZE);
        }
        rand = new Random(2019);
        this.winner = pbs.winner;
        this.turnPlayer = pbs.turnPlayer;
        this.turnNumber = pbs.turnNumber;
    }

    SaboteurTile[][] getBoard() { return this.board; }
    private int[][] getIntBoard() {
        //update the int board.
        //Note that this tool is not available to the player.
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; i < BOARD_SIZE; i++) {
                if(this.board[i][j] == null){
                    for (int k = 0; i < 3; i++) {
                        for (int h = 0; i < 3; i++) {
                            this.intBoard[i * 3 + k][j * 3 + h] = EMPTY;
                        }
                    }
                }
                else {
                    int[][] path = this.board[i][j].getPath();
                    for (int k = 0; i < 3; i++) {
                        for (int h = 0; i < 3; i++) {
                            this.intBoard[i * 3 + k][j * 3 + h] = path[k][h];
                        }
                    }
                }
            }
        }

        return this.intBoard; }
    public int[][] getHiddenIntBoard() {
        //update the int board, and provide it to the player with the hidden objectives set at EMPTY.
        //Note that this function is available to the player.
        boolean[] listHiddenRevealed;
        if(turnPlayer==1) listHiddenRevealed= player1hiddenRevealed;
        else listHiddenRevealed = player2hiddenRevealed;

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
                    boolean isAnHiddenObjective = false;
                    for(int h=0;h<3;h++) {
                        if(this.board[i][j].getIdx().equals(this.hiddenCards[h].getIdx())){
                            if(!listHiddenRevealed[h]){
                                isAnHiddenObjective = true;
                            }
                            break;
                        }
                    }
                    if(!isAnHiddenObjective) {
                        int[][] path = this.board[i][j].getPath();
                        for (int k = 0; i < 3; i++) {
                            for (int h = 0; i < 3; i++) {
                                this.intBoard[i * 3 + k][j * 3 + h] = path[k][h];
                            }
                        }
                    }
                }
            }
        }

        return this.intBoard; }
    @Override
    public Object clone() {
        return new SaboteurBoardState(this);
    }

    @Override
    public int getWinner() { return winner; }

    @Override
    public void setWinner(int win) { winner = win; }

    @Override
    public int getTurnPlayer() { return turnPlayer; }

    @Override
    public int getTurnNumber() { return turnNumber; }

    @Override
    public boolean isInitialized() { return board != null; }

    @Override
    public int firstPlayer() { return FIRST_PLAYER; }

    @Override
    public Move getRandomMove() {
        ArrayList<SaboteurMove> moves = getAllLegalMoves();
        return moves.get(rand.nextInt(moves.size()));
    }

    public SaboteurTile getPieceAt(int xPos, int yPos) {
        if (xPos < 0 || xPos >= BOARD_SIZE || yPos < 0 || yPos >= BOARD_SIZE) {
            throw new IllegalArgumentException("Out of range");
        }
        return board[xPos][yPos];
    }

    public boolean verifyLegit(int[][] path,int[] pos){
        // Given a tile's path, and a position to put this path, verify that it respects the rule of positionning;

        if(board[pos[0]][pos[1]] != null) return false;

        //the following integer are used to make sure that at least one path exists between the possible new tile to be added and existing tiles.
        // For example, a tile can't be placed near the objective, similarly a tile can't be connected only by a wall to another tile.
        int requiredEmptyAround=4;
        int numberOfEmptyAround=0;

        ArrayList<SaboteurTile> objHiddenList=new ArrayList<>();

        for(int i=0;i<3;i++) {
            if (!hiddenRevealed[i]) objHiddenList.add(this.hiddenCards[i]);
        }
        //verify left side:
        if(pos[1]>0) {
            SaboteurTile neighborCard = this.board[pos[0]][pos[1] - 1];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                if (path[0] != neighborPath[2]) return false;
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
                if (path[2] != neighborPath[0]) return false;
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
                if (p != np) return false;
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
                if (p != np) return false;
                else if(p[0] == 0 && p[1]== 0 && p[2] ==0 ) numberOfEmptyAround +=1; //we are touching by a wall
            }
        }
        else numberOfEmptyAround+=1;

        if(numberOfEmptyAround==requiredEmptyAround)  return false;

        return true;
    }
    public ArrayList<int[]> possiblePositions(SaboteurTile card) {
        // Given a card, returns all the possiblePositions at which the card could be positioned in an ArrayList of int[];
        // Note that the card will not be flipped in this test, a test for the flipped card should be made by giving to the function the flipped card.
        ArrayList<int[]> possiblePos = new ArrayList<int[]>();
        int[][] moves = {{0, -1},{1, 0},{1, 0},{-1, 0}};
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (this.board[i][j] == null) {
                    for (int m = 0; m < 4; m++) {
                        if (0 <= i+moves[m][0] && i+moves[m][0] < BOARD_SIZE && 0 <= j+moves[m][1] && j+moves[m][1] < BOARD_SIZE) {
                            if (this.verifyLegit(card.getPath(), new int[]{i + moves[m][0], j + moves[m][1]} )){
                                possiblePos.add(new int[]{i + moves[m][0], j + j+moves[m][1]});
                            }
                        }
                    }
                }
            }
        }
        return possiblePos;
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

    public boolean isLegal(SaboteurMove m) {
        // For a move to be legal, the player must have the card in its hand
        // and then the game rules apply.
        // Note that we do not test the flipped version. To test it: use the flipped card in the SaboteurMove object.

        SaboteurCard testCard = m.getCardPlayed();
        int[] pos = m.getPosPlayed();
        int currentPlayer = m.getPlayerID();
        if (currentPlayer != turnPlayer) return false;

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

        boolean legal = false;
        for(SaboteurCard card : hand){
            if (card instanceof SaboteurTile && testCard instanceof SaboteurTile && !isBlocked) {
                if(((SaboteurTile) card).getIdx().equals(((SaboteurTile) testCard).getIdx())) return verifyLegit(((SaboteurTile) card).getPath(),pos);
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

    private void draw(){
        if(this.Deck.size()>0){
            if(turnPlayer==1){
                this.player1Cards.add(this.Deck.remove(0));
            }
            else{
                this.player2Cards.add(this.Deck.remove(0));
            }
        }
    }

    public void processMove(SaboteurMove m) throws IllegalArgumentException {
        // Verify that a move is legal (if not throw an IllegalArgumentException
        // And then execute the move.
        // Concerning the map observation, the player then has to check by himself the result of its observation.
        if (!isLegal(m)) { throw new IllegalArgumentException("Invalid move. Move: " + m.toPrettyString()); }

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
        this.draw();
        this.updateWinner();

        turnPlayer = 1 - turnPlayer; // Swap player
    }


    private Boolean cardPath(ArrayList<int[]> originTargets,int[] targetPos,Boolean usingCard){
        // the search algorithm, usingCard indicate weither we search a path of cards (true) or a path of ones (aka tunnel)(false).
        ArrayList<int[]> queue = new ArrayList<>(); //will store the current neighboring tile. Composed of position (int[]).
        Map<int[],Boolean> visited = new HashMap<int[],Boolean>(); //will store the visited tile with an Hash table where the key is the position the board.
        visited.put(targetPos,true);
        if(usingCard) addUnvisitedNeighborToQueue(targetPos,queue,visited,BOARD_SIZE);
        else addUnvisitedNeighborToQueue(targetPos,queue,visited,BOARD_SIZE*3);
        while(queue.size()>0){
            int[] visitingPos = queue.remove(0);
            if(originTargets.contains(visitingPos)){
                return true;
            }
            visited.put(visitingPos,true);
            if(usingCard) addUnvisitedNeighborToQueue(visitingPos,queue,visited,BOARD_SIZE);
            else addUnvisitedNeighborToQueue(visitingPos,queue,visited,BOARD_SIZE*3);
        }
        return false;
    }
    private void addUnvisitedNeighborToQueue(int[] pos,ArrayList<int[]> queue, Map<int[],Boolean> visited,int maxSize){
        int[][] moves = {{0, -1},{1, 0},{1, 0},{-1, 0}};
        int i = pos[0];
        int j = pos[1];
        for (int m = 0; m < 4; m++) {
            if (0 <= i+moves[m][0] && i+moves[m][0] < maxSize && 0 <= j+moves[m][1] && j+moves[m][1] < maxSize) { //if the hypothetical neighbor is still inside the board
                int[] neighborPos = new int[]{i+moves[m][0],j+moves[m][1]};
                if(!visited.get(neighborPos)){
                    queue.add(neighborPos);
                }
            }
        }
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
               To speed up: The neighbor are added ranked on their distance to the origin...
        */
        this.getIntBoard(); //update the int board.
        boolean atLeastOnefound = false;
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
            if(this.hiddenRevealed[currentTargetIdx] = false) {  //verify that the current target has not been already discovered. Even if there is a destruction event, the target keeps being revealed!

                if (cardPath(originTargets, targetPos, true)) { //checks that there is a cardPath
                    //next: checks that there is a path of ones.
                    ArrayList<int[]> originTargets2 = new ArrayList<>();
                    //the starting points
                    originTargets.add(new int[]{originPos*3, originPos*3});
                    originTargets.add(new int[]{originPos*3-1, originPos*3});
                    originTargets.add(new int[]{originPos*3+1, originPos*3});
                    originTargets.add(new int[]{originPos*3, originPos*3-1});
                    originTargets.add(new int[]{originPos*3+1, originPos*3-1});
                    //get the target position
                    int[] targetPos2 = {0, 0};
                    for (int i = 0; i < 3; i++) {
                        if (this.hiddenCards[i].getIdx().equals(target.getIdx())) {
                            targetPos2[0] = SaboteurBoardState.hiddenPos[i][0]*3 + 1;
                            targetPos2[1] = SaboteurBoardState.hiddenPos[i][1]*3 + 1;
                            break;
                        }
                    }
                    if (cardPath(originTargets2, targetPos2, false)) {
                        this.hiddenRevealed[currentTargetIdx] = true;
                        this.player1hiddenRevealed[currentTargetIdx] = true;
                        this.player2hiddenRevealed[currentTargetIdx] = true;
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

    private void updateWinner() {

        pathToHidden(new SaboteurTile[]{new SaboteurTile("nugget"),new SaboteurTile("hidden1"),new SaboteurTile("hidden2")});
        int nuggetIdx = -1;
        for(int i =0;i<3;i++){
            if(this.hiddenCards[i].getIdx().equals("nugget")){
                nuggetIdx = i;
                break;
            }
        }
        boolean playerWin = this.hiddenRevealed[nuggetIdx];
        if (playerWin) { // Current player has won
            winner = turnPlayer;
        } else if (gameOver() && winner!=1-turnPlayer) {
            winner = Board.DRAW;
        }
    }

    @Override
    public boolean gameOver() {
        return this.Deck.size()==0 && this.player1Cards.size()==0 && this.player2Cards.size()==0 || winner != Board.NOBODY;
    }

    public void printBoard() {
        System.out.println(this.toString());
    }

    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();
        for (int i = 0; i < BOARD_SIZE*3; i++) {
            for (int j = 0; j < BOARD_SIZE*3; j++) {
                boardString.append(intBoard[i][j]);
                boardString.append(",");
            }
            boardString.append("\n");
        }
        return boardString.toString();
    }

    public static void main(String[] args) {
        SaboteurBoardState pbs = new SaboteurBoardState();

        Scanner scanner = new Scanner(System.in);
        int id = FIRST_PLAYER;
        while(pbs.winner == Board.NOBODY) {
            System.out.print("Enter move (cardIndex x y): ");
            String moveStr = scanner.nextLine();
            SaboteurMove m = new SaboteurMove(moveStr + " " + id);
            if (!pbs.isLegal(m)) {
                System.out.println("Invalid move: " + m.toPrettyString());
                continue;
            }
            pbs.processMove(m);
            pbs.printBoard();
            id = 1 - id;
        }

        switch(pbs.winner) {
            case 1:
                System.out.println("First player wins.");
                break;
            case 0:
                System.out.println("Second player wins.");
                break;
            case Board.DRAW:
                System.out.println("Draw.");
                break;
            case Board.NOBODY:
                System.out.println("Nobody has won.");
                break;
            default:
                System.out.println("Unknown error.");
        }
    }
}
