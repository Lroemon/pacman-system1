package nl.tudelft.jpacman.level.specialbox;

import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.level.LevelFactory;
import nl.tudelft.jpacman.npc.Ghost;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A Teleporter is a special box that works by pair. There must be another Teleporter linked with to allow Pacman
 * to teleport from each other (both directions). Ghosts cannot take it.
 *
 * @author Rémy Decocq
 */
public class TeleporterBox extends SpecialBox {

    /**
     * The other teleporter linked with, can be null (so, no effect)
     */
    private TeleporterBox linked;

    /**
     * A Teleporter should be linked with another and both have different sprites.
     * Handled in {@link LevelFactory#createTeleporterBox()}
     * @param linked the other Teleporter to link with
     * @param sprite the sprite of the teleporter
     */
    public TeleporterBox(TeleporterBox linked, Sprite sprite) {
        super(sprite);
        this.setLinked(linked);
    }

    /**
     * Instantiate a Teleporter not linked to another for now (so will have no effect).
     * @see TeleporterBox#TeleporterBox(TeleporterBox, Sprite)
     * @param sprite
     */
    public TeleporterBox(Sprite sprite){
        this(null, sprite);
    }

    /**
     * Operate the teleportation to the linked Teleporter.
     * @param unit the unit to teleport to the Square of linked Teleporter
     */
    private void teleportToLinked(Unit unit){
        unit.occupy(linked.getSquare());
    }

    /**
     * Process the teleportation if allowed and possible (if exists a linked Teleporter).
     * @param level the current level
     * @param unit the unit that walked on the teleporter, ghosts are ignored
     * @return true iff teleportation happened
     */
    @Override
    public boolean onTake(Level level, Unit unit) {
        if (unit instanceof Ghost || linked == null)
            return false;
        this.teleportToLinked(unit);
        return true;
    }

    /**
     * Set the current linked teleporter, and register itself to it to establish the pair
     * @param other the teleporter to link with
     */
    public void setLinked(TeleporterBox other){
        this.linked = other;
        if (other != null && !other.isLinked()) // register itself for the other but avoiding loops
            other.setLinked(this);
    }

    /**
     * @return Whether this teleporter is linked with another
     */
    public boolean isLinked(){
        return this.linked != null;
    }


}
