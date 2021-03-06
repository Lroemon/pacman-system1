package nl.tudelft.jpacman.level;

import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.level.specialbox.SpecialBox;
import nl.tudelft.jpacman.npc.Ghost;

/**
 * An extensible default interaction map for collisions caused by the player.
 *
 * The implementation makes use of the interactionmap, and as such can be easily
 * and declaratively extended when new types of units (ghosts, players, ...) are
 * added.
 *
 * @author Arie van Deursen
 * @author Jeroen Roosen
 *
 */
public class DefaultPlayerInteractionMap implements CollisionMap {

    private final CollisionMap collisions = defaultCollisions();

    private Level level;

    public void setLevel(Level level){
        this.level = level;
    }

    @Override
    public void collide(Unit mover, Unit movedInto) {
        collisions.collide(mover, movedInto);
    }

    /**
     * Creates the default collisions Player-Ghost and Player-Pellet.
     *
     * @return The collision map containing collisions for Player-Ghost and
     *         Player-Pellet.
     */
    private CollisionInteractionMap defaultCollisions() {
        CollisionInteractionMap collisionMap = new CollisionInteractionMap();

        collisionMap.onCollision(Player.class, Ghost.class,
            (player, ghost) -> {
                if(ghost.isScared()){
                    player.addPoints(player.killGhost(ghost));
                }else{
                    boolean killPlayer = true;
                    // Collisions related to special states (extension fruits)
                    if (player.isOnSpecialState(Player.SpecialStates.ON_TOMATO))
                        killPlayer = false;
                    player.setAlive(!killPlayer);
                }
            });

        collisionMap.onCollision(Player.class, Pellet.class,
            (player, pellet) -> {
                pellet.onEat(level, player);
            });

        // Special Boxes are different units

        collisionMap.onCollision(Player.class, SpecialBox.class,
            (unit, box) -> {
                box.onTake(level, unit);
            });

        collisionMap.onCollision(Ghost.class, SpecialBox.class,
            (unit, box) -> {
                box.onTake(level, unit);
            });

        return collisionMap;
    }
}
