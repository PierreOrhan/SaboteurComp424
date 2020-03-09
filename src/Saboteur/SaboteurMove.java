package Saboteur;

import Saboteur.cardClasses.*;
import boardgame.Board;
import boardgame.Move;

/**
 * This class is used for communication with the server.
 * A move is summarized by {the card used, the position at which it is used (falls back to (0,0) for the effects card, to (x,0) for the Drop where x is the index of the card in your hand you want to drop),
 *                           the player that is using the card.}
 * Because the game is 1V1 a malus effect goes on the other player while bonus goes on itself.
 * @author Pierre Orhan, modified from mgrenander
 */
public class SaboteurMove extends Move {
    private int playerId;
    private String cardName;
    private int xMove;
    private int yMove;
    private boolean fromBoard;
    private String boardInit;

    public SaboteurMove(SaboteurCard card,int x, int y, int playerId) {
        this.cardName = card.getName();
        this.playerId = playerId;
        this.xMove = x;
        this.yMove = y;
        this.fromBoard = false;
    }

    public SaboteurMove(String formatString) {
        if(formatString.split(":")[0].equals("BoardInit")){ //Initialization move from the board, used by the server, not important for the player;
            this.boardInit = formatString;
            this.fromBoard = true;
            this.playerId = Board.BOARD;
        }
        else {
            String[] components = formatString.split(" ");
            try {
                this.cardName = components[0];
                this.xMove = Integer.parseInt(components[1]);
                this.yMove = Integer.parseInt(components[2]);
                this.playerId = Integer.parseInt(components[3]);
                this.fromBoard = false;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Received an uninterpretable string format for a TablutMove.");
            }
        }
    }

    // Getters
    public SaboteurCard getCardPlayed() {
        switch (this.cardName.split(":")[0]){
            case "Tile": return new SaboteurTile(this.cardName.split(":")[1]);
            case "Map": return new SaboteurMap();
            case "Malus": return new SaboteurMalus();
            case "Bonus": return new SaboteurBonus();
            case "Destroy": return new SaboteurDestroy();
        }
        // Otherwise the move is a drop
        return new SaboteurDrop();
    }

    public int[] getPosPlayed(){
        return new int[]{this.xMove,this.yMove};
    }

    // Server methods
    @Override
    public int getPlayerID() { return this.playerId; }

    @Override
    public void setPlayerID(int playerId) { this.playerId = playerId; }

    @Override
    public void setFromBoard(boolean fromBoard) { this.fromBoard = fromBoard; }
    public boolean getFromBoard() { return this.fromBoard;}

    public String getBoardInit() {return  this.boardInit;}

    @Override
    public boolean doLog() { return true; }

    @Override
    public String toPrettyString() {
        return String.format("Player %d, CardUsed: (%s), PosOfUse: (%d, %d)", playerId, cardName, xMove, yMove);
    }

    @Override
    public String toTransportable() {
        if(this.fromBoard){
            return String.format("%s",this.boardInit);
        }
        return String.format("%s %d %d %d",cardName, xMove, yMove,playerId);
    }
}
