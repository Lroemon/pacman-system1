package nl.tudelft.jpacman.level.specialbox;

import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A special box is persistent in the square it is placed on and can apply on any unity (various effects).
 *
 * @author Rémy Decocq
 */
public class SpecialBox extends Unit {

    private final Sprite image;

    /**
     * A special box not eatable and that may apply to any unit
     * @param sprite the sprite corresponding to the special box
     */
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
