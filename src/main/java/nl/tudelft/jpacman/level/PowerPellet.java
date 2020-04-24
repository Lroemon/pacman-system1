package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.sprite.Sprite;

public class PowerPellet extends Pellet {

    /**
     * Creates a new pellet.
     *
     * @param points The point value of this pellet.
     * @param sprite The sprite of this pellet.
     */
    public PowerPellet(int points, Sprite sprite) {
        super(points, sprite);
    }

    @Override
    public void onEat(Level level, Player player){
        super.onEat(level, player);
        level.scareGhosts();
    }
}
