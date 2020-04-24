package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.sprite.Sprite;

public class FruitPellet extends Pellet {

    /**
     * List with fruitType
     */
    public enum FruitType{

        APPLE("/sprite/apple.png"),
        CHERRY("/sprite/cherry.png"),
        ORANGE("/sprite/orange.png"),
        STRAWBERRY("/sprite/strawberry.png"),
        MELON("/sprite/melon.png");

        public final String spritePath;

        FruitType(String spritePath){
            this.spritePath = spritePath;
        }

    }

    /**
     * Creates a new pellet.
     *
     * @param points The point value of this pellet.
     * @param sprite The sprite of this pellet.
     */
    public FruitPellet(int points, Sprite sprite) {
        super(points, sprite);
    }

    @Override
    public void onEat(Level level, Player player){
        super.onEat(level, player);
        player.addLife();
    }
}
