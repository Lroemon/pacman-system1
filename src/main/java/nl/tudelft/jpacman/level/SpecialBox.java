package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.sprite.Sprite;

public class SpecialBox extends Unit {

    private final Sprite image;

    public SpecialBox(Sprite sprite) {
        super.setMovable(false);
        this.image = sprite;
    }

    @Override
    public Sprite getSprite() {
        return this.image;
    }

    /**
     * Called when an unit walk on the box square.
     * @param level the current level
     * @param unit the unit that walked on the box
     * @return true iff an action was performed
     */
    public boolean onTake(Level level, Unit unit){
        return true;
    }
}
