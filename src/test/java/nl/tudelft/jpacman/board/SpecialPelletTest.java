package nl.tudelft.jpacman.board;

import com.google.common.collect.Lists;
import nl.tudelft.jpacman.level.*;
import nl.tudelft.jpacman.npc.Ghost;
import nl.tudelft.jpacman.npc.ghost.GhostColor;
import nl.tudelft.jpacman.npc.ghost.Pinky;
import nl.tudelft.jpacman.sprite.AnimatedSprite;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SpecialPelletTest {

    private final int SCORE = 10;
    private final long EFFECT_DUR_TESTS = 200L;
    private final PacManSprites sprites = new PacManSprites();
    private final Board board = mock(Board.class);
    private final CollisionMap collisions = mock(CollisionMap.class);

    private BasicSquare square;
    private Ghost ghost;
    private Player player;
    private Sprite pelletSprite;
    private Level dumbLevel;

    /**
     * Player is protected
     */
    private class DfltPlayer extends Player {
        public DfltPlayer(Map<Direction, Sprite> spriteMap, AnimatedSprite deathAnimation){
            super(spriteMap, deathAnimation);
        }
    }

    /**
     * Stop effect after duration facilities are protected
     */
    private class DumbSpecialPellet extends SpecialPellet {

        public DumbSpecialPellet() {
            super(SCORE, sprites.getPelletSprite());
        }

        public void scheduleEffectDuration(Player player){
            super.scheduleEffectDuration(player, EFFECT_DUR_TESTS);
        }

    }

    @BeforeEach
    void setUp(){
        square = new BasicSquare();
        ghost = new Pinky(sprites.getGhostSprite(GhostColor.PINK));
        dumbLevel = new Level(board, Lists.newArrayList(ghost), Lists.newArrayList(), collisions);
        player = new DfltPlayer(sprites.getPacmanSprites(), sprites.getPacManDeathAnimation());
        assertThat(player.getScore()).isEqualTo(0);
        assertThat(player.isOnSpecialState()).isFalse();
        assertThat(player.getSpecialState()).isEqualTo(Player.SpecialStates.NONE);
    }

    @Test
    void genericSpecialPellet(){
        pelletSprite = sprites.getPelletSprite();
        SpecialPellet sp = new SpecialPellet(SCORE, pelletSprite);
        sp.occupy(square);

        assertThat(sp.getSquare()).isEqualTo(square);
        sp.onEat(dumbLevel, player);
        assertThat(sp.hasSquare()).isFalse();
        // onEat() doesn't change state since its from Pellet (should be overloaded in subclasses)
        assertThat(player.getSpecialState()).isEqualTo(Player.SpecialStates.NONE);
        assertThat(player.getScore()).isEqualTo(SCORE);
    }

    @Test
    void stopEffectAfterDurationReset() throws InterruptedException {
        Player.SpecialStates prevState = player.getSpecialState();
        Sprite prevSprites = player.getSprite();
        float previousSpeedModifier = player.getSpeedModifier();

        SpecialPellet.setNewStatePlayer(player, Player.SpecialStates.ON_TOMATO,
                                        sprites.getPacmanTomatoSprites());
        player.setSpeedModifier(previousSpeedModifier + 1);

        (new DumbSpecialPellet()).scheduleEffectDuration(player);
        TimeUnit.MILLISECONDS.sleep(EFFECT_DUR_TESTS * 2);

        // check if player has been reset after effect duration
        assertThat(prevState).isEqualTo(player.getSpecialState());
        assertThat(prevSprites).isEqualTo(player.getSprite());
        assertThat(previousSpeedModifier).isEqualTo(player.getSpeedModifier());
    }

    @Test
    void grenadeSpecialPellet(){
        pelletSprite = sprites.getGrenadePelletSprite();
        GrenadePellet sp = new GrenadePellet(SCORE, pelletSprite);
        sp.occupy(square);
        player.occupy(square);
        BasicSquare spawnGhost = new BasicSquare();
        ghost.setSpawnSquare(spawnGhost);
        ghost.occupy(square); // put the ghost in same case so explosion kill him

        assertThat(ghost.hasSquare()).isTrue();
        sp.onEat(dumbLevel, player);
        // Grenade is instant and has no duration effect and state linked
        assertThat(player.getSpecialState()).isEqualTo(Player.SpecialStates.NONE);
        assertThat(ghost.getSquare()).isEqualTo(spawnGhost);
    }

    @Test
    void pepperSpecialPellet(){
        pelletSprite = sprites.getPepperPelletSprite();
        PepperPellet sp = new PepperPellet(SCORE, pelletSprite);
        sp.occupy(square);
        player.occupy(square);

        float previousSpeedModif = player.getSpeedModifier();
        float previousSpeed = player.getSpeed();
        sp.onEat(dumbLevel, player);
        assertThat(player.getSpecialState()).isEqualTo(Player.SpecialStates.ON_PEPPER);
        assertThat(player.getSpeedModifier()).isGreaterThan(previousSpeedModif);
        assertThat(player.getSpeed()).isGreaterThan(previousSpeed);
    }

    @Test
    void tomatoSpecialPellet(){
        pelletSprite = sprites.getTomatoPelletSprite();
        TomatoPellet sp = new TomatoPellet(SCORE, pelletSprite);
        sp.occupy(square);
        player.occupy(square);

        sp.onEat(dumbLevel, player);
        assertThat(player.getSpecialState()).isEqualTo(Player.SpecialStates.ON_TOMATO);
    }

    @Test
    void potatoSpecialPellet(){
        pelletSprite = sprites.getPotatoPelletSprite();
        PotatoPellet sp = new PotatoPellet(SCORE, pelletSprite);
        sp.occupy(square);
        player.occupy(square);

        float previousSpeedModif = ghost.getSpeedMultiplier();
        sp.onEat(dumbLevel, player);
        assertThat(player.getSpecialState()).isEqualTo(Player.SpecialStates.ON_POTATO);
        assertThat(ghost.getSpeedMultiplier()).isGreaterThan(previousSpeedModif);
    }

    @Test
    void fishSpecialPellet(){
        pelletSprite = sprites.getPotatoPelletSprite();
        FishPellet sp = new FishPellet(SCORE, pelletSprite);
        sp.occupy(square);
        player.occupy(square);
        ghost.occupy(square); // needed to compute effect duration using closest ghost

        boolean wasPacmanMovable = player.isMovable();
        assertThat(wasPacmanMovable).isTrue();
        sp.onEat(dumbLevel, player);
        assertThat(player.getSpecialState()).isEqualTo(Player.SpecialStates.ON_FISH);
        assertThat(player.isMovable()).isFalse();
    }

}
