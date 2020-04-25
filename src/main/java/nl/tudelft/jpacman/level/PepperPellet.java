package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.sprite.Sprite;

import java.util.Timer;
import java.util.TimerTask;

public class PepperPellet extends Pellet {

    public static final float BASIC_SPEED_FACTOR = 1.8f;
    public static final float INCREASED_SPEED_FACTOR = 2.5f;

    public static final int SCORE_THRESH_DURATION = 1500;
    public static final long BASIC_DURATION = 3000L;
    public static final long INCREASED_DURATION = 5000L;

    private Timer timer;

    /**
     * Creates a new pellet.
     *
     * @param points The point value of this pellet.
     * @param sprite The sprite of this pellet.
     */
    public PepperPellet(int points, Sprite sprite) {
        super(points, sprite);
        this.timer = new Timer();
    }

    @Override
    public void onEat(Level level, Player player){
        super.onEat(level, player);
        if (level.areGhostsScared())
            player.setSpeedModifier(INCREASED_SPEED_FACTOR);
        else
            player.setSpeedModifier(BASIC_SPEED_FACTOR);
        long duration = player.getScore() > SCORE_THRESH_DURATION ? INCREASED_DURATION : BASIC_DURATION;
        this.timer.schedule(new StopPepperEffect(player), duration);
    }

    private class StopPepperEffect extends TimerTask {

        private final Player player;

        public StopPepperEffect(Player player){
            super();
            this.player = player;
        }

        @Override
        public void run() {
            player.resetSpeedModifier();
        }
    }
}
