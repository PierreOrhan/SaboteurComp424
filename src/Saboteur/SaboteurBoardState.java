package Saboteur;

import Saboteur.cardClasses.*;
import boardgame.Board;
import boardgame.BoardState;
import boardgame.Move;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.function.UnaryOperator;
import java.util.Collections;

/**
 *
 * Note: First player white, second player black!!
 * @author Pierre, adapted from mgrenander work on pentagoswap.
 */
public class SaboteurBoardState extends BoardState {
    public static final int BOARD_SIZE = 29;
    public static final int originPos = 14;

    public static final int EMPTY = -1;
    public static final int TUNNEL = 1;
    public static final int WALL = 0;

    private static int FIRST_PLAYER = 0;

    private SaboteurTile[][] board;
    private int[][] intBoard;
    //player variables:
    private ArrayList<SaboteurCard> player1Cards; //hand of player 1
    private ArrayList<SaboteurCard> player2Cards; //hand of player 2
    private int player1nbMalus;
    private int player2nbMalus;

    private ArrayList<SaboteurCard> Deck; //deck form which player pick
    public static final int[][] hiddenPos = {{originPos+7,originPos-2},{originPos+7,originPos},{originPos+7,originPos+2}};
    private SaboteurTile[] hiddenCards = new SaboteurTile[3];
    private boolean[] hiddenRevealed = {false,false,false};


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
    int[][] getIntBoard() {
        //update the int board.
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


    public void processMove(SaboteurMove m) throws IllegalArgumentException {
        if (!isLegal(m)) { throw new IllegalArgumentException("Invalid move. Move: " + m.toPrettyString()); }

        //TODO

        if (turnPlayer != FIRST_PLAYER) { turnNumber += 1; } // Update the turn number if needed
        turnPlayer = 1 - turnPlayer; // Swap player
    }
    private boolean pathToHidden(String[] objectives){
        /* This function look if a path is linking the starting point to one of the states among objectives.
            :return: if there exists one: true
            if not: false
            In Addition it changes self.hidden[foundState] to true!

            Implementation details:
            To do so we start by computing the 0-1 path matrix,
                then we use a A* search by indicating that going toward on of the three different goal is the best option among potential candidates
        */
        this.getIntBoard(); //update the int board.

        // TODO
        return false;
    }

    private void updateWinner() {
        boolean playerWin = pathToHidden(new String[]{"nugget"});

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
            case WHITE:
                System.out.println("White wins.");
                break;
            case BLACK:
                System.out.println("Black wins.");
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
