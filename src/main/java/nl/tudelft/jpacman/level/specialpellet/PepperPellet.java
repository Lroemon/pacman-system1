package nl.tudelft.jpacman.level.specialpellet;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;

import java.util.Map;

/**
 * A Pepper pellet ({@link SpecialPellet}) is a positive bonus that increases pacman's speed when eaten, with variation
 * considering the current state of the level.
 * It is based on score, a player that already scored should deserve a better speed bonus to reach still unexplored
 * labyrinth areas !
 *
 * @author Rémy Decocq
 */
public class PepperPellet extends SpecialPellet {

    /**
     * A basic speed multiplier
     */
    public static final float BASIC_SPEED_FACTOR = 1.8f;
    /**
     * A strong speed multiplier
     */
    public static final float INCREASED_SPEED_FACTOR = 2.5f;

    /**
     * Threshold on score to vary effect
     */
    public static final int SCORE_THRESH_DURATION = 1500;

    /**
     * Basic effect time
     */
    public static final long BASIC_DURATION = 3000L;

    /**
     * Increased effect time
     */
    public static final long INCREASED_DURATION = 5000L;

    /**
     * The oriented sprites to modify Pacman skin during effect
     */
    private static final Map<Direction, Sprite> pacmanSprites = new PacManSprites().getPacmanPepperSprites();


    /**
     * Creates a new pellet.
     *
     * @param points The point value of this pellet.
     * @param sprite The sprite of this pellet.
     */
    public PepperPellet(int points, Sprite sprite) {
        super(points, sprite);
    }

    private long getDuration(Player player) {
        return player.getScore() > SCORE_THRESH_DURATION ? INCREASED_DURATION : BASIC_DURATION;
    }

    private void setPlayerSpeedModifier(Level level, Player player){
        if (level.areGhostsScared())
            player.setSpeedModifier(INCREASED_SPEED_FACTOR);
        else
            player.setSpeedModifier(BASIC_SPEED_FACTOR);
    }

    /**
     * Called when this pellet is eaten by a Player (pacman)
     * @param level the current level
     * @param player who eat this pellet
     */
    @Override
    public void onEat(Level level, Player player){
        super.onEat(level, player);
        long duration = this.getDuration(player);
        this.setPlayerSpeedModifier(level, player);
        setNewStatePlayer(player, Player.SpecialStates.ON_PEPPER, pacmanSprites);
        scheduleEffectDuration(player, duration); // the default reset at the end will reset speedModifier
    }

}
