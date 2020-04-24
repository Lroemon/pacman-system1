package nl.tudelft.jpacman;

import static org.assertj.core.api.Assertions.assertThat;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.game.Game;
import nl.tudelft.jpacman.level.Player;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Smoke test launching the full game,
 * and attempting to make a number of typical moves.
 * <p>
 * This is <strong>not</strong> a <em>unit</em> test -- it is an end-to-end test
 * trying to execute a large portion of the system's behavior directly from the
 * user interface. It uses the actual sprites and monster AI, and hence
 * has little control over what is happening in the game.
 * <p>
 * Because it is an end-to-end test, it is somewhat longer
 * and has more assert statements than what would be good
 * for a small and focused <em>unit</em> test.
 *
 * @author Arie van Deursen, March 2014.
 */
public class LauncherSmokeTest {

    private Launcher launcher;

    /**
     * Static Magic Numbers configuration test one
     */
    private static final int STEP1_SCORE = 10;
    private static final int STEP2_SCORE = 10;
    private static final int STEP3_SCORE = 60;
    private static final int STEP4_SCORE = 120;
    private static final int STEP5_SCORE = 120;

    private static final int STEP1_MOVE_NUM = 1;
    private static final int STEP2_MOVE_NUM = 1;
    private static final int STEP3_MOVE_NUM = 7;
    private static final int STEP4_MOVE_NUM = 6;
    private static final int STEP5_MOVE_NUM = 2;
    private static final int STEP6_MOVE_NUM = 2;
    private static final int KILLING_MOVE_NUM = 10;

    private static final int PLAYER_INDEX = 0;

    private static final long THREAD_SLEEP_TIME = 500L;

    /**
     * Static Magic Numbers configuration test two
     */
    private static final int STEP1_SCORE2 = 180;

    private static final Pair<Direction, Integer>[] STEP1_MOVES2 = new Pair[]{
        new Pair<Direction, Integer>(Direction.EAST, 6),
        new Pair<Direction, Integer>(Direction.SOUTH, 2),
        new Pair<Direction, Integer>(Direction.EAST, 2),
        new Pair<Direction, Integer>(Direction.NORTH, 2),
        new Pair<Direction, Integer>(Direction.EAST, 2),
    };

    private static final Pair<Direction, Integer>[] STEP2_MOVES2 = new Pair[]{
        new Pair<Direction, Integer>(Direction.NORTH, 2),
        new Pair<Direction, Integer>(Direction.WEST, 5)
    };

    /**
     * Launch the user interface.
     */
    @BeforeEach

    void setUpPacman() {
        launcher = new Launcher();
        launcher.launch();
    }

    /**
     * Quit the user interface when we're done.
     */
    @AfterEach
    void tearDown() {
        launcher.dispose();
    }

    /**
     * Launch the game, and imitate what would happen in a typical game.
     * The test is only a smoke test, and not a focused small test.
     * Therefore it is OK that the method is a bit too long.
     *
     * @throws InterruptedException Since we're sleeping in this test.
     */
    @SuppressWarnings({"magicnumber", "methodlength", "PMD.JUnitTestContainsTooManyAsserts"})
    @Test
    void smokeTest() throws InterruptedException {
        Game game = launcher.getGame();
        Player player = game.getPlayers().get(PLAYER_INDEX);

        // start cleanly.
        assertThat(game.isInProgress()).isFalse();
        game.start();
        assertThat(game.isInProgress()).isTrue();
        assertThat(player.getScore()).isZero();

        // get points
        move(game, Direction.EAST, STEP1_MOVE_NUM);
        assertThat(player.getScore()).isEqualTo(STEP1_SCORE);

        // now moving back does not change the score
        move(game, Direction.WEST, STEP2_MOVE_NUM);
        assertThat(player.getScore()).isEqualTo(STEP2_SCORE);

        // try to move as far as we can
        move(game, Direction.EAST, STEP3_MOVE_NUM);
        assertThat(player.getScore()).isEqualTo(STEP3_SCORE);

        // move towards the monsters
        move(game, Direction.NORTH, STEP4_MOVE_NUM);
        assertThat(player.getScore()).isEqualTo(STEP4_SCORE);

        // no more points to earn here.
        move(game, Direction.WEST, STEP5_MOVE_NUM);
        assertThat(player.getScore()).isEqualTo(STEP5_SCORE);

        move(game, Direction.NORTH, STEP6_MOVE_NUM);

        // Sleeping in tests is generally a bad idea.
        // Here we do it just to let the monsters move.
        Thread.sleep(THREAD_SLEEP_TIME);

        // we're close to monsters, this will get us killed.
        move(game, Direction.WEST, KILLING_MOVE_NUM);
        move(game, Direction.EAST, KILLING_MOVE_NUM);
        assertThat(player.isAlive()).isFalse();

        game.stop();
        assertThat(game.isInProgress()).isFalse();
    }

    /**
     * Test to check fruits and power pellet.
     */
    @Test
    void smokeTest2() throws InterruptedException {
        Game game = launcher.getGame();
        Player player = game.getPlayers().get(PLAYER_INDEX);

        assertThat(game.isInProgress()).isFalse();
        game.start();
        assertThat(game.isInProgress()).isTrue();
        assertThat(player.getScore()).isZero();

        move(game, STEP1_MOVES2);
        assertThat(player.getScore()).isEqualTo(STEP1_SCORE2);

        assertThat(game.getLevel().areGhostsScared());

        Thread.sleep(7000);

        assertThat(!game.getLevel().areGhostsScared());

        move(game, STEP2_MOVES2);
        assertThat(player.getLifeLeft()).isEqualTo(2);

        game.stop();
        assertThat(game.isInProgress()).isFalse();
    }

    /**
     * Make number of moves in given direction.
     *
     * @param game     The game we're playing
     * @param dir      The direction to be taken
     * @param numSteps The number of steps to take
     */
    public static void move(Game game, Direction dir, int numSteps) {
        Player player = game.getPlayers().get(PLAYER_INDEX);
        for (int i = 0; i < numSteps; i++) {
            game.move(player, dir);
        }
    }

    /**
     *
     * @param game The game we are playing.
     * @param movements The pair movement, direction and steps.
     */
    public static void move(Game game, Pair<Direction, Integer>[] movements){
        for(Pair<Direction, Integer> movement : movements){
            move(game, movement.t, movement.p);
        }
    }

    private static class Pair<T, P>{
        public final T t;
        public final P p;

        public Pair(T t, P p){
            this.t = t;
            this.p = p;
        }
    }
}
