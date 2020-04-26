package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;

import java.util.Map;

/**
 * A Tomato pellet ({@link SpecialPellet}) is a positive bonus that make Pacman invisible to ghosts when eaten,
 * with variation considering the current state of the level.
 * It is based on remaining lives, a Pacman that only has 1 life should be safe longer !
 */
public class TomatoPellet extends SpecialPellet {

    public static final int LIFE_THRESH_DURATION = 1;
    public static final long BASIC_DURATION = 3000L;
    public static final long INCREASED_DURATION = 5000L;

    private static final Map<Direction, Sprite> pacmanSprites = new PacManSprites().getPacmanTomatoSprites();


    /**
     * Creates a new pellet.
     *
     * @param points The point value of this pellet.
     * @param sprite The sprite of this pellet.
     */
    public TomatoPellet(int points, Sprite sprite) {
        super(points, sprite);
    }

    @Override
    public void onEat(Level level, Player player){
        super.onEat(level, player);
        long duration = player.getLifeLeft() > LIFE_THRESH_DURATION ? BASIC_DURATION : INCREASED_DURATION;
        setNewStatePlayer(player, Player.SpecialStates.ON_TOMATO, pacmanSprites); // collisions take this state into account
        scheduleEffectDuration(player, duration);
    }

}
