package student_player;

import boardgame.Move;

import Saboteur.SaboteurPlayer;
import Saboteur.cardClasses.SaboteurMap;

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
        super("260714814");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(SaboteurBoardState boardState) {
    	SaboteurMove myMove = boardState.getRandomMove();
    	double max = 0;
    	for(SaboteurMove move : boardState.getAllLegalMoves()) {
    		//Prioritize map
    		if(move.getCardPlayed() instanceof SaboteurMap) {
    			myMove = move;
    			break;
    		}
    		BoardCopy board;
    		double utility = 0;
    		//Number of random runs per legal move
    		int numRuns = 10;
    		for(int i = 0; i < numRuns; i++) {
    			board = new BoardCopy(boardState.getHiddenBoard(), boardState.getHiddenIntBoard(), boardState.getCurrentPlayerCards(), player_id);
    			//Process your move, then start the random run
    			board.processMove(move);
    			utility += board.run();
    		}
    		if(utility > max) {
    			myMove = move;
    			max = utility;
    		}
    		//System.out.println(max);
    	}
    	MyTools.discard.add(myMove.getCardPlayed());
        return myMove;
    }
}