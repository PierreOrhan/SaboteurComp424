package Saboteur;

import Saboteur.cardClasses.SaboteurCard;
import boardgame.Board;
import boardgame.BoardPanel;
import Saboteur.SaboteurBoard;
import Saboteur.SaboteurBoardState;
import Saboteur.cardClasses.SaboteurTile;
import Saboteur.SaboteurMove;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.image.AffineTransformOp;

/**
 * @author Pierre Orhan, adapted from mgrenander
 */
public class SaboteurBoardPanel extends BoardPanel implements MouseListener, MouseMotionListener, ComponentListener {

    final class TileImage {
        public final int Height;
        public final int Width;
        private BufferedImage img;
        private SaboteurCard tile;
        int xPos;
        int yPos;
        // Construct a tile!
        TileImage(SaboteurCard tile, int x, int y) {
            this.tile = tile;
            String name = tile.getName().contains("Tile") ? tile.getName().split(":")[1] : tile.getName(); //in case we want to load other types of cards images...
            try {
                this.img= ImageIO.read(new File("D:\\0-cours\\projet\\Comp424\\lastYearproject\\pentago-swap\\src\\Saboteur\\tiles\\" + name + ".png"));
            }catch (IOException ie){
                System.out.println("problem loading images, at");
                System.out.println("D:\\0-cours\\projet\\Comp424\\lastYearproject\\pentago-swap\\src\\Saboteur\\tiles\\" + name + ".png");
            }
            this.Height = img.getHeight();
            this.Width = img.getWidth();
            this.yPos = x * Height+10;
            this.xPos = y * Width;

        }
        void draw(Graphics g) {
            draw(g, xPos, yPos);
        }
        void draw(Graphics g, int cx, int cy) {
//            System.out.println("img cara:("+Height+","+Width+")"+tile.getName());
//            System.out.println("drawn at:("+cx+","+cy+")");
            g.drawImage(this.img,cx,cy,null);
        }
    }
    // Stores all board pieces.
    private ArrayList<TileImage> allTileImgs;
    private ArrayList<TileImage> p1cardsImgs;
    private ArrayList<TileImage> p2cardsImgs;
    private BoardPanelListener listener;
    private boolean isPieceSelected;
    private boolean isQuadSelected;
    private Integer quadSelection;
    public BufferedImage background;

