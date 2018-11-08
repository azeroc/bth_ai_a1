/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wumpusworld;

import java.util.Random;

/**
 * Wumpus World Environment for Q-table training
 * @author Azeroc
 */
public class WumpusEnv {
    // LEARNING ENVIRONMENT SETTINGS
    private static final boolean ENABLE_FULL_EXPLORATION = true; // Encourages to fill missing Q-values for actions at least once
    public static final int MAX_EP_LEN = 10000; // Max episode length (Max actions per episode)
    
    // CONSEQUENCE REWARDS
    public static final Double REW_WUMPUS = -200.0;   // Step into Wumpus
    public static final Double REW_KILL_WUMPUS = 20.0; // Kill Wumpus
    public static final Double REW_PIT = -20.0;      // Step into Pit
    public static final Double REW_TURN = -1.0;       // Turning
    public static final Double REW_CLIMB_OUT = 10.0;  // Climb out of pit
    public static final Double REW_GRAB_GOLD = 50.0; // Grab gold
    public static final Double REW_EXPLORE = 10.0; // Explore unexplored tiles
    public static final Double REW_BAD_RISK = -20.0; // Take bad risk, ignoring percepts
    public static final Double REW_MISS_ARROW = -10.0; // Miss the shot against Wumpus
    public static final Double REW_WASTE_TIME = -100.0; // Waste time by doing useless actions
    
    // Private members
    private final Random rand;
    
    public WumpusEnv() {
        rand = new Random();
    }    
    
    // === Private methods ===
    
    /**
     * Take action in World and observe reward from taking it
     * @param w World object
     * @param action QState action index
     * @return Reward of action consequence
     */
    private Double observeAction(World w, int action) {        
        String worldAction = QState.resolveToWorldAction(action);
        
        // Get pre-action state
        int oldX = w.getPlayerX();
        int oldY = w.getPlayerY();
        boolean oldPitFlag = w.isInPit();
        boolean oldArrowFlag = w.hasArrow();
        boolean oldInStench = w.hasStench(oldX, oldY);
        boolean isSafeExplored = w.isSafeExplored();
        World ow = w.cloneWorld();
        
        // Do action
        w.doAction(worldAction);
        
        // Get post-action state
        int newX = w.getPlayerX();
        int newY = w.getPlayerY();
        boolean newPitFlag = w.isInPit();
        boolean newInStench = w.hasStench(newX, newY);
        
        // Check if we got gold
        if (w.hasGold()) {
            return REW_GRAB_GOLD;
        }
        
        // Wasting time doing 'Grab' action
        if (worldAction.equals(World.A_GRAB)) {
            return REW_WASTE_TIME;
        }
        
        // Wasting time in pit
        if (oldPitFlag && newPitFlag) {
            return REW_WASTE_TIME;
        }
                
        // Player has climbed out
        if (oldPitFlag && !newPitFlag) {
            return REW_CLIMB_OUT;
        }
        
        // Wasting time doing 'Climb' action
        if (worldAction.equals(World.A_CLIMB)) {
            return REW_WASTE_TIME;
        }
        
        // Player stepped into Wumpus
        if (w.hasWumpus(newX, newY)) {
            return REW_WUMPUS;
        }
        
        // Player has fallen into pit
        if (!oldPitFlag && newPitFlag) {
            return REW_PIT;
        }
        
        // Player is wasting time shooting with no arrow left
        if (worldAction.equals(World.A_SHOOT) && !oldArrowFlag) {
            return REW_WASTE_TIME;
        }
        
        // Player wastes his arrow shot
        if (worldAction.equals(World.A_SHOOT)) {
            if (oldInStench && newInStench) { // Didn't kill Wumpus (in stench, but faced wrong way)
                return REW_MISS_ARROW;
            }
            
            if (oldInStench && !newInStench) { // Reward for killing wumpus
                return REW_KILL_WUMPUS;
            }
            
            if (!oldInStench) { // Didn't kill Wumpus (not in a stench tile)
                return REW_WASTE_TIME;
            }
        }
        
        // Player wastes time trying to go past X,Y boundaries
        // (i.e. doesn't move anywhere)
        if (worldAction.equals(World.A_MOVE) && oldX == newX && oldY == newY) {
            return REW_WASTE_TIME;
        }
        
        // Player took bad risk when he shouldn't risk yet
        if (!isSafeExplored && !ow.isSafeTile(newX, newY)) {
            return REW_BAD_RISK;
        }
        
        // Player explores unexplored
        if (ow.isUnknown(newX, newY)) {
            return REW_EXPLORE;
        }
        
        return 0.0;
    }
    
    /**
     * Select action for training step
     * The greater eps is, the greater chance for random action
     * The lesser eps is, the greater chance for taking best Q-value action
     * @param eps e-greedy epsilon
     * @return action index
     */
    private int selectAction(QState state, Double eps) {
        Double chance = rand.nextDouble();
        
        // Full exploration setting
        if (ENABLE_FULL_EXPLORATION) {
            for (int i = 0; i < QState.Q_ARR_SIZE; i++) {
                if (state.actionQValues[i].equals(QState.DEFAULT_VAL)) {
                    return i;
                }
            }
        }
        
        // Using epsilon (0.0 .. 1.0), determine whether to take random action
        // ... or take best Q-value action
        if (eps > chance) {
            return rand.nextInt(5);
        } else {
            return state.argmaxAction();
        }
    }
    
    /**
     * Make episode step using e-greedy Q-learning, updating QTable with results
     * @param w World object
     * @param action QState action index
     */
    private void step(World w, Double eps, Double gamma, Double alpha) {
        QTable qt = QTable.getInstance();
        QState state = qt.getQStateFromWorld(w); // Current state
        int action = selectAction(state, eps); // Select action in current state
        Double reward = observeAction(w, action); // Get reward for executing action from current state
        QState nextState = qt.getQStateFromWorld(w); // State after action
        Double currentVal = state.actionQValues[action];
        Double futureMaxVal = nextState.argmaxValue(); // Get maximum future reward from next state
        
        // Update Q-value for current state with selected action if there was previous Q-value
        // If there wasn't previous Q-value, then initialize it with first reward
        if (!state.actionQValues[action].equals(QState.DEFAULT_VAL)) {
            // Q-learning formula
            // s - current state, a - current state taken action
            // sn - next state, an[] - all next state actions
            // Q(s, a) = (1 - ALPHA) * Q(s, a) + ALPHA * ( REWARD + GAMMA * max(Q(sn, an[])) )
            state.actionQValues[action] = (1.0 - alpha) * currentVal + alpha * (reward + gamma * futureMaxVal);
        } else {
            state.actionQValues[action] = reward;
        }
        
        // Because parseQState will give ref which is stored in QTable (one way or another)
        // ... then there is no need to re-insert it into table, we are simply
        // ... updating existing QState entry fields
    }

    // === Public methods ===
    /**
     * Train episode using e-greedy Q-Learning
     * @param alpha Learning rate
     * @param gamma Discount factor
     * @param eps e-greedy epsilon
     * @param map WorldMap to train on
     */
    public void trainEpisode(Double alpha, Double gamma, Double eps, WorldMap map) {
        World w = map.generateWorld();
        int actionsTaken = 0;
        
        while (!w.gameOver()) {
            this.step(w, eps, gamma, alpha);
            actionsTaken++;
            
            if (actionsTaken > MAX_EP_LEN) {
                break;
            }
        }
    }
}
