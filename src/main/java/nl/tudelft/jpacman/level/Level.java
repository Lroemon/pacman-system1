package nl.tudelft.jpacman.level;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.npc.Ghost;

/**
 * A level of Pac-Man. A level consists of the board with the players and the
 * AIs on it.
 *
 * @author Jeroen Roosen 
 */
@SuppressWarnings("PMD.TooManyMethods")
public class Level {

    private static final int NPC_STARTER_INTERVAL_DIVIDER = 2;

    private static final int FIRST_STEP_SCARING_TIME = 7;
    private static final int SECOND_STEP_SCARING_TIME = 5;

    private static final long SPECIAL_SPAWNING_INTERVAL = 7000L;

    /**
     * The board of this level.
     */
    private final Board board;

    /**
     * The lock that ensures moves are executed sequential.
     */
    private final Object moveLock = new Object();

    /**
     * The lock that ensures starting and stopping can't interfere with each
     * other.
     */
    private final Object startStopLock = new Object();

    /**
     * The NPCs of this level and, if they are running, their schedules.
     */
    private final Map<Ghost, ScheduledExecutorService> npcs;

    /**
     * <code>true</code> iff this level is currently in progress, i.e. players
     * and NPCs can move.
     */
    private boolean inProgress;

    /**
     * The squares from which players can start this game.
     */
    private final List<Square> startSquares;

    /**
     * The start current selected starting square.
     */
    private int startSquareIndex;

    /**
     * The players on this level.
     */
    private final List<Player> players;

    /**
     * The table of possible collisions between units.
     */
    private final CollisionMap collisions;

    /**
     * The objects observing this level.
     */
    private final Set<LevelObserver> observers;

    /**
     * The level of scaring 0 - 3. 5 seconds for 0-1 and 7 seconds for 2-3.
     */
    private int scaringLevel;

    /**
     * The time left that ghosts are afraid.
     */
    private int scaringTimeLeft;

    /**
     * The timer which manages the time left for the scaring level.
     */
    private Timer scaringTimer;

    /**
     * The spawner object to spawn dynamically special pellets and boxes in the board
     */
    private SpecialUnitySpawner spawner;
    private ScheduledExecutorService spawnService;

    /**
     * Creates a new level for the board.
     *
     * @param board
     *            The board for the level.
     * @param ghosts
     *            The ghosts on the board.
     * @param startPositions
     *            The squares on which players start on this board.
     * @param collisionMap
     *            The collection of collisions that should be handled.
     */
    public Level(Board board, List<Ghost> ghosts, List<Square> startPositions,
                 CollisionMap collisionMap) {
        assert board != null;
        assert ghosts != null;
        assert startPositions != null;

        this.board = board;
        this.inProgress = false;
        this.npcs = new HashMap<>();
        for (Ghost ghost : ghosts) {
            npcs.put(ghost, null);
        }
        this.startSquares = startPositions;
        this.startSquareIndex = 0;
        this.players = new ArrayList<>();
        this.collisions = collisionMap;
        this.observers = new HashSet<>();

        this.scaringTimer = new Timer();
    }

