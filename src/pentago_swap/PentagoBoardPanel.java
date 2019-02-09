package pentago_swap;

import pentago_swap.PentagoBoardState.Piece;

import java.awt.event.ComponentEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import boardgame.BoardPanel;
import java.util.ArrayList;

/**
 * @author mgrenander
 */
public class PentagoBoardPanel extends BoardPanel implements MouseListener, MouseMotionListener, ComponentListener {
    static final Color BACKGROUND_COLOR = Color.GRAY;
    static final Color LINE_COLOR = Color.BLACK;
    static final Color BOARD_COLOR2 = new Color(245, 222, 179); // a subtle "wheat" color for the board...
    static final Color BOARD_COLOR1 = new Color(244, 164, 96); // complemented with a tasteful "sandybrown".
    static final Color WHITE_COL = Color.WHITE;
    static final Color BLACK_COL = Color.BLACK;

    static final int BOARD_DIM = PentagoBoardState.BOARD_SIZE;
    static final int PIECE_SIZE = 75;
    static final int FONT_SIZE = (int) (PIECE_SIZE * 0.5);
    static final int SQUARE_SIZE = (int) (PIECE_SIZE * 1.25); // Squares 25% bigger than pieces.

    final class GUIPiece {
        private Piece pieceType;
        public int xPos;
        public int yPos;
        public PentagoCoord coord;

        // Construct a piece!
        public GUIPiece(Piece pieceType, int xPos, int yPos, PentagoCoord coord) {
            this.pieceType = pieceType;
            this.xPos = xPos;
            this.yPos = yPos;
            this.coord = coord;
        }

        public void draw(Graphics g) {
            draw(g, xPos, yPos);
        }

        public void draw(Graphics g, int cx, int cy) {
            int x = cx - PIECE_SIZE / 2;
            int y = cy - PIECE_SIZE / 2;

            g.setColor(pieceType == Piece.BLACK ? BLACK_COL : WHITE_COL);

            // Paint piece.
            g.fillOval(x, y, PIECE_SIZE, PIECE_SIZE);
            if (pieceType != Piece.BLACK) {// draw a border around whites
                g.setColor(Color.BLACK);
                g.drawOval(x, y, PIECE_SIZE, PIECE_SIZE);
            }
        }
    }

    // Stores all board pieces.
    private ArrayList<GUIPiece> boardPieces;
    BoardPanelListener listener;
    private boolean isPieceSelected;
    private PentagoCoord pieceSelection;
    private boolean isQuadSelected;
    private Integer quadSelection;

