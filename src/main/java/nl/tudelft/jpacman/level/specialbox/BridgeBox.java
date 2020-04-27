package nl.tudelft.jpacman.level.specialbox;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 *
 * A Bridge is a special box that brings a notion of vertical levels or "layers" for collisions. It can be seen as
 * 2 crossed roads "alignments" NORTH-SOUTH and WEST-EAST (direction doesn't matter). These roads are bots located
 * on a single {@link nl.tudelft.jpacman.board.Square} but collisions on each are independents. This element introduces
 * a new binary unit state {@link Unit.VerticalPos}, used to handle collisions on a same vertical level (see
 * {@link Level#move(Unit, Direction)}). One of the two road is the highest, depending on the bridge unity orientation.
 * An horizontally oriented bridge has its upper road as WEST-EAST.
 *
 * @author Rémy Decocq
 */
public class BridgeBox extends SpecialBox {

    /**
     * An oriented bridge with an up and a down way depending its direction.
     * @param direction indicates the direction of the up-road (so NORTH would semantically leads to same bridge than
     *                  SOUTH).
     * @param sprite the sprite to use for a bridge oriented this way
     */
    public BridgeBox(Direction direction, Sprite sprite) {
        super(sprite);
        super.setDirection(direction);
    }

    /**
     * Change vertical level of an unit coming into the bridge square, depending its entering direction and the bridge
     * orientation
     *
     * @param unit the unit coming in the bridge
     */
    private void changeLevelUsingComingDir(Unit unit) {
        Direction uDir = unit.getDirection();
        Direction bDir = super.getDirection();
        if (Direction.isHorizontalAlign(bDir)) {
            if (Direction.isHorizontalAlign(uDir))
                unit.setVerticalPosition(VerticalPos.UP);
            else
                unit.setVerticalPosition(VerticalPos.DOWN);
        } else {
            if (Direction.isHorizontalAlign(uDir))
                unit.setVerticalPosition(VerticalPos.DOWN);
            else
                unit.setVerticalPosition(VerticalPos.UP);
        }
    }

    @Override
    public boolean onTake(Level level, Unit unit) {
        this.changeLevelUsingComingDir(unit);
        return true;
    }

    /**
     * Get which bridge road the unit is in considering its vertical level
     * @param u the unit
     * @return the alignement of the road
     */
    public Align getAlignForLevel(Unit u){
        if (Direction.isVerticalAlign(getDirection())){
            if (u.getVerticalPosition() == VerticalPos.DOWN)
                return Align.HORIZONTAL;
            else
                return Align.VERTICAL;
        } else {
            if (u.getVerticalPosition() == VerticalPos.DOWN)
                return Align.VERTICAL;
            else
                return Align.HORIZONTAL;
        }
    }

    /**
     * Stands for one of the two possible ways to cross a bridge, see {@link BridgeBox}.
     */
    public enum Align {
        HORIZONTAL, VERTICAL;
    }

}
