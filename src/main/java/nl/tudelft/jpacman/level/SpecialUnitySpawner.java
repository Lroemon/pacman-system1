package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.level.specialbox.SpecialBox;
import nl.tudelft.jpacman.level.specialbox.TeleporterBox;
import nl.tudelft.jpacman.level.specialpellet.SpecialPellet;

import java.util.ArrayList;
import java.util.Random;

/**
 * A manager to dynamically spawn new special units ({@link SpecialBox} and {@link SpecialPellet}) in a way that
 * makes sense with current state of a level. It uses probabilities computation, following this logic :
 *   - the spawning chance depends on the current number of pellets compared to initially (the less pellets are present,
 *     the higher is chance to spawn new units.
 *   - the fact that it will be a bonus or penalty depends on the current player score compared to a fixed reference,
 *     the more score he has, the higher is the chance to spawn units applying penalty. When score is high, the game
 *     is close to end and now way to facilitate it for the player !
 *
 * The linking with the level is done using {@link #setLevel(Level)}, and the time part is handled by it. It means
 * it should call at a time interval {@link #trySpawnSpecial()} to spawn new unit in with some probability depending
 * its current state.
 *
 * @author Rémy Decocq
 */
public class SpecialUnitySpawner {

    private static final int NBR_BONUSES = 4;
    private static final int NBR_PENALTIES = 2;
    private static final int NBR_BOXES = 3;

    private static final float PELLET_CHANCE = 0.7f;
    private static final float REFERENCE_SCORE = 10000;
    private static final float CAP_SPAWN_CHANCE = 0.7f;

    private final Random rdm = new Random();
    private final LevelFactory levelCreator;
    private Level level;
    private int initNbrPellets;
    private ArrayList<Square> lastFreeSquares;

    /**
     * Create a spawner for new special units in a given level to set with {@link #setLevel(Level)}, using an already
     * existing factory to instantiate those new units
     *
     * @param levelCreator a factory for new units
     */
    public SpecialUnitySpawner(LevelFactory levelCreator){
        this.levelCreator = levelCreator;
    }

    /**
     * Spawn a new special unit with a given chance. The finally chosen unit depends again on a probabilistic process
     * (see {@link SpecialUnitySpawner}).
     *
     * @param chanceSpawn the chance to spawn a new special unit
     * @return true iff spawned
     */
    public boolean trySpawnSpecial(float chanceSpawn){
        assert level != null;
        if (rdm.nextFloat() > chanceSpawn)
            return false;
        int currPlayerScore = (int) Math.min(REFERENCE_SCORE, level.getOnePlayer().getScore());
        boolean spawnPellet = rdm.nextFloat() < PELLET_CHANCE;
        if (spawnPellet){
            float chancePenalty = currPlayerScore / REFERENCE_SCORE; // less score less chance to get penalty
            Pellet toPlace = rdm.nextFloat() < chancePenalty ? getPenalty() : getBonus();
            return this.placePellet(toPlace);
        } else {
            return this.placeBox();
        }
    }

    /**
     * Try with an automatically computed probability to spawn a new special unit on a free square of the board.
     * This probability depends on the current number of available pellets against initial number.
     * @return true iff a unit was spawned
     */
    public boolean trySpawnSpecial(){
        this.lastFreeSquares = this.level.getBoard().getFreeOccupantSquares();
        if (lastFreeSquares.size() == 0)
            return false;
        float chanceSpawn = 1 - this.level.remainingPellets() / (float) (this.initNbrPellets + 1);
        chanceSpawn = Math.min(CAP_SPAWN_CHANCE, chanceSpawn);
        return this.trySpawnSpecial(chanceSpawn);
    }

    private Pellet getBonus(){
        int ind = rdm.nextInt(NBR_BONUSES);
        switch (ind){
            case 0: return levelCreator.createGrenadePellet();
            case 1: return levelCreator.createPepperPellet();
            case 2: return levelCreator.createTomatoPellet();
            default: return levelCreator.createPowerPellet();
        }
    }

    private Pellet getPenalty(){
        int ind = rdm.nextInt(NBR_PENALTIES);
        switch (ind){
            case 0: return levelCreator.createPotatoPellet();
            default: return levelCreator.createFishPellet();
        }
    }

    private boolean placeBox(){
        int indSel = rdm.nextInt(this.lastFreeSquares.size());
        Square target = this.lastFreeSquares.get(indSel);
        int ind = rdm.nextInt(NBR_BOXES);
        if (ind == 0){
            levelCreator.createTrapBox().occupy(target);
            return true;
        } else if (ind == 1 && this.lastFreeSquares.size() > 1){
            int oldIndSel = indSel;
            do{
                indSel = rdm.nextInt(this.lastFreeSquares.size());
            } while (indSel == oldIndSel);
            Square targetTo = this.lastFreeSquares.get(indSel);
            TeleporterBox t1, t2;
            t1 = levelCreator.createTeleporterBox();
            t2 = levelCreator.createTeleporterBox();
            t1.setLinked(t2);
            t1.occupy(target);
            t2.occupy(targetTo);
            return true;
        } else if (ind == 2){
            levelCreator.createBridgeBox(Direction.getRdmDir()).occupy(target);
            return true;
        }
        return false;
    }

    private boolean placePellet(Pellet pellet) {
        int indSel = rdm.nextInt(this.lastFreeSquares.size());
        Square target = this.lastFreeSquares.get(indSel);
        pellet.occupy(target);
        return true;
    }

    /**
     * Set the level to consider to spawn new unit in and retrieve material for probabilities computation
     * Reciprocally register this spawner to the given level {@link Level#setSpawner(SpecialUnitySpawner)}.
     *
     * @param level the level to consider
     */
    public void setLevel(Level level){
        this.level = level;
        this.initNbrPellets = this.level.remainingPellets();
        this.level.setSpawner(this);
    }

}
