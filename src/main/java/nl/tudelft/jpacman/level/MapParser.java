package nl.tudelft.jpacman.level;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.tudelft.jpacman.PacmanConfigurationException;
import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.BoardFactory;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.npc.Ghost;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Creates new {@link Level}s from text representations.
 *
 * @author Jeroen Roosen
 */
public class MapParser {

    /**
     * The factory that creates the levels.
     */
    private final LevelFactory levelCreator;

    /**
     * The factory that creates the squares and board.
     */
    private final BoardFactory boardCreator;

    /**
     * The map with all square builders.
     */
    private final Map<Character, ISquareBuilder> squareBuilders = getSquareBuilders();

    /**
     * Creates a new map parser.
     *
     * @param levelFactory
     *            The factory providing the NPC objects and the level.
     * @param boardFactory
     *            The factory providing the Square objects and the board.
     */
    public MapParser(LevelFactory levelFactory, BoardFactory boardFactory) {
        this.levelCreator = levelFactory;
        this.boardCreator = boardFactory;
    }

    private Map<Character, ISquareBuilder> getSquareBuilders(){
        Map<Character, ISquareBuilder> squareBuilders = new HashMap<Character, ISquareBuilder>();

        squareBuilders.put(' ', new ADefaultSquareBuilder() {
            @Override
            protected Square getSquare() {
                return boardCreator.createGround();
            }
        });

        squareBuilders.put('#', new ADefaultSquareBuilder() {
            @Override
            protected Square getSquare() {
                return boardCreator.createWall();
            }
        });

        squareBuilders.put('.', new ADefaultSquareBuilder() {
            @Override
            protected Square getSquare() {
                Square pelletSquare = boardCreator.createGround();
                levelCreator.createPellet().occupy(pelletSquare);
                return pelletSquare;
            }
        });

        squareBuilders.put('G', new ADefaultSquareBuilder() {
            @Override
            protected Square getSquare() {
                return makeGhostSquare(super.ghosts, levelCreator.createGhost());
            }
        });

        squareBuilders.put('P', new ADefaultSquareBuilder() {
            @Override
            protected Square getSquare() {
                Square playerSquare = boardCreator.createGround();
                super.startPositions.add(playerSquare);
                return playerSquare;
            }
        });

        squareBuilders.put('0', new ADefaultSquareBuilder() {
            @Override
            protected Square getSquare() {
                Square powerPelletSquare = boardCreator.createGround();
                levelCreator.createPowerPellet().occupy(powerPelletSquare);
                return powerPelletSquare;
            }
        });

        squareBuilders.put('A', new ADefaultSquareBuilder() {
            @Override
            protected Square getSquare() {
                Square pelletSquare = boardCreator.createGround();
                levelCreator.createFruitPellet(FruitPellet.FruitType.APPLE).occupy(pelletSquare);
                return pelletSquare;
            }
        });

        squareBuilders.put('C', new ADefaultSquareBuilder() {
            @Override
            protected Square getSquare() {
                Square pelletSquare = boardCreator.createGround();
                levelCreator.createFruitPellet(FruitPellet.FruitType.CHERRY).occupy(pelletSquare);
                return pelletSquare;
            }
        });

        squareBuilders.put('M', new ADefaultSquareBuilder() {
            @Override
            protected Square getSquare() {
                Square pelletSquare = boardCreator.createGround();
                levelCreator.createFruitPellet(FruitPellet.FruitType.MELON).occupy(pelletSquare);
                return pelletSquare;
            }
        });

        squareBuilders.put('O', new ADefaultSquareBuilder() {
            @Override
            protected Square getSquare() {
                Square pelletSquare = boardCreator.createGround();
                levelCreator.createFruitPellet(FruitPellet.FruitType.ORANGE).occupy(pelletSquare);
                return pelletSquare;
            }
        });

        squareBuilders.put('S', new ADefaultSquareBuilder() {
            @Override
            protected Square getSquare() {
                Square pelletSquare = boardCreator.createGround();
                levelCreator.createFruitPellet(FruitPellet.FruitType.STRAWBERRY).occupy(pelletSquare);
                return pelletSquare;
            }
        });

        // Here are parsed characters for Special Boxes/Fruits

        squareBuilders.put('p', new ADefaultSquareBuilder() {
            @Override
            protected Square getSquare() {
                Square pelletSquare = boardCreator.createGround();
                levelCreator.createPepperPellet().occupy(pelletSquare);
                return pelletSquare;
            }
        });

        squareBuilders.put('t', new ADefaultSquareBuilder() {
            @Override
            protected Square getSquare() {
                Square pelletSquare = boardCreator.createGround();
                levelCreator.createTomatoPellet().occupy(pelletSquare);
                return pelletSquare;
            }
        });

        squareBuilders.put('f', new ADefaultSquareBuilder() {
            @Override
            protected Square getSquare() {
                Square pelletSquare = boardCreator.createGround();
                levelCreator.createFishPellet().occupy(pelletSquare);
                return pelletSquare;
            }
        });

        squareBuilders.put('h', new ADefaultSquareBuilder() {
            @Override
            protected Square getSquare() {
                Square pelletSquare = boardCreator.createGround();
                levelCreator.createPotatoPellet().occupy(pelletSquare);
                return pelletSquare;
            }
        });

        return squareBuilders;
    }

