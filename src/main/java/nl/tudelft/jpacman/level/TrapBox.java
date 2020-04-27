package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Unit;
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

    public static final long BASE_WAITING_TIME = 2000L;

    /**
     * The timer handling trap effect duration.
     */
    private final Timer timer;

    public TrapBox(Sprite sprite) {
        super(sprite);
        this.timer = new Timer();
    }

    @Override
    public boolean onTake(Level level, Unit unit){
        unit.setMovable(false);
        this.timer.schedule(new StopTrapEffect(unit), BASE_WAITING_TIME);
        return true;
    }

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
