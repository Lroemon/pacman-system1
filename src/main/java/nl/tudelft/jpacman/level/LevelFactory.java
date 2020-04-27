package nl.tudelft.jpacman.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.npc.Ghost;
import nl.tudelft.jpacman.npc.ghost.GhostColor;
import nl.tudelft.jpacman.npc.ghost.GhostFactory;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * Factory that creates levels and units.
 *
 * @author Jeroen Roosen 
 */
public class LevelFactory {

    private static final int GHOSTS = 4;
    private static final int BLINKY = 0;
    private static final int INKY = 1;
    private static final int PINKY = 2;
    private static final int CLYDE = 3;

    /**
     * The default value of a pellet.
     */
    private static final int PELLET_VALUE = 10;

    /**
     * The default value of a power pellet.
     */
    private static final int POWER_PELLET_VALUE = 50;

    /**
     * The sprite store that provides sprites for units.
     */
    private final PacManSprites sprites;

    /**
     * Used to cycle through the various ghost types.
     */
    private int ghostIndex;

    /**
     * The factory providing ghosts.
     */
    private final GhostFactory ghostFact;

    /**
     * A list of teleporters fed as new one are instantiated, used to link them by pairs (two last ones each time)
     */
    private final ArrayList<TeleporterBox> teleporters;

    /**
     * Creates a new level factory.
     *
     * @param spriteStore
     *            The sprite store providing the sprites for units.
     * @param ghostFactory
     *            The factory providing ghosts.
     */
    public LevelFactory(PacManSprites spriteStore, GhostFactory ghostFactory) {
        this.sprites = spriteStore;
        this.ghostIndex = -1;
        this.ghostFact = ghostFactory;

        this.teleporters = new ArrayList<>();
    }

    /**
     * Creates a new level from the provided data.
     *
     * @param board
     *            The board with all ghosts and pellets occupying their squares.
     * @param ghosts
     *            A list of all ghosts on the board.
     * @param startPositions
     *            A list of squares from which players may start the game.
     * @return A new level for the board.
     */
    public Level createLevel(Board board, List<Ghost> ghosts,
                             List<Square> startPositions) {

        // We'll adopt the simple collision map for now.
        //CollisionMap collisionMap = new PlayerCollisions();
        //Not SIMPLE SAM
        DefaultPlayerInteractionMap collisionMap = new DefaultPlayerInteractionMap();
        Level level = new Level(board, ghosts, startPositions, collisionMap);

        collisionMap.setLevel(level);

        return level;
    }

    /**
     * Creates a new ghost.
     *
     * @return The new ghost.
     */
    public Ghost createGhost() {
        Ghost ghost;
        ghostIndex++;
        ghostIndex %= GHOSTS;
        switch (ghostIndex) {
            case BLINKY:
                ghost = ghostFact.createBlinky(); break;
            case INKY:
                ghost = ghostFact.createInky(); break;
            case PINKY:
                ghost = ghostFact.createPinky(); break;
            case CLYDE:
                ghost = ghostFact.createClyde(); break;
            default:
                ghost = new RandomGhost(sprites.getGhostSprite(GhostColor.RED));
        }
        return ghost;
    }

    /**
     * Creates a new pellet.
     *
     * @return The new pellet.
     */
    public Pellet createPellet() {
        return new Pellet(PELLET_VALUE, sprites.getPelletSprite());
    }

    /**
     * Creates a new power pellet.
     *
     * @return The new power pellet.
     */
    public PowerPellet createPowerPellet(){ return new PowerPellet(POWER_PELLET_VALUE, sprites.getPowerPelletSprite()); }

    /**
     *
     * @param fruit the type of the fruit.
     * @return a fruit.
     */
    public FruitPellet createFruitPellet(FruitPellet.FruitType fruit){
        return new FruitPellet(PELLET_VALUE, sprites.loadSprite(fruit.spritePath));
    }

    // Special Boxes

    /**
     * @return a Trap that locks units for an amount of time (see {@link TrapBox}).
     */
    public TrapBox createTrapBox(){
        return new TrapBox(sprites.getTrapBoxSprite());
    }

    /**
     * @return a Teleporter that is paired with another one if possible (see {@link TeleporterBox}).
     */
    public TeleporterBox createTeleporterBox(){
        TeleporterBox toLinkWith = null;
        for (TeleporterBox t: this.teleporters){
            toLinkWith = !t.isLinked() ? t : null;
        }
        TeleporterBox newTp = new TeleporterBox(toLinkWith, sprites.getTeleporterBox(toLinkWith != null));
        this.teleporters.add(newTp);
        return newTp;
    }

    /**
     * @param direction an orientation for the bridge (up road)
     * @return a Bridge that impact {@link nl.tudelft.jpacman.board.Unit.VerticalPos} of units (see {@link BridgeBox})
     */
    public BridgeBox createBridgeBox(Direction direction){
        return new BridgeBox(direction, sprites.getBridgeBox(direction));
    }

    // Special Pellets

    /**
     * @return a Grenade pellet with special effect when eaten by Pacman (see {@link GrenadePellet})
     */
    public GrenadePellet createGrenadePellet(){
        return new GrenadePellet(PELLET_VALUE, sprites.getGrenadePelletSprite());
    }

    /**
     * @return a Pepper pellet with special effect when eaten by Pacman (see {@link PepperPellet})
     */
    public PepperPellet createPepperPellet(){
        return new PepperPellet(PELLET_VALUE, sprites.getPepperPelletSprite());
    }

    /**
     * @return a Tomato pellet with special effect when eaten by Pacman (see {@link TomatoPellet})
     */
    public TomatoPellet createTomatoPellet(){
        return new TomatoPellet(PELLET_VALUE, sprites.getTomatoPelletSprite());
    }

    /**
     * @return a Potato pellet with special negative effect when eaten by Pacman (see {@link PotatoPellet})
     */
    public PotatoPellet createPotatoPellet(){
        return new PotatoPellet(PELLET_VALUE, sprites.getPotatoPelletSprite());
    }

    /**
     * @return a Fish pellet with special effect when eaten by Pacman (see {@link FishPellet})
     */
    public FishPellet createFishPellet() {
        return new FishPellet(PELLET_VALUE, sprites.getFishPelletSprite());
    }


    /**
     * Implementation of an NPC that wanders around randomly.
     *
     * @author Jeroen Roosen
     */
    private static final class RandomGhost extends Ghost {

        /**
         * The suggested delay between moves.
         */
        private static final long DELAY = 175L;

        /**
         * Creates a new random ghost.
         *
         * @param ghostSprite
         *            The sprite for the ghost.
         */
        RandomGhost(Map<Direction, Sprite> ghostSprite) {
            super(ghostSprite, (int) DELAY, 0);
        }

        @Override
        public Optional<Direction> nextAiMove() {
            return Optional.empty();
        }
    }
}
