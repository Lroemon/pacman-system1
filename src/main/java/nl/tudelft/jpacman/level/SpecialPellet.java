package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.sprite.Sprite;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A special pellet can give bonus/penalty to Pacman when eaten (behaviour defined by subclassing). The effect has a
 * given duration and may change Pacman's state {@link Player#getSpecialState()} taken into account by some game rules
 * depending the considered effect. Also, this state modification may be signaled changing Pacman's current sprites.
 */
public class SpecialPellet extends Pellet {

    /**
     * The timer handling special effect duration.
     */
    private final Timer timer;

    /**
     * Creates a new pellet.
     *
     * @param points The point value of this pellet.
     * @param sprite The sprite of this pellet.
     */
    public SpecialPellet(int points, Sprite sprite) {
        super(points, sprite);
        this.timer = new Timer();
    }

    /**
     * Change the current special state for a Pacman ({@link Player}) and the new sprites to display
     * @param player
     * @param newState
     * @param newPacmanSprites directed sprites based on ones used by default to display Pacman
     */
    protected static void setNewStatePlayer(Player player, Player.SpecialStates newState,
                                            Map<Direction, Sprite> newPacmanSprites) {
        player.setSpecialState(newState);
        player.setSprite(newPacmanSprites);
    }

    /**
     * @see #setNewStatePlayer(Player, Player.SpecialStates, Map)  
     * @param player
     * @param newState
     */
    protected static void setNewStatePlayer(Player player, Player.SpecialStates newState){
        player.setSpecialState(newState);
    }

    /**
     * Instantiate a new {@link StopEffect} task that will reset player state after a duration.
     * @param player the player to reset default special state (to {@link Player.SpecialStates#NONE}) and sprites he
     *               used at instantiation.
     * @param durationMs the time in millisecond to pass before stopping any special effect (reset).
     */
    protected void scheduleEffectDuration(Player player, long durationMs){
        this.timer.schedule(new StopEffect(player), durationMs);
    }

    /**
     * Specify a customized action to handle at the en of the effect.
     * @see #scheduleEffectDuration(Player, long)
     * @param stopTask should be a specialized subclass of {@link StopEffect}.
     * @param durationMs
     */
    protected void scheduleEffectDuration(StopEffect stopTask, long durationMs){
        this.timer.schedule(stopTask, durationMs);
    }

    /**
     * A task handled when the special effect duration expires, generic and complying with most of special effects.
     * semantic.
     */
    protected class StopEffect extends TimerTask {

        private final Player player;

        /**
         * @param player the Pacman on which the effect has been activated.
         */
        public StopEffect(Player player){
            super();
            this.player = player;
        }

        /**
         * Reset the player special state (to {@link Player.SpecialStates#NONE}) and Pacman's sprites he
         * used at instantiation.
         */
        @Override
        public void run() {
            player.resetSpecialState();
            player.resetSprite();
        }
    }
}
