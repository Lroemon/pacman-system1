package nl.tudelft.jpacman.board;


import nl.tudelft.jpacman.PacmanConfigurationException;
import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.level.PlayerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * A top-down view of a matrix of {@link Square}s.
 *
 * @author Jeroen Roosen 
 */
public class Board {

    /**
     * The grid of squares with board[x][y] being the square at column x, row y.
     */
    private final Square[][] board;

    /**
     * Creates a new board.
     *
     * @param grid
     *            The grid of squares with grid[x][y] being the square at column
     *            x, row y.
     */
    @SuppressWarnings("PMD.ArrayIsStoredDirectly")
    Board(Square[][] grid) {
        assert grid != null;
        this.board = grid;
        assert invariant() : "Initial grid cannot contain null squares";
    }

    /**
     * Whatever happens, the squares on the board can't be null.
     * @return false if any square on the board is null.
     */
    protected final boolean invariant() {
        for (Square[] row : board) {
            for (Square square : row) {
                if (square == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the number of columns.
     *
     * @return The width of this board.
     */
    public int getWidth() {
        return board.length;
    }

    /**
     * Returns the number of rows.
     *
     * @return The height of this board.
     */
    public int getHeight() {
        return board[0].length;
    }

    /**
     * Returns the square at the given <code>x,y</code> position.
     *
     * Precondition: The <code>(x, y)</code> coordinates are within the
     * width and height of the board.
     *
     * @param x
     *            The <code>x</code> position (column) of the requested square.
     * @param y
     *            The <code>y</code> position (row) of the requested square.
     * @return The square at the given <code>x,y</code> position (never null).
     */
    public Square squareAt(int x, int y) {
        assert withinBorders(x, y);
        Square result = board[x][y];
        assert result != null : "Follows from invariant.";
        return result;
    }

    /**
     * Determines whether the given <code>x,y</code> position is on this board.
     *
     * @param x
     *            The <code>x</code> position (row) to test.
     * @param y
     *            The <code>y</code> position (column) to test.
     * @return <code>true</code> iff the position is on this board.
     */
    public boolean withinBorders(int x, int y) {
        return x >= 0 && x < getWidth() && y >= 0 && y < getHeight();
    }

    /**
     * Check if the grid is well done. It means that all way of the map is accessible.
     */
    public void checkGrid(){
        Player player = new PlayerFactory(null).createTestPacMand();
        Square first = getFirstAccessibleSquare(player);

        List<Square> explored = explore(first, player);

        for(Square[] squaresLine : this.board){
            for(Square square : squaresLine){
                if(square.isAccessibleTo(player)){
                    if(!explored.contains(square)){
                        throw new PacmanConfigurationException("All squares of the board are not accessible by PacMan !");
                    }
                }
            }
        }
    }

    /**
     *
     * @param first the first square to explore.
     * @param unit the unit which explores.
     * @return a list of Square explored.
     */
    private List<Square> explore(Square first, Unit unit){
        List<Square> explored = new LinkedList<Square>();
        Queue<Square> toExplore = new LinkedList<Square>();

        do{
            explored.add(first);
            toExplore.addAll(nextSquares(first, unit, explored));
            first = toExplore.poll();
        }while(first != null);

        return explored;
    }

    /**
     *
     * @param square the square where the unit is.
     * @param unit the unit which moves.
     * @param except restriction list of squares.
     * @return the list of square that this unit can go.
     */
    private List<Square> nextSquares(Square square, Unit unit, List<Square> except){
        List<Square> squares = new LinkedList<Square>();

        for (Direction direction : Direction.values()){
            Square nextSquare = square.getSquareAt(direction);
            boolean isAccessible = nextSquare == null ? false : nextSquare.isAccessibleTo(unit);

            if(isAccessible && !except.contains(nextSquare)){
                squares.add(nextSquare);
            }
        }

        return squares;
    }

    /**
     *
     * @return the first square which is accessible.
     */
    private Square getFirstAccessibleSquare(Unit unit){
        for(Square[] squareLine : this.board){
            for(Square square : squareLine){
                if(square.isAccessibleTo(unit)){
                    return square;
                }
            }
        }
        return null;
    }

    public ArrayList<Square> getFreeOccupantSquares(){
        return this.getSquaresByOccupantsNbr(0);
    }

    public ArrayList<Square> getSquaresByOccupantsNbr(int nbr){
        ArrayList<Square> frees = new ArrayList<>();
        for(int i=0; i < this.getWidth(); i++){
            for(int j=0; j < this.getHeight(); j++){
                Square s = this.squareAt(i, j);
                if(s.getOccupants().size() == nbr && !(s instanceof BoardFactory.Wall))
                    frees.add(this.squareAt(i, j));
            }
        }
        return frees;
    }

}