    /**
     * Parses the text representation of the board into an actual level.
     *
     * <ul>
     * <li>Supported characters:
     * <li>' ' (space) an empty square.
     * <li>'#' (bracket) a wall.
     * <li>'.' (period) a square with a pellet.
     * <li>'P' (capital P) a starting square for players.
     * <li>'G' (capital G) a square with a ghost.
     * <li>'A' (capital A) an apple.</li>
     * <li>'C' (capital C) a cherry.</li>
     * <li>'S' (capital S) a strawberry.</li>
     * <li>'O' (capital O) an orange.</li>
     * <li>'M' (capital M) a melon.</li>
     * </ul>
     *
     * @param map
     *            The text representation of the board, with map[x][y]
     *            representing the square at position x,y.
     * @return The level as represented by this text.
     */
    public Level parseMap(char[][] map) {
        int width = map.length;
        int height = map[0].length;

        Square[][] grid = new Square[width][height];

        List<Ghost> ghosts = new ArrayList<>();
        List<Square> startPositions = new ArrayList<>();

        makeGrid(new MakeGridParameters(map, width, height, grid, ghosts, startPositions));

        Board board = boardCreator.createBoard(grid);
        board.checkGrid();
        return levelCreator.createLevel(board, ghosts, startPositions);
    }

