package nl.tudelft.jpacman.board;

import com.google.common.collect.Lists;
import nl.tudelft.jpacman.level.*;
import nl.tudelft.jpacman.level.specialbox.BridgeBox;
import nl.tudelft.jpacman.level.specialbox.TeleporterBox;
import nl.tudelft.jpacman.level.specialbox.TrapBox;
import nl.tudelft.jpacman.npc.Ghost;
import nl.tudelft.jpacman.npc.ghost.GhostColor;
import nl.tudelft.jpacman.npc.ghost.Pinky;
import nl.tudelft.jpacman.sprite.AnimatedSprite;
import nl.tudelft.jpacman.sprite.PacManSprites;
import nl.tudelft.jpacman.sprite.Sprite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Test for each implemented special box and their effect on units
 *
 * @author Rémy Decocq
 */
public class SpecialBoxTest {

    private final PacManSprites sprites = new PacManSprites();
    private final Board board = mock(Board.class);
    private final CollisionMap collisions = new DefaultPlayerInteractionMap();

    private BasicSquare square;
    private Ghost ghost;
    private Player player;
    private Level dumbLevel;

    /**
     * Player is protected
     */
    private class DfltPlayer extends Player {
        public DfltPlayer(Map<Direction, Sprite> spriteMap, AnimatedSprite deathAnimation){
            super(spriteMap, deathAnimation);
        }
    }

    @BeforeEach
    void setUp(){
        square = new BasicSquare();
        ghost = new Pinky(sprites.getGhostSprite(GhostColor.PINK));
        dumbLevel = new Level(board, Lists.newArrayList(ghost), Lists.newArrayList(), collisions);
        player = new DfltPlayer(sprites.getPacmanSprites(), sprites.getPacManDeathAnimation());
    }

    @Test
    void trapSpecialBox() throws InterruptedException {
        TrapBox trap = new TrapBox(sprites.getTrapBoxSprite());
        assertThat(player.isMovable()).isTrue();
        trap.onTake(dumbLevel, player);
        assertThat(player.isMovable()).isFalse();
        TimeUnit.MILLISECONDS.sleep(TrapBox.BASE_WAITING_TIME + 100L);
        assertThat(player.isMovable()).isTrue();
    }

    @Test
    void teleporterSpecialBox(){
        TeleporterBox tp1, tp2;
        tp1 = new TeleporterBox(sprites.getTeleporterBox(false));
        tp2 = new TeleporterBox(tp1, sprites.getTeleporterBox(true));
        BasicSquare oSquare = new BasicSquare();
        tp1.occupy(square);
        tp2.occupy(oSquare);
        player.occupy(square);

        assertThat(player.getDirection()).isEqualTo(Direction.EAST);
        assertThat(player.getSquare()).isEqualTo(tp1.getSquare());
        // Take tp1 that teleports to tp2
        assertThat(tp1.onTake(dumbLevel, player)).isTrue();
        assertThat(player.getDirection()).isEqualTo(Direction.EAST);
        assertThat(player.getSquare()).isEqualTo(tp2.getSquare());
        // Take back tp2 that reteleports to tp1
        assertThat(tp2.onTake(dumbLevel, player)).isTrue();
        assertThat(player.getDirection()).isEqualTo(Direction.EAST);
        assertThat(player.getSquare()).isEqualTo(tp1.getSquare());
    }

    @Test
    void bridgeSpecialBoxGetUnitAlign(){
        Direction bDirV = Direction.NORTH;
        Direction bDirH = Direction.WEST;
        BridgeBox bridgeV = new BridgeBox(bDirV, sprites.getBridgeBox(bDirV));
        BridgeBox bridgeH = new BridgeBox(bDirH, sprites.getBridgeBox(bDirH));

        assertThat(player.getVerticalPosition()).isEqualTo(Unit.VerticalPos.DOWN);
        // Down road of bridge
        assertThat(bridgeV.getAlignForLevel(player)).isEqualTo(BridgeBox.Align.HORIZONTAL);
        assertThat(bridgeH.getAlignForLevel(player)).isEqualTo(BridgeBox.Align.VERTICAL);

        player.setVerticalPosition(Unit.VerticalPos.UP);
        // Up road of the bridge
        assertThat(bridgeV.getAlignForLevel(player)).isEqualTo(BridgeBox.Align.VERTICAL);
        assertThat(bridgeH.getAlignForLevel(player)).isEqualTo(BridgeBox.Align.HORIZONTAL);
    }

    @Test
    void bridgeSpecialBoxSetVerticalOnTaken(){
        Direction bDirV = Direction.NORTH;
        BridgeBox bridgeV = new BridgeBox(bDirV, sprites.getBridgeBox(bDirV));
        bridgeV.occupy(square);
        BasicSquare fromSquare = new BasicSquare();
        fromSquare.link(square, Direction.NORTH); // an unit from this square moving NORTH arrives on bridge
        player.occupy(fromSquare);

        assertThat(player.getVerticalPosition()).isEqualTo(Unit.VerticalPos.DOWN);
        dumbLevel.start();
        dumbLevel.move(player, Direction.NORTH); // collision handler player -> bridge changes vertical level

        assertThat(player.getVerticalPosition()).isEqualTo(Unit.VerticalPos.UP);
        // The unit can only take leaving directions associated with his level on the bridge
        assertThat(square.canLeaveByDirection(player, Direction.NORTH)).isTrue();
        assertThat(square.canLeaveByDirection(player, Direction.SOUTH)).isTrue();
        assertThat(square.canLeaveByDirection(player, Direction.EAST)).isFalse();
        assertThat(square.canLeaveByDirection(player, Direction.WEST)).isFalse();
    }

}