    /**
     * Adds an observer that will be notified when the level is won or lost.
     *
     * @param observer
     *            The observer that will be notified.
     */
    public void addObserver(LevelObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an observer if it was listed.
     *
     * @param observer
     *            The observer to be removed.
     */
    public void removeObserver(LevelObserver observer) {
        observers.remove(observer);
    }

    /**
     * Registers a player on this level, assigning him to a starting position. A
     * player can only be registered once, registering a player again will have
     * no effect.
     *
     * @param player
     *            The player to register.
     */
    public void registerPlayer(Player player) {
        assert player != null;
        assert !startSquares.isEmpty();

        if (players.contains(player)) {
            return;
        }
        players.add(player);
        Square square = startSquares.get(startSquareIndex);
        player.occupy(square);
        player.setSpawnSquare(square);
        startSquareIndex++;
        startSquareIndex %= startSquares.size();
    }

    /**
     * Set a spawner for this Level instance, this method is already called by
     * {@link SpecialUnitySpawner#setLevel(Level)} when linking it with this level.
     * @param spawner the spawner to use for probabilistic spawning special units in this level
     */
    public void setSpawner(SpecialUnitySpawner spawner) {
        this.spawner = spawner;
    }

    /**
     * Returns the board of this level.
     *
     * @return The board of this level.
     */
    public Board getBoard() {
        return board;
    }

    public Player getOnePlayer(){
        if (this.players.size() > 0)
            return this.players.get(0);
        return null;
    }

    /**
     * Moves the unit into the given direction if possible and handles all
     * collisions.
     *
     * @param unit
     *            The unit to move.
     * @param direction
     *            The direction to move the unit in.
     */
    public void move(Unit unit, Direction direction) {
        assert unit != null;
        assert direction != null;
        assert unit.hasSquare();

        // added movable notion for extension
        if (!isInProgress() || !unit.isMovable()) {
            return;
        }

        synchronized (moveLock) {
            unit.setDirection(direction);
            Square location = unit.getSquare();
            if (!location.canLeaveByDirection(unit, direction))
                return;

            Square destination = location.getSquareAt(direction);

            if (destination.isAccessibleTo(unit)) {
                // Special boxes first to handle arriving on a bridge that changes vertical level
                List<Unit> occupants = destination.getOrderedOccupants();
                unit.occupy(destination);
                // Units that could set vertical pos for collision treatment will be treated first to UP unit if needed
                unit.setVerticalPosition(Unit.VerticalPos.DOWN);
                for (Unit occupant : occupants) {
                    if (unit.getVerticalPosition() == occupant.getVerticalPosition())
                        collisions.collide(unit, occupant);
                }
            }
            updateObservers();
        }
    }

    /**
     * Starts or resumes this level, allowing movement and (re)starting the
     * NPCs.
     */
    public void start() {
        synchronized (startStopLock) {
            if (isInProgress()) {
                return;
            }
            startNPCs();
            startSpawner();
            inProgress = true;
            updateObservers();
        }
    }

    /**
     * Stops or pauses this level, no longer allowing any movement on the board
     * and stopping all NPCs.
     */
    public void stop() {
        synchronized (startStopLock) {
            if (!isInProgress()) {
                return;
            }
            stopNPCs();
            stopSpawner();
            inProgress = false;
        }
    }

    /**
     * Starts all NPC movement scheduling.
     */
    private void startNPCs() {
        for (final Ghost npc : npcs.keySet()) {
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

            service.schedule(new NpcMoveTask(service, npc),
                npc.getInterval() / NPC_STARTER_INTERVAL_DIVIDER, TimeUnit.MILLISECONDS);

            npcs.put(npc, service);
        }
    }

    /**
     * Stops all NPC movement scheduling and interrupts any movements being
     * executed.
     */
    private void stopNPCs() {
        for (Entry<Ghost, ScheduledExecutorService> entry : npcs.entrySet()) {
            ScheduledExecutorService schedule = entry.getValue();
            assert schedule != null;
            schedule.shutdownNow();
        }
    }

    /**
     * Start calling to spawner every {@link #SPECIAL_SPAWNING_INTERVAL}
     */
    private void startSpawner(){
        if (this.spawner != null) {
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
            this.spawnService = service;
            service.schedule(new DynamicSpawnTask(service, this.spawner),
                SPECIAL_SPAWNING_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Stop repeated calling to spawner
     */
    private void stopSpawner(){
        if (this.spawnService != null)
            this.spawnService.shutdownNow();
    }

    /**
     * Returns whether this level is in progress, i.e. whether moves can be made
     * on the board.
     *
     * @return <code>true</code> iff this level is in progress.
     */
    public boolean isInProgress() {
        return inProgress;
    }

    /**
     * Updates the observers about the state of this level.
     */
    private void updateObservers() {
        if (!isAnyPlayerAlive()) {
            for (LevelObserver observer : observers) {
                observer.levelLost();
            }
        }
        if (remainingPellets() == 0) {
            for (LevelObserver observer : observers) {
                observer.levelWon();
            }
        }
    }

    /**
     * Returns <code>true</code> iff at least one of the players in this level
     * is alive.
     *
     * @return <code>true</code> if at least one of the registered players is
     *         alive.
     */
    public boolean isAnyPlayerAlive() {
        for (Player player : players) {
            if (player.isAlive()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Counts the pellets remaining on the board.
     *
     * @return The amount of pellets remaining on the board.
     */
    public int remainingPellets() {
        Board board = getBoard();
        int pellets = 0;
        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                for (Unit unit : board.squareAt(x, y).getOccupants()) {
                    if (unit instanceof Pellet) {
                        pellets++;
                    }
                }
            }
        }
        assert pellets >= 0;
        return pellets;
    }

    public ArrayList<Ghost> getGhosts(){
        return new ArrayList<>(this.npcs.keySet());
    }

    /**
     *
     * @return true if at least of ghost is scared.
     */
    public boolean areGhostsScared(){
        return this.scaringTimeLeft > 0;
    }

    /**
     * Make all ghost of the level scared.
     */
    public void scareGhosts(){
        for(Ghost ghost : this.npcs.keySet()){
            ghost.setScared(true);
        }
        this.setScaringTimeLeft();
        this.setScaringTimer();
        this.scaringLevel++;
    }

    /**
     * Make all ghost unscared.
     */
    public void unscareGhosts(){
        for(Ghost ghost : this.npcs.keySet()){
            ghost.setScared(false);
        }
        for(Player player : this.players){
            player.resetPredatorMod();
        }
    }

    /**
     * Compute and set the time that Ghost are afraid.
     */
    private void setScaringTimeLeft(){
        if(this.scaringLevel < 2){
            this.scaringTimeLeft = FIRST_STEP_SCARING_TIME;
        }else if(this.scaringLevel < 4){
            if(this.scaringTimeLeft < SECOND_STEP_SCARING_TIME){
                this.scaringTimeLeft = SECOND_STEP_SCARING_TIME;
            }
        }else{
            this.scaringTimeLeft = 0;
        }
    }

    /**
     * Check if the timer should start or no.
     */
    private void setScaringTimer(){
        this.scaringTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(scaringTimeLeft > 0){
                    scaringTimeLeft--;
                    setScaringTimer();
                }else{
                    unscareGhosts();
                }
            }
        }, 1000L);
    }

    public float getScaringTimeLeft(){
        return this.scaringTimeLeft;
    }

    /**
     * A task that moves an NPC and reschedules itself after it finished.
     *
     * @author Jeroen Roosen
     */
    private final class NpcMoveTask implements Runnable {

        /**
         * The service executing the task.
         */
        private final ScheduledExecutorService service;

        /**
         * The NPC to move.
         */
        private final Ghost npc;

        /**
         * Creates a new task.
         *
         * @param service
         *            The service that executes the task.
         * @param npc
         *            The NPC to move.
         */
        NpcMoveTask(ScheduledExecutorService service, Ghost npc) {
            this.service = service;
            this.npc = npc;
        }

        @Override
        public void run() {
            Direction nextMove = npc.nextMove();
            if (nextMove != null) {
                move(npc, nextMove);
            }
            long interval = npc.getInterval();
            service.schedule(this, interval, TimeUnit.MILLISECONDS);
        }
    }


    /**
     * A task to make calls to the spawner, that spawns some special units in the board level with probability
     */
    private final class DynamicSpawnTask implements Runnable {

        private final ScheduledExecutorService service;
        private final SpecialUnitySpawner spawner;

        /**
         * Create a new task calling repeatedly at {@link #SPECIAL_SPAWNING_INTERVAL} interval
         * @param service the service
         * @param spawner the spawner
         */
        DynamicSpawnTask(ScheduledExecutorService service, SpecialUnitySpawner spawner){
            this.service = service;
            this.spawner = spawner;
        }

        @Override
        public void run() {
            spawner.trySpawnSpecial();
            service.schedule(this, SPECIAL_SPAWNING_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * An observer that will be notified when the level is won or lost.
     *
     * @author Jeroen Roosen
     */
    public interface LevelObserver {

        /**
         * The level has been won. Typically the level should be stopped when
         * this event is received.
         */
        void levelWon();

        /**
         * The level has been lost. Typically the level should be stopped when
         * this event is received.
         */
        void levelLost();
    }
}
