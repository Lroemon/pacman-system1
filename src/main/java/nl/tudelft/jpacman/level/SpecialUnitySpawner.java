package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;

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
 */
public class SpecialUnitySpawner {

    private static final int NBR_BONUSES = 4;
    private static final int NBR_PENALTIES = 2;
    private static final int NBR_BOXES = 3;

    private static final float PELLET_CHANCE = 0.7f;
    private static final float BOX_CHANCE = 1 - PELLET_CHANCE;
    private static final float REFERENCE_SCORE = 10000;
    private static final float CAP_SPAWN_CHANCE = 0.7f;

    private final Random rdm = new Random();
    private final LevelFactory levelCreator;
    private Level level;
    private int initNbrPellets;
    private ArrayList<Square> lastFreeSquares;

    public SpecialUnitySpawner(LevelFactory levelCreator){
        this.levelCreator = levelCreator;
    }

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
            levelCreator.createBridgeBox(Direction.getRdmDir());
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

    public void setLevel(Level level){
        this.level = level;
        this.initNbrPellets = this.level.remainingPellets();
        this.level.setSpawner(this);
    }

}