    private void makeGrid(MakeGridParameters makeGridParameters) {
        int width = makeGridParameters.width;
        int height = makeGridParameters.height;
        char[][] map = makeGridParameters.map;
        Square[][] grid = makeGridParameters.grid;
        List<Ghost> ghosts = makeGridParameters.ghosts;
        List<Square> startPositions = makeGridParameters.startPositions;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                char c = map[x][y];
                addSquare(new AddSquareParameters(grid, ghosts, startPositions, x, y, c));
            }
        }
    }

    /**
     * Adds a square to the grid based on a given character. These
     * character come from the map files and describe the type
     * of square.
     *
     * @param addSquareParameters
     */
    protected void addSquare(AddSquareParameters addSquareParameters) {
        Square[][] grid = addSquareParameters.grid;
        List<Square> startPositions = addSquareParameters.startPositions;
        int x = addSquareParameters.x;
        int y = addSquareParameters.y;
        char c = addSquareParameters.c;
        List<Ghost> ghosts = addSquareParameters.ghosts;

        ISquareBuilder builder = this.squareBuilders.getOrDefault(c, null);
        if(builder == null){
            throw new PacmanConfigurationException("Invalid character at "
                + x + "," + y + ": " + c);
        }

        builder.buildSquare(addSquareParameters);

    }

    /**
     * creates a Square with the specified ghost on it
     * and appends the placed ghost into the ghost list.
     *
     * @param ghosts all the ghosts in the level so far, the new ghost will be appended
     * @param ghost the newly created ghost to be placed
     * @return a square with the ghost on it.
     */
    protected Square makeGhostSquare(List<Ghost> ghosts, Ghost ghost) {
        Square ghostSquare = boardCreator.createGround();
        ghosts.add(ghost);
        ghost.occupy(ghostSquare);
        ghost.setSpawnSquare(ghostSquare);
        return ghostSquare;
    }

    /**
     * Parses the list of strings into a 2-dimensional character array and
     * passes it on to {@link #parseMap(char[][])}.
     *
     * @param text
     *            The plain text, with every entry in the list being a equally
     *            sized row of squares on the board and the first element being
     *            the top row.
     * @return The level as represented by the text.
     * @throws PacmanConfigurationException If text lines are not properly formatted.
     */
    public Level parseMap(List<String> text) {

        checkMapFormat(text);

        int height = text.size();
        int width = text.get(0).length();

        char[][] map = new char[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = text.get(y).charAt(x);
            }
        }
        return parseMap(map);
    }

    /**
     * Check the correctness of the map lines in the text.
     * @param text Map to be checked
     * @throws PacmanConfigurationException if map is not OK.
     */
    private void checkMapFormat(List<String> text) {
        if (text == null) {
            throw new PacmanConfigurationException(
                "Input text cannot be null.");
        }

        if (text.isEmpty()) {
            throw new PacmanConfigurationException(
                "Input text must consist of at least 1 row.");
        }

        int width = text.get(0).length();

        if (width == 0) {
            throw new PacmanConfigurationException(
                "Input text lines cannot be empty.");
        }

        for (String line : text) {
            if (line.length() != width) {
                throw new PacmanConfigurationException(
                    "Input text lines are not of equal width.");
            }
        }
    }

    /**
     * Parses the provided input stream as a character stream and passes it
     * result to {@link #parseMap(List)}.
     *
     * @param source
     *            The input stream that will be read.
     * @return The parsed level as represented by the text on the input stream.
     * @throws IOException
     *             when the source could not be read.
     */
    public Level parseMap(InputStream source) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            source, "UTF-8"))) {
            List<String> lines = new ArrayList<>();
            while (reader.ready()) {
                lines.add(reader.readLine());
            }
            return parseMap(lines);
        }
    }

    /**
     * Parses the provided input stream as a character stream and passes it
     * result to {@link #parseMap(List)}.
     *
     * @param mapName
     *            Name of a resource that will be read.
     * @return The parsed level as represented by the text on the input stream.
     * @throws IOException
     *             when the resource could not be read.
     */
    @SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION",
                        justification = "try with resources always cleans up")
    public Level parseMap(String mapName) throws IOException {
        try (InputStream boardStream = MapParser.class.getResourceAsStream(mapName)) {
            if (boardStream == null) {
                throw new PacmanConfigurationException("Could not get resource for: " + mapName);
            }
            return parseMap(boardStream);
        }
    }

    /**
     * @return the BoardCreator
     */
    protected BoardFactory getBoardCreator() {
        return boardCreator;
    }

    /**
     * Interface to generate board game element from character
     */
    public interface ISquareBuilder{
        void buildSquare(AddSquareParameters addSquareParameters);
    }

    private abstract class ADefaultSquareBuilder implements ISquareBuilder{
        private Square[][] grid;
        private List<Ghost> ghosts;
        private List<Square> startPositions;
        private int x;
        private int y;
        private char c;

        @Override
        public void buildSquare(AddSquareParameters addSquareParameters){
            this.grid = addSquareParameters.grid;
            this.ghosts = addSquareParameters.ghosts;
            this.startPositions = addSquareParameters.startPositions;
            this.x = addSquareParameters.x;
            this.y = addSquareParameters.y;
            this.c = addSquareParameters.c;

            this.grid[x][y] = this.getSquare();
        }

        protected abstract Square getSquare();
    }

    private static class AddSquareParameters {
        public final Square[][] grid;
        public final List<Ghost> ghosts;
        public final List<Square> startPositions;
        public final int x;
        public final int y;
        public final char c;

        /**
         * @param grid
         *            The grid of squares with board[x][y] being the
         *            square at column x, row y.
         * @param ghosts
         *            List of all ghosts that were added to the map.
         * @param startPositions
         *            List of all start positions that were added
         *            to the map.
         * @param x
         *            x coordinate of the square.
         * @param y
         *            y coordinate of the square.
         * @param c
         */
        private AddSquareParameters(Square[][] grid, List<Ghost> ghosts, List<Square> startPositions, int x, int y, char c) {
            this.grid = grid;
            this.ghosts = ghosts;
            this.startPositions = startPositions;
            this.x = x;
            this.y = y;
            this.c = c;
        }
    }

    private static class MakeGridParameters {
        public final char[][] map;
        public final int width;
        public final int height;
        public final Square[][] grid;
        public final List<Ghost> ghosts;
        public final List<Square> startPositions;

        private MakeGridParameters(char[][] map, int width, int height, Square[][] grid, List<Ghost> ghosts, List<Square> startPositions) {
            this.map = map;
            this.width = width;
            this.height = height;
            this.grid = grid;
            this.ghosts = ghosts;
            this.startPositions = startPositions;
        }
    }
}