    // Constructing with this as the listener for everything.
    SaboteurBoardPanel() {
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(this);
        try{
            this.background = ImageIO.read(new File("D:\\0-cours\\projet\\Comp424\\lastYearproject\\pentago-swap\\src\\Saboteur\\tiles\\background.png"));
        }catch (IOException ie){
            System.out.println("problem loading background image");
        }
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
        //super.drawBoard(g); // Paints background and other
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //Makes pretty
        g2.scale(0.5, 0.5);
        g2.drawImage(this.background,0,0,null);
        allTileImgs = new ArrayList<>();
        updateBoardPieces(); //update allTileImgs
        for (TileImage ti : allTileImgs) {
            ti.draw(g2);
        }
        for (TileImage ti : p1cardsImgs) {
            ti.draw(g2);
        }
        for (TileImage ti : p2cardsImgs) {
            ti.draw(g2);
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

    private void updateBoardPieces() {
        SaboteurBoardState mysb = (SaboteurBoardState) this.getCurrentBoard().getBoardState();
        allTileImgs = new ArrayList<>();
        SaboteurTile[][] board = mysb.getBoardForDisplay();
        for (int i = 0; i < SaboteurBoardState.BOARD_SIZE; i++) {
            for (int j = 0; j < SaboteurBoardState.BOARD_SIZE; j++) {
                SaboteurTile st = board[i][j];
                if (st != null) {
                    TileImage ti = new TileImage(st, i, j);
                    allTileImgs.add(ti);
                }
            }
        }
       ArrayList<SaboteurCard> players1Cards = mysb.getPlayerCardsForDisplay(1);
       this.p1cardsImgs = new ArrayList<>();
       int y = 1;
       int x = SaboteurBoardState.BOARD_SIZE+1;
       for(SaboteurCard pc : players1Cards){
           this.p1cardsImgs.add(new TileImage(pc,x,y));
           y+=1;
       }
       ArrayList<SaboteurCard> players2Cards = mysb.getPlayerCardsForDisplay(0);
        this.p2cardsImgs = new ArrayList<>();
        y = 15;
        for(SaboteurCard pc : players2Cards){
            this.p2cardsImgs.add(new TileImage(pc,x,y));
            y+=1;
        }
    }

    //todo : implement all the following method to manage the environment.
    @Override
    public void mousePressed(MouseEvent e) {
        if (listener == null) { return; }

        if (!isPieceSelected) { // Player wants to click on a piece
            //processPlacePiece(e);
        } else if (!isQuadSelected) { // Player wants to click on a quadrant
           // processQuadClick(e);
        } else { // The second quandrant was pressed
            //completeMove(e);
        }
    }
//
//    private void resetSelection() {
//        isPieceSelected = false;
//        isQuadSelected = false;
//        quadSelection = null;
//    }
//
//    private void processPlacePiece(MouseEvent e) {
//        int clickX = e.getX();
//        int clickY = e.getY();
//
//        // Check if we clicked on an occupied square. If so this is not a real move
//        for (GUIPiece gp : boardPieces) {
//            if (clickInSquare(clickX, clickY, gp.xPos, gp.yPos)) {
//                return;
//            }
//        }
//        PentagoBoardState pbs = (PentagoBoardState) getCurrentBoard().getBoardState();
//        PentagoCoord dest = null;
//        outer:for (int i = 0; i < PentagoBoardState.BOARD_SIZE; i++) {
//            for (int j = 0; j < PentagoBoardState.BOARD_SIZE; j++) {
//                if (pbs.getPieceAt(i, j) == Piece.EMPTY) {
//                    int xPos = j * SQUARE_SIZE + SQUARE_SIZE / 2;
//                    int yPos = i * SQUARE_SIZE + SQUARE_SIZE / 2;
//                    if(clickInSquare(clickX, clickY, xPos, yPos)) {
//                        dest = new PentagoCoord(i, j);
//                        break outer;
//                    }
//                }
//            }
//        }
//        if (dest == null) { return; }
//        if (pbs.isPlaceLegal(dest)) {
//            isPieceSelected = true;
//            pieceSelection = new PentagoCoord(dest.getX(), dest.getY());
//            pbs.getBoard()[dest.getX()][dest.getY()] = pbs.getTurnPlayer() == PentagoBoardState.WHITE ? Piece.WHITE : Piece.BLACK;
//            humanRepaint();
//            System.out.println("PIECE PLACED");
//        }
//    }
//
//    private void processQuadClick(MouseEvent e) {
//        quadSelection = findQuadSelection(e);
//        if (quadSelection == null) { return; }
//        isQuadSelected = true;
//        humanRepaint();
//        System.out.println("QUAD SELECTED");
//    }
//
//    private void completeMove(MouseEvent e) {
//        Integer secondQuad = findQuadSelection(e);
//        if (secondQuad == null || secondQuad.equals(quadSelection)) { return; }
//        PentagoBoardState pbs = (PentagoBoardState) getCurrentBoard().getBoardState();
//        PentagoMove move = new PentagoMove(pieceSelection, quadSelection, secondQuad, pbs.getTurnPlayer());
//        listener.moveEntered(move);
//        cancelMoveRequest();
//        resetSelection(); // Reset the selection variables
//        System.out.println("MOVE COMPLETED");
//    }
//
//    private Integer findQuadSelection(MouseEvent e) {
//        int clickX = e.getX();
//        int clickY = e.getY();
//        for (int i = 0; i < PentagoBoardState.BOARD_SIZE; i++) {
//            for (int j = 0; j < PentagoBoardState.BOARD_SIZE; j++) {
//                int xPos = j * SQUARE_SIZE + SQUARE_SIZE / 2;
//                int yPos = i * SQUARE_SIZE + SQUARE_SIZE / 2;
//                if(clickInSquare(clickX, clickY, xPos, yPos)) {
//                    if (i < 3 && j < 3) { return 0; }
//                    else if (i < 3 && j >= 3) { return 1; }
//                    else if (j < 3) { return 2; }
//                    else { return 3; }
//                }
//            }
//        }
//        return null; // Was not a valid quad selection
//    }
//
//
//    private static boolean clickInSquare(int x, int y, int cx, int cy) {
//        return Math.abs(x - cx) < SQUARE_SIZE / 2 && Math.abs(y - cy) < SQUARE_SIZE / 2;
//    }

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
