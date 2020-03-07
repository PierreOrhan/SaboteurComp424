package Saboteur;

import Saboteur.cardClasses.*;
import boardgame.Board;
import boardgame.BoardPanel;
import Saboteur.SaboteurBoard;
import Saboteur.SaboteurBoardState;
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

    //user interactions:
    private BoardPanelListener listener;
    //For a user: either he selects:
    // ==A tile==
    //if a tile is selected by the user in its hand --> then we wait for a mouse pressed at the position he wants to put the tile.
    private boolean isTileSelected;
    private SaboteurTile selectedTile;
    //== A Destroy ==
    private boolean isDestroySelected;
    //==A map ==
    private boolean isMapSelected;
    //== Bonus or malus are automatically selected;


    //background image
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
        isTileSelected = false;
        isDestroySelected= false;
        isMapSelected = false;
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

        if (isTileSelected) { // Player wants to click on a piece
            processTileChoice(e);
        } else if (isDestroySelected) { // Player wants to click on a quadrant
           processDestroyChoice(e);
        } else if(isMapSelected) { // The second quandrant was pressed
           processMapChoice(e);
        } else{
            System.out.println("processing card choice");
            processCardChoice(e);
        }
    }

    private void resetSelection() {
        isTileSelected = false;
        isDestroySelected= false;
        isMapSelected = false;
    }

    private void processCardChoice(MouseEvent e) {
        int clickX = e.getX();
        int clickY = e.getY();

        int turnPlayer = this.getCurrentBoard().getTurnPlayer();
        updateBoardPieces();
        ArrayList<TileImage> hand = turnPlayer == 1 ? this.p1cardsImgs : this.p2cardsImgs;
        // Check if we clicked on a card in the hand
        for (TileImage gp : hand) {
            if (clickInSquare(clickX, clickY, gp.xPos, gp.yPos, gp.Height, gp.Width)) {
                if (gp.tile.getName().contains("Tile")) {
                    this.isTileSelected = true;
                    this.selectedTile = (SaboteurTile) gp.tile;
                } else if (gp.tile.getName().contains("Map")) this.isMapSelected = true;
                else if (gp.tile.getName().contains("Destroy")) this.isDestroySelected = true;
                else if (gp.tile.getName().contains("Malus")) this.processMalusChoice();
                else if (gp.tile.getName().contains("Bonus")) this.processBonusChoice();
                break;
            }
        }
    }
    private void processTileChoice(MouseEvent e){
        int clickX = e.getX();
        int clickY = e.getY();
        int Width = 37;
        int Height = 60;
        SaboteurBoardState pbs = (SaboteurBoardState) getCurrentBoard().getBoardState();
        SaboteurTile[][] boardDisplayed = pbs.getBoardForDisplay();
        outer: for (int i = 0; i < SaboteurBoardState.BOARD_SIZE; i++) {
            for (int j = 0; j < SaboteurBoardState.BOARD_SIZE; j++) {
                if(boardDisplayed[i][j]==null) {
                    int yPos = i *Height  + 10;
                    int xPos = j * Width;
                    int[] newPos = {xPos,yPos};
                    if (clickInSquare(clickX, clickY, xPos, yPos,Height,Width)) {
                        if(pbs.verifyLegit(this.selectedTile.getPath(),newPos)) {
                            SaboteurMove move = new SaboteurMove(this.selectedTile, i, j, pbs.getTurnPlayer());
                            listener.moveEntered(move);
                            cancelMoveRequest();
                            resetSelection(); // Reset the selection variables
                            System.out.println("tile MOVE COMPLETED");
                        }
                        break outer;
                    }
                }
            }
        }
    }
    private void processMapChoice(MouseEvent e){
        int clickX = e.getX();
        int clickY = e.getY();
        int Width = 37;
        int Height = 60;
        SaboteurBoardState pbs = (SaboteurBoardState) getCurrentBoard().getBoardState();
        for(int h=0;h<3;h++){
            int i = SaboteurBoardState.hiddenPos[h][0];
            int j = SaboteurBoardState.hiddenPos[h][0];
            int yPos = i *Height  + 10;
            int xPos = j * Width;
            if (clickInSquare(clickX, clickY, xPos, yPos,Height,Width)) {
                SaboteurMove move = new SaboteurMove(new SaboteurMap(), h, 0, pbs.getTurnPlayer());
                listener.moveEntered(move);
                cancelMoveRequest();
                resetSelection(); // Reset the selection variables
                System.out.println("map MOVE COMPLETED");
                break;
            }
        }
    }
    private void processDestroyChoice(MouseEvent e){
        int clickX = e.getX();
        int clickY = e.getY();
        int Width = 37;
        int Height = 60;
        SaboteurBoardState pbs = (SaboteurBoardState) getCurrentBoard().getBoardState();
        SaboteurTile[][] boardDisplayed = pbs.getBoardForDisplay();
        ArrayList<SaboteurTile> fixTile = new ArrayList<SaboteurTile>();
        fixTile.add(0,boardDisplayed[SaboteurBoardState.originPos][SaboteurBoardState.originPos]);
        fixTile.add(1,boardDisplayed[SaboteurBoardState.hiddenPos[0][0]][SaboteurBoardState.hiddenPos[0][1]]);
        fixTile.add( 2,boardDisplayed[SaboteurBoardState.hiddenPos[1][0]][SaboteurBoardState.hiddenPos[1][1]]);
        fixTile.add(3,boardDisplayed[SaboteurBoardState.hiddenPos[2][0]][SaboteurBoardState.hiddenPos[2][1]]);
        outer: for (int i = 0; i < SaboteurBoardState.BOARD_SIZE; i++) {
            for (int j = 0; j < SaboteurBoardState.BOARD_SIZE; j++) {
                if (!(boardDisplayed[i][j]==null || fixTile.contains(boardDisplayed[i][j]))) {
                    int yPos = i *Height  + 10;
                    int xPos = j * Width;
                    if (clickInSquare(clickX, clickY, xPos, yPos,Height,Width)) {
                        SaboteurMove move = new SaboteurMove(new SaboteurDestroy(), i, j, pbs.getTurnPlayer());
                        listener.moveEntered(move);
                        cancelMoveRequest();
                        resetSelection(); // Reset the selection variables
                        System.out.println("destroy MOVE COMPLETED");
                        break outer;
                    }
                }
            }
        }
    }
    private void processMalusChoice(){
        SaboteurBoardState pbs = (SaboteurBoardState) getCurrentBoard().getBoardState();
        SaboteurMove move = new SaboteurMove(new SaboteurMalus(), 0, 0, pbs.getTurnPlayer());
        listener.moveEntered(move);
        cancelMoveRequest();
        resetSelection(); // Reset the selection variables
        System.out.println("malus MOVE COMPLETED");
    }
    private void processBonusChoice() {
        SaboteurBoardState pbs = (SaboteurBoardState) getCurrentBoard().getBoardState();
        SaboteurMove move = new SaboteurMove(new SaboteurBonus(), 0, 0, pbs.getTurnPlayer());
        listener.moveEntered(move);
        cancelMoveRequest();
        resetSelection(); // Reset the selection variables
        System.out.println("bonus MOVE COMPLETED");
    }

    private static boolean clickInSquare(int x, int y, int cx, int cy, int imgHeight, int imgWidth) {
        return (x - cx) <= imgWidth && (y - cy) <= imgHeight;
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
