package nl.tudelft.jpacman.level;

import java.util.Map;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.npc.Ghost;
import nl.tudelft.jpacman.sprite.AnimatedSprite;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A player operated unit in our game.
 *
 * @author Jeroen Roosen 
 */
public class Player extends Unit {

    private static final int KILLING_GHOST_BASE_SCORE = 200;

    /**
     * The amount of points accumulated by this player.
     */
    private int score;

    /**
     * The animations for every direction.
     */
    private final Map<Direction, Sprite> sprites;

    /**
     * The animation that is to be played when Pac-Man dies.
     */
    private final AnimatedSprite deathSprite;

    /**
     * <code>true</code> iff this player is alive.
     */
    private boolean alive;

    /**
     * The number of life.
     */
    private int life;

    /**
     * the number of ghosts killed by EL DEPREDATOR
     */
    private int predatorModeKillNumber;

    /**
     * Creates a new player with a score of 0 points.
     *
     * @param spriteMap
     *            A map containing a sprite for this player for every direction.
     * @param deathAnimation
     *            The sprite to be shown when this player dies.
     */
    protected Player(Map<Direction, Sprite> spriteMap, AnimatedSprite deathAnimation) {
        this.score = 0;
        this.alive = true;
        this.life = 1;
        this.sprites = spriteMap;
        this.deathSprite = deathAnimation;
        deathSprite.setAnimating(false);

        //BOOSKO Sam adding.
        this.predatorModeKillNumber = 0;
    }

    /**
     * Just for testing. Like the board gris test.
     */
    protected Player(){
        this.sprites = null;
        this.deathSprite = null;
    }

    /**
     * Returns whether this player is alive or not.
     *
     * @return <code>true</code> iff the player is alive.
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Sets whether this player is alive or not.
     *
     * @param isAlive
     *            <code>true</code> iff this player is alive.
     */
    public void setAlive(boolean isAlive) {
        if (isAlive) {
            deathSprite.setAnimating(false);
            this.alive = true;
        }
        if (!isAlive) {
            this.life--;
            if(this.life <= 0){
                deathSprite.restart();
                this.alive = false;
            }else{
                super.respawn();
                this.alive= true;
            }
        }
    }

    /**
     *
     * @return the number of life that the player still has.
     */
    public int getLifeLeft(){
        return this.life;
    }

    /**
     * Add one life to the pacman.
     */
    public void addLife(){
        this.life++;
    }

    /**
     * Reset the score bonus for the leak mode.
     */
    public void resetPredatorMod(){
        this.predatorModeKillNumber = 0;
    }

    /**
     *
     * @param ghost that the player killed.
     * @return the score for this ghost.
     */
    public int killGhost(Ghost ghost){
        this.predatorModeKillNumber += 1;

        ghost.respawn();

        return KILLING_GHOST_BASE_SCORE * this.predatorModeKillNumber;
    }

    /**
     * Returns the amount of points accumulated by this player.
     *
     * @return The amount of points accumulated by this player.
     */
    public int getScore() {
        return score;
    }

    @Override
    public Sprite getSprite() {
        if (isAlive()) {
            return sprites.get(getDirection());
        }
        return deathSprite;
    }

    /**
     * Adds points to the score of this player.
     *
     * @param points
     *            The amount of points to add to the points this player already
     *            has.
     */
    public void addPoints(int points) {
        score += points;
    }
}
