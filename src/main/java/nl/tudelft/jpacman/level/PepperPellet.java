package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;

import java.util.Map;

/**
 * A Pepper pellet ({@link SpecialPellet}) is a positive bonus that increases pacman's speed when eaten, with variation
 * considering the current state of the level.
 * It is based on score, a player that already scored should deserve a better speed bonus to reach still unexplored
 * labyrinth areas !
 */
public class PepperPellet extends SpecialPellet {

    public static final float BASIC_SPEED_FACTOR = 1.8f;
    public static final float INCREASED_SPEED_FACTOR = 2.5f;

    public static final int SCORE_THRESH_DURATION = 1500;
    public static final long BASIC_DURATION = 3000L;
    public static final long INCREASED_DURATION = 5000L;

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

    @Override
    public void onEat(Level level, Player player){
        super.onEat(level, player);
        long duration = player.getScore() > SCORE_THRESH_DURATION ? INCREASED_DURATION : BASIC_DURATION;
        if (level.areGhostsScared())
            player.setSpeedModifier(INCREASED_SPEED_FACTOR);
        else
            player.setSpeedModifier(BASIC_SPEED_FACTOR);
        setNewStatePlayer(player, Player.SpecialStates.ON_PEPPER, pacmanSprites);
        scheduleEffectDuration(player, duration);
    }

}
