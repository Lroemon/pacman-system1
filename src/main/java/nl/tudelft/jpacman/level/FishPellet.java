package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.npc.Ghost;
import nl.tudelft.jpacman.npc.ghost.Navigation;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;

import java.util.List;
import java.util.Map;

/**
 * A Fish pellet ({@link SpecialPellet}) is a penalty that locks pacman down still in his square, with variation
 * considering the current state of the level.
 * The duration of the non-movable state for Pacman depends on the distance of the closest Ghost. There is a base time
 * anyway, so sometimes Pacman will be fated to die but that's his fault, don't eat Nemo Pacman ! The closer a ghost
 * is, the less time he will be trapped though.
 */
public class FishPellet extends SpecialPellet {

    public static final int GHOST_DIST_THRESH = 12;

    public static final long BASIC_DURATION = 1000L;
    public static final long INCREASED_DURATION = 3000L;

    private static final Map<Direction, Sprite> pacmanSprites = new PacManSprites().getPacmanFishSprites();

    /**
     * Creates a new pellet.
     *
     * @param points The point value of this pellet.
     * @param sprite The sprite of this pellet.
     */
    public FishPellet(int points, Sprite sprite) {
        super(points, sprite);
    }

    /**
     * Compute effect duration based on the closes ghost distance, using a complying formula when he's less than
     * {@link #GHOST_DIST_THRESH} away.
     * @param player the pacman.
     * @return the duration effect in milliseconds.
     */
    private long getDuration(Player player){
        Unit nearestGhost = Navigation.findNearest(Ghost.class, player.getSquare());
        if (nearestGhost == null)
            return BASIC_DURATION;
        List<Direction> path = Navigation.shortestPath(nearestGhost.getSquare(), player.getSquare(), nearestGhost);
        if (path == null)
            return BASIC_DURATION;
        int ghostDist = path.size();
        if (ghostDist > GHOST_DIST_THRESH)
            return INCREASED_DURATION;
        // A ghost is close, let's evaluate a approximately fair duration (ghostDist <= THRESH), the closer the ghost is
        // the shorter the duration should be
        long durationDiff = INCREASED_DURATION - BASIC_DURATION;
        return BASIC_DURATION + (durationDiff / (1 + (GHOST_DIST_THRESH - ghostDist)));
    }

    @Override
    public void onEat(Level level, Player player){
        super.onEat(level, player);
        long duration = this.getDuration(player);
        player.setMovable(false);
        setNewStatePlayer(player, Player.SpecialStates.ON_FISH, pacmanSprites);
        scheduleEffectDuration(new StopFishEffect(player), duration);
    }

    private class StopFishEffect extends StopEffect{

        /**
         * @param player the Pacman on which the effect has been activated.
         */
        public StopFishEffect(Player player) {
            super(player);
        }

        /**
         * Reset the player special state (to {@link Player.SpecialStates#NONE}) and Pacman's sprites he
         * used at instantiation. The reset operated by superclass does not enable
         * resetting {@link Unit#isMovable()} so it is overloaded here.
         */
        @Override
        public void run() {
            super.run();
            player.setMovable(true);
        }
    }

}
