package pentago_swap;

import boardgame.Board;
import boardgame.BoardState;
import boardgame.Move;

import java.util.ArrayList;
import java.util.function.UnaryOperator;
import java.util.Random;

/**
 *
 * Note: First player white, second player black!!
 * @author mgrenander
 */
public class PentagoBoardState extends BoardState {
    public static final int BOARD_SIZE = 9;
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    public static final int MAX_TURNS = 36;
    public static final int ILLEGAL = -1;
    public static enum Piece {
        BLACK, WHITE, EMPTY
    }

    private static final UnaryOperator<PentagoCoord> getNextHorizontal = c -> new PentagoCoord(c.getX()+1, c.getY());
    private static final UnaryOperator<PentagoCoord> getNextVertical = c -> new PentagoCoord(c.getX(), c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextDiagRight = c -> new PentagoCoord(c.getX()+1, c.getY()+1);
    private static final UnaryOperator<PentagoCoord> getNextDiagLeft = c -> new PentagoCoord(c.getX()+1, c.getY()-1);
    private static int FIRST_PLAYER = 0;

    private Piece[][] board;
    private int turnPlayer;
    private int turnNumber;
    private int winner;
    private Random rand;

    public PentagoBoardState() {
        super();
        this.board = new Piece[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                this.board[i][j] = Piece.EMPTY;
            }
        }
        rand = new Random(2019);
        winner = Board.NOBODY;
        turnPlayer = FIRST_PLAYER;
        turnNumber = 0;
    }

    // For cloning
    private PentagoBoardState(PentagoBoardState pbs) {
        super();
        this.board = new Piece[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                this.board[i][j] = pbs.board[i][j];
            }
        }
        rand = new Random(2019);
        this.winner = pbs.winner;
        this.turnPlayer = pbs.turnPlayer;
        this.turnNumber = pbs.turnNumber;
    }

    @Override
    public Object clone() {
        return new PentagoBoardState(this);
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
        ArrayList<PentagoMove> moves = getAllLegalMoves();
        return moves.get(rand.nextInt(moves.size()));
    }

    // TODO: complete this
    public ArrayList<PentagoMove> getAllLegalMoves() {
        return null;
    }

    private void updateWinner() {
        boolean playerWin = checkVerticalWin(turnPlayer) || checkHorizontalWin(turnPlayer) || checkDiagRightWin(turnPlayer) || checkDiagLeftWin(turnPlayer);
        if (playerWin) {
            int otherPlayer = 1 - turnPlayer;
            boolean otherWin = checkVerticalWin(otherPlayer) || checkHorizontalWin(otherPlayer) || checkDiagRightWin(otherPlayer) || checkDiagLeftWin(otherPlayer);
            winner = otherWin ? Board.DRAW : turnPlayer;
        }

        if (gameOver()) { winner = Board.DRAW; }
    }

    private boolean checkVerticalWin(int player) {
        return checkWinRange(player, 0, 2, 0, BOARD_SIZE, getNextVertical);
    }

    private boolean checkHorizontalWin(int player) {
        return checkWinRange(player, 0, BOARD_SIZE, 0, 2, getNextHorizontal);
    }

    private boolean checkDiagRightWin(int player) {
        return checkWinRange(player, 0, 2, 0, 2, getNextDiagRight);
    }

    private boolean checkDiagLeftWin(int player) {
        return checkWinRange(player, 0 ,2, BOARD_SIZE - 2, BOARD_SIZE, getNextDiagLeft);
    }

    private boolean checkWinRange(int player, int xStart, int xEnd, int yStart, int yEnd, UnaryOperator<PentagoCoord> direction) {
        boolean win = false;
        for (int i = xStart; i < xEnd; i++) {
            for (int j = yStart; j < yEnd; j++) {
                win |= checkWin(player, new PentagoCoord(i, j), direction);
                if (win) { return true; }
            }
        }
        return false;
    }

    private boolean checkWin(int player, PentagoCoord start, UnaryOperator<PentagoCoord> direction) {
        int winCounter = 0;
        Piece currColour = player == 0 ? Piece.WHITE : Piece.BLACK;
        PentagoCoord current = start;
        while(true) {
            try {
                if (currColour == this.board[current.getX()][current.getY()]) {
                    winCounter++;
                    current = direction.apply(current);
                } else {
                    break;
                }
            } catch (IllegalArgumentException e) { //We have run off the board
                break;
            }
        }
        return winCounter >= 5;
    }

    @Override
    public boolean gameOver() {
        return turnNumber >= MAX_TURNS || winner != Board.NOBODY;
    }
}
