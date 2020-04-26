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
     * Number of tiles moved per seconde. From https://www.slideshare.net/grimlockt/pac-man-6561257 slide 48
     */
    private static final float PLAYER_SPEED = 10f;

    /**
     * The amount of points accumulated by this player.
     */
    private int score;

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
     * Speed of the player by tile per second.
     */
    private final float speed = PLAYER_SPEED;

    /**
     * Speed modifier for new features. The value is assessed by newSpeed = speed * speedModifier.
     */
    private float speedModifier;

    /**
     * A current special state among {@link SpecialStates} enumeration, influences
     * diverse game rules depending bonus/penalty effect related to this state
     */
    private SpecialStates specState;

    /**
     * The animations for every direction, storing the original one.
     */
    private Map<Direction, Sprite> sprites, basic_sprite;

    /**
     * The animation that is to be played when Pac-Man dies.
     */
    private final AnimatedSprite deathSprite;


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
        this.speedModifier = 1f;
        this.specState = SpecialStates.NONE;
        this.sprites = spriteMap;
        this.basic_sprite = spriteMap;
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

    /**
     * Set new sprites to use for Pacman.
     * @param sprites the new Sprites considering each {@link Direction} possible.
     */
    public void setSprite(Map<Direction, Sprite> sprites) {
        this.sprites = sprites;
    }

    /**
     * Reset the current Pacman sprite to the one used originally when instantiated
     */
    public void resetSprite(){
        this.sprites = this.basic_sprite;
    }

    /**
     * Get current sprite used to display Pacman.
     */
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

    /**
     *
     * @return true if the speed modifier can be edited, else false.
     */
    public boolean setSpeedModifier(float speedModifier){
        if(Math.abs(speedModifier) < 1e-10){
            return false;
        }
        this.speedModifier = speedModifier;
        return true;
    }

    /**
     * Reset the {@link #speedModifier} to 1 (no modification on regular speed).
     */
    public void resetSpeedModifier(){
        this.speedModifier = 1f;
    }

    /**
     *
     * @return the speed of the player.
     */
    public float getSpeed(){
        return this.speed * this.speedModifier;
    }

    /**
     * Get the current special state {@link SpecialStates}.
     * @return the current special state with {@link SpecialStates#NONE} meaning no special state currently applied.
     */
    public SpecialStates getSpecialState() {
        return this.specState;
    }

    /**
     * @return whether there is a currently special state applied (so not {@link SpecialStates#NONE}).
     */
    public boolean isOnSpecialState() {
        return this.specState != SpecialStates.NONE;
    }

    /**
     * @param testState state to check against current one.
     * @return whether the current special state is the one given.
     */
    public boolean isOnSpecialState(SpecialStates testState) {
        return this.specState == testState;
    }

    /**
     * Change the current special state
     * @param specialState
     */
    public void setSpecialState(SpecialStates specialState) {
        this.specState = specialState;
    }

    /**
     * Reset the current special state to {@link SpecialStates#NONE} meaning no state applied.
     */
    public void resetSpecialState() {
        this.specState = SpecialStates.NONE;
    }

    /**
     * Possible special states applicable to Pacman, taking in account in diverse game rules (bonuses/penalties).
     */
    public enum SpecialStates {
        NONE, ON_PEPPER, ON_TOMATO, ON_BEAN, ON_POTATO, ON_FISH
    }

}
