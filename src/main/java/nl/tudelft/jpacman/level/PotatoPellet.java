package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.npc.Ghost;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;

import java.util.Map;

/**
 * A Potato pellet ({@link SpecialPellet}) is a penalty that increases ghosts speed when eaten, with variation
 * considering the current state of the level.
 * The duration of the effect depends on the current score : the more Pacman has, the more he's a juicy target for
 * ghosts and so they will be buffed longer. Miam miam.
 *
 * @author Rémy Decocq
 */
public class PotatoPellet extends SpecialPellet {

    public static final float SPEED_MULTIPLIER = 2f;

    public static final long BASIC_DURATION = 4000L;
    public static final long INCREASED_DURATION = 7000L;

    public static final int SCORE_THRESH_DURATION = 1500;

    private static final Map<Direction, Sprite> pacmanSprites = new PacManSprites().getPacmanPotatoSprites();

    /**
     * Creates a new pellet.
     *
     * @param points The point value of this pellet.
     * @param sprite The sprite of this pellet.
     */
    public PotatoPellet(int points, Sprite sprite) {
        super(points, sprite);
    }

    private static void increaseGhostsSpeed(Level level) {
        for(Ghost g: level.getGhosts()){
            g.setSpeedMultiplier(SPEED_MULTIPLIER);
        }
    }

    private static void resetGhostsSpeed(Level level) {
        for(Ghost g: level.getGhosts()){
            g.resetSpeedMultiplier();
        }
    }

    private long getDuration(Player player){
        return player.getScore() < SCORE_THRESH_DURATION ? BASIC_DURATION : INCREASED_DURATION;
    }

    @Override
    public void onEat(Level level, Player player){
        super.onEat(level, player);
        long duration = this.getDuration(player);
        increaseGhostsSpeed(level);
        setNewStatePlayer(player, Player.SpecialStates.ON_POTATO, pacmanSprites);
        scheduleEffectDuration(new StopPotatoEffect(player, level), duration);
    }

    private class StopPotatoEffect extends StopEffect{

        private final Level level;

        /**
         * @param player the Pacman on which the effect has been activated.
         */
        public StopPotatoEffect(Player player, Level level) {
            super(player);
            this.level = level;
        }

        /**
         * Reset the player special state (to {@link Player.SpecialStates#NONE}) and Pacman's sprites he
         * used at instantiation. The reset operated by superclass does not enable
         * resetting {@link Ghost#resetSpeedMultiplier()} so it is overloaded here.
         */
        @Override
        public void run() {
            super.run();
            resetGhostsSpeed(this.level);
        }
    }
}
