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
     * Game where the player is.
     */
    private final Game game;
    /**
     * Player that controller controls.
     */
    private final Player player;

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
        return (long)( 1000L / this.player.getSpeed());
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
