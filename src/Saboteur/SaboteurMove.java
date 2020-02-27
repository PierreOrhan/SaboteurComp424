package Saboteur;

import Saboteur.cardClasses.*;
import boardgame.Move;

/**
 * @author Pierre Orhan, modified from mgrenander
 */
public class SaboteurMove extends Move {
    private int playerId;
    private String cardName;
    private int xMove;
    private int yMove;
    private boolean fromBoard;

    public SaboteurMove(SaboteurCard card,int x, int y, int playerId) {
        this.cardName = card.getName();
        this.playerId = playerId;
        this.xMove = x;
        this.yMove = y;
        this.fromBoard = false;
    }

    public SaboteurMove(String formatString) {
        String[] components = formatString.split(" ");
        try {
            this.cardName = components[0];
            this.xMove = Integer.parseInt(components[1]);
            this.yMove = Integer.parseInt(components[2]);
            this.playerId = Integer.parseInt(components[4]);
            this.fromBoard = false;
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("Received an uninterpretable string format for a TablutMove.");
        }
    }

    // Getters
    public SaboteurCard getCardPlayed() {
        switch (this.cardName.split(":")[0]){
            case "tile": return new SaboteurTile(this.cardName.split(":")[1]);
            case "Map": return new SaboteurMap();
            case "Malus": return new SaboteurMalus();
            case "Bonus": return new SaboteurBonus();
            case "Destroy": return new SaboteurDestroy();
        }
        // Otherwise the move is a drop
        return new SaboteurDrop();
    }

    // Fetch player's name
    public String getPlayerName(int player) {
        if (playerId != SaboteurBoardState.BLACK && playerId != SaboteurBoardState.WHITE) {
            return "Illegal";
        }
        return player == SaboteurBoardState.WHITE ? "White" : "Black";
    }

    // Fetch the current player name
    public String getPlayerName() {
        return getPlayerName(this.playerId);
    }

    // Server methods
    @Override
    public int getPlayerID() { return this.playerId; }

    @Override
    public void setPlayerID(int playerId) { this.playerId = playerId; }

    @Override
    public void setFromBoard(boolean fromBoard) { this.fromBoard = fromBoard; }

    @Override
    public boolean doLog() { return true; }

    @Override
    public String toPrettyString() {
        return String.format("Player %d, CardUsed: (%s), PosOfUse: (%d, %d)", playerId, cardName, xMove, yMove);
    }

    @Override
    public String toTransportable() {
        return String.format("%s %d %d %d",cardName, xMove, yMove,playerId);
    }
}
