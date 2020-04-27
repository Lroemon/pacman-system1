package nl.tudelft.jpacman.level.specialpellet;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.level.Pellet;
import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.npc.Ghost;
import nl.tudelft.jpacman.npc.ghost.Navigation;
import nl.tudelft.jpacman.sprite.Sprite;

import java.util.ArrayList;
import java.util.List;

/**
 * A Grenade pellet ({@link SpecialPellet}) is an instant bonus that kill ghosts around when eaten, with variation
 * considering the current state of the level.
 * The range of the explosion depends on on the number of remaining lives for Pacman : the more unsafe he is, the more
 * powerful the explosion (ranges up to 4 squares). It takes into account walls, so this is the path length that is
 * considered.
 *
 * @author Rémy Decocq
 */
public class GrenadePellet extends Pellet {

    public static final int MAX_RANGE = 4;
    public static final int MIN_RANGE = 2;

    /**
     * Creates a new pellet.
     *
     * @param points The point value of this pellet.
     * @param sprite The sprite of this pellet.
     */
    public GrenadePellet(int points, Sprite sprite) {
        super(points, sprite);
    }

    /**
     * Retrieve all ghost from the level that are in the explosion range considering its power depending on
     * remaining lives.
     *
     * @param level the level to search into
     * @param player the player from which explosion comes from
     * @return a list of ghost in explosion range, so should be killed
     */
    private static ArrayList<Ghost> getGhostsInRange(Level level, Player player){
        ArrayList<Ghost> toKill = new ArrayList<>();
        int range = (MAX_RANGE + 1) - player.getLifeLeft(); // range increase as pacman has less remaining lives
        range = Math.max(range, MIN_RANGE);

        for(Ghost g: level.getGhosts()){
            List<Direction> path = Navigation.shortestPath(g.getSquare(), player.getSquare(), g);
            if (path != null && path.size() <= range)
                toKill.add(g);
        }
        return toKill;
    }

    /**
     * Make a player kill a list of ghost (earning score as it was eating them in a row).
     * @param toKill the list of ghosts to kill
     * @param player the player considered as ghosts killer
     */
    private static void killGhosts(ArrayList<Ghost> toKill, Player player){
        for (Ghost g: toKill){
            player.killGhost(g);
        }
    }

    @Override
    public void onEat(Level level, Player player){
        super.onEat(level, player);
        killGhosts(getGhostsInRange(level, player), player);
    }
}