    // Constructing with this as the listener for everything.
    public PentagoBoardPanel() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);

        isPieceSelected = false;
        isQuadSelected = false;
    }

    // Overriding BoardPanel methods to help with listener functionality.
    @Override
    protected void requestMove(BoardPanelListener l) {
        listener = l;
        System.out.println("REQUESTED.");
    }

    @Override
    protected void cancelMoveRequest() {
        listener = null;
    }

    // Drawing a board.
    @Override
    public void drawBoard(Graphics g) {
        super.drawBoard(g); // Paints background and other
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //Makes pretty

        //Paint board
        for (int i = 0; i < PentagoBoardState.BOARD_SIZE; i++) {
            for (int j = 0; j < PentagoBoardState.BOARD_SIZE; j++) {
                Color currColor = (i + j) % 2 == 0 ? BOARD_COLOR1 : BOARD_COLOR2;
                g2.setColor(currColor);
                g2.fillRect(i*SQUARE_SIZE, j*SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }
        }
        g2.setStroke(new BasicStroke(3));
        g2.setColor(LINE_COLOR);

        int endPos = BOARD_DIM * SQUARE_SIZE;
        g2.drawLine(0, 0, endPos, 0);
        g2.drawLine(0,0, 0, endPos);
        g2.drawLine(0, endPos, endPos, endPos);
        g2.drawLine(endPos, 0, endPos, endPos);

        int midPos = endPos / 2;
        g2.drawLine(0, midPos, endPos, midPos);
        g2.drawLine(midPos, 0, midPos, endPos);
        g2.setStroke(new BasicStroke(1));

        boardPieces = new ArrayList<>();
        updateBoardPieces();
        for (GUIPiece gp : boardPieces) {
            gp.draw(g2);
        }

    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
    }

    // A bit sketchy, but has to be done to highlight possible moves for human.
    private void humanRepaint() {
        bufferDirty = true;
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (listener == null) { return; }
        int clickX = e.getX();
        int clickY = e.getY();

        if (!isPieceSelected) { // Player wants to click on a piece
            processPlacePiece(e);
        } else if (!isQuadSelected) { // Player wants to click on a quadrant
            processQuadClick(e);
        } else { // The second quandrant was pressed
            completeMove(e);
        }
    }

    private void resetSelection() {
        isPieceSelected = false;
        isQuadSelected = false;
        quadSelection = null;
        pieceSelection = null;
    }

    private void processPlacePiece(MouseEvent e) {
        int clickX = e.getX();
        int clickY = e.getY();

        // Check if we clicked on an occupied square. If so this is not a real move
        for (GUIPiece gp : boardPieces) {
            if (clickInSquare(clickX, clickY, gp.xPos, gp.yPos)) {
                return;
            }
        }
        PentagoBoardState pbs = (PentagoBoardState) getCurrentBoard().getBoardState();
        PentagoCoord dest = null;
        outer:for (int i = 0; i < PentagoBoardState.BOARD_SIZE; i++) {
            for (int j = 0; j < PentagoBoardState.BOARD_SIZE; j++) {
                if (pbs.getPieceAt(i, j) == Piece.EMPTY) {
                    int xPos = j * SQUARE_SIZE + SQUARE_SIZE / 2;
                    int yPos = i * SQUARE_SIZE + SQUARE_SIZE / 2;
                    if(clickInSquare(clickX, clickY, xPos, yPos)) {
                        dest = new PentagoCoord(i, j);
                        break outer;
                    }
                }
            }
        }
        if (dest == null) { return; }
        if (pbs.isPlaceLegal(dest)) {
            isPieceSelected = true;
            pieceSelection = new PentagoCoord(dest.getX(), dest.getY());
            pbs.getBoard()[dest.getX()][dest.getY()] = pbs.getTurnPlayer() == PentagoBoardState.WHITE ? Piece.WHITE : Piece.BLACK;
            humanRepaint();
        }
    }

    private void processQuadClick(MouseEvent e) {
        // TODO: highlighting the quad selection would be great
        quadSelection = findQuadSelection(e);
        if (quadSelection == null) { return; }
        isQuadSelected = true;
    }

    private void completeMove(MouseEvent e) {
        Integer secondQuad = findQuadSelection(e);
        if (secondQuad == null || secondQuad.equals(quadSelection)) { return; }
        PentagoBoardState pbs = (PentagoBoardState) getCurrentBoard().getBoardState();
        PentagoMove move = new PentagoMove(pieceSelection, quadSelection, secondQuad, pbs.getTurnPlayer());
        listener.moveEntered(move);
        cancelMoveRequest();
        resetSelection(); // Reset the selection variables
    }

    private Integer findQuadSelection(MouseEvent e) {
        int clickX = e.getX();
        int clickY = e.getY();
        for (int i = 0; i < PentagoBoardState.BOARD_SIZE; i++) {
            for (int j = 0; j < PentagoBoardState.BOARD_SIZE; j++) {
                int xPos = j * SQUARE_SIZE + SQUARE_SIZE / 2;
                int yPos = i * SQUARE_SIZE + SQUARE_SIZE / 2;
                if(clickInSquare(clickX, clickY, xPos, yPos)) {
                    if (i < 3 && j < 3) { return 0; }
                    else if (i < 3 && j >= 3) { return 1; }
                    else if (j < 3) { return 2; }
                    else { return 3; }
                }
            }
        }
        return null; // Was not a valid quad selection
    }

    private void updateBoardPieces() {
        PentagoBoardState pbs = (PentagoBoardState) getCurrentBoard().getBoardState();
        boardPieces = new ArrayList<>();
        for (int i = 0; i < PentagoBoardState.BOARD_SIZE; i++) {
            for (int j = 0; j < PentagoBoardState.BOARD_SIZE; j++) {
                Piece p = pbs.getPieceAt(i, j);
                if (p != Piece.EMPTY) {
                    int xPos = j * SQUARE_SIZE + SQUARE_SIZE / 2;
                    int yPos = i * SQUARE_SIZE + SQUARE_SIZE / 2;
                    GUIPiece gp = new GUIPiece(p, xPos, yPos, new PentagoCoord(i, j));
                    boardPieces.add(gp);
                }
            }
        }
    }

    // Helpers.
    @Override
    public Color getBackground() {
        return BACKGROUND_COLOR;
    }

    private static boolean clickInCircle(int x, int y, int cx, int cy) {
        return Math.pow(cx - x, 2) + Math.pow(cy - y, 2) < Math.pow(PIECE_SIZE / 2, 2);
    }

    private static boolean clickInSquare(int x, int y, int cx, int cy) {
        return Math.abs(x - cx) < SQUARE_SIZE / 2 && Math.abs(y - cy) < SQUARE_SIZE / 2;
    }

    /* Don't use these interface methods */
    public void mouseDragged(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void componentResized(ComponentEvent arg0) {
    }

    public void mouseClicked(MouseEvent arg0) {
    }

    public void mouseEntered(MouseEvent arg0) {
    }

    public void mouseExited(MouseEvent arg0) {
    }

    public void mouseMoved(MouseEvent arg0) {
    }

    public void componentMoved(ComponentEvent arg0) {
    }

    public void componentShown(ComponentEvent arg0) {
    }

    public void componentHidden(ComponentEvent arg0) {
    }
}
