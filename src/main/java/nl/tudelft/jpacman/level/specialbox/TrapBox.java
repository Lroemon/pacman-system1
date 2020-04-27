package nl.tudelft.jpacman.level.specialbox;

import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.sprite.Sprite;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A Trap is a special box acting as a penalty when any unit walks on it.
 * It locks the unit on the square for a certain amount of time.
 *
 * @author Rémy Decocq
 */
public class TrapBox extends SpecialBox {

    /**
     * Fixed time to wait trapped
     */
    public static final long BASE_WAITING_TIME = 2000L;

    /**
     * The timer handling trap effect duration.
     */
    private final Timer timer;

    /**
     * A Trap locking units for a given time
     * @param sprite the sprite to display
     */
    public TrapBox(Sprite sprite) {
        super(sprite);
        this.timer = new Timer();
    }

    /**
     * Called when an unit walk on the box square.
     * @param level the current level
     * @param unit the unit that walked on the box
     * @return true iff an action was performed
     */
    @Override
    public boolean onTake(Level level, Unit unit){
        unit.setMovable(false);
        this.timer.schedule(new StopTrapEffect(unit), BASE_WAITING_TIME);
        return true;
    }

    /**
     * Task to disable trap effect on trapped unit
     */
    protected class StopTrapEffect extends TimerTask {

        protected final Unit unit;

        /**
         * @param unit the unit to lock
         */
        public StopTrapEffect(Unit unit){
            super();
            this.unit = unit;
        }

        /**
         * Reset the unit state, enabling its movements again.
         */
        @Override
        public void run() {
            unit.setMovable(true);
        }
    }

}
