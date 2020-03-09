package Saboteur;

import boardgame.Board;
import boardgame.BoardPanel;
import boardgame.BoardState;
import boardgame.Move;

/**
 * @author Pierre Orhan, extends  mgrenander work
 */
public class SaboteurBoard extends Board {
    private SaboteurBoardState boardState;

    public SaboteurBoard() {
        super();
        boardState = new SaboteurBoardState();
    }

    @Override
    public int getWinner() { return boardState.getWinner(); }

    @Override
    public void forceWinner(int win) { boardState.setWinner(win); }

    @Override
    public int getTurnPlayer() { return boardState.getTurnPlayer(); }

    @Override
    public int getTurnNumber() { return boardState.getTurnNumber(); }

    @Override
    public void move(Move m) throws IllegalArgumentException {
        boardState.processMove((SaboteurMove) m);
    }
    @Override
    public Move getBoardMove(){
        return boardState.getBoardMove();
    }

    @Override
    public BoardState getBoardState() { return boardState; }

    @Override
    public BoardPanel createBoardPanel() {
        SaboteurBoardPanel sbp = new SaboteurBoardPanel();
        return sbp; }

    @Override
    public String getNameForID(int p) { return String.format("Player-%d", p); }

    @Override
    public int getIDForName(String s) { return Integer.valueOf(s.split("-")[1]); }

    @Override
    public int getNumberOfPlayers() { return 2; }

    @Override
    public Move parseMove(String str) throws IllegalArgumentException {
        return new SaboteurMove(str);
    }

    @Override
    public Object clone() { //NOTE (PIERRE 2020) the clone is not necessary for the server connection and never used their...
        SaboteurBoard board = new SaboteurBoard();
        board.boardState = (SaboteurBoardState) boardState.clone();
        return board;
    }

    @Override
    public Move getRandomMove() { return boardState.getRandomMove(); }
}
