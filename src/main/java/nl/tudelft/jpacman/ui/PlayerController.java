package nl.tudelft.jpacman.ui;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.game.Game;
import nl.tudelft.jpacman.level.Player;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Class to manage the player continious movement
 * @author BOOSKO Sam
 */
public class PlayerController{

    /**
     * Number of tiles moved per seconde. From https://www.slideshare.net/grimlockt/pac-man-6561257 slide 48
     */
    private static final float PLAYER_SPEED = 10f;

    /**
     * Game where the player is.
     */
    private final Game game;
    /**
     * Player that controller controls.
     */
    private final Player player;
    /**
     * Speed of the player by tile per second.
     */
    private final float speed;

    /**
     * Speed modifier for new features. The value is assessed by newSpeed = speed * speedModifier.
     */
    private float speedModifier;

    /**
     * Direction of the player movement.
     */
    private Direction currentDirection;

    /**
     * bool to know if the player should do new moving step.
     */
    private boolean isMoving;

    /**
     * Timer for the player movement.
     */
    private final Timer timer;

    /**
     * Contructor
     * @param game where the player is.
     * @param player that controller controls.
     */
    public PlayerController(Game game, Player player){
        this.game = game;
        this.player = player;
        this.speed = PLAYER_SPEED;
        this.speedModifier = 1f;

        this.currentDirection = null;

        this.timer = new Timer();
    }

    /**
     * Change the player movement direction.
     * @param newDirection the new direction.
     */
    public void setDirection(Direction newDirection){
        this.currentDirection = newDirection;
    }

    /**
     *
     * @return the speed of the player.
     */
    public float getSpeed(){
        return this.speed * this.speedModifier;
    }

    /**
     *
     * @return true if the speed modifier can be edited, else fase.
     */
    public boolean setSpeedModifier(float speedModifier){
        if(Math.abs(speedModifier) < 1e-10){
            return false;
        }
        this.speedModifier = speedModifier;
        return true;
    }

    /**
     * Start the moving of the player.
     */
    public void startMoving(){
        this.isMoving = true;
        this.nextStep();
    }

    /**
     * Stop the moving of the player.
     */
    public void stopMoving(){
        this.isMoving = false;
    }

    /**
     * Schedule the next moving step.
     */
    private void nextStep(){
        this.timer.schedule(new PlayerTaskMovement(), this.getNextStepTime());
    }

    /**
     * @return the time in milliseconds before the next moving step;
     */
    private long getNextStepTime(){
        return (long)( 1000L / this.getSpeed());
    }

    /**
     * Runnable class to move the player depending on his speed and direction.
     */
    private class PlayerTaskMovement extends TimerTask {

        @Override
        public void run() {
            if(isMoving){
                if(currentDirection != null){
                    game.move(player, currentDirection);
                }
                nextStep();
            }
        }
    }

}
