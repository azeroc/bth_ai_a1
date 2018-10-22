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
    // LEARNING PARAMS
    public static final Double ALPHA = 0.01; // Learning Rate
    public static final Double GAMMA = 0.99; // Reward discount
    public static final int MAX_EP_LEN = 10000; // Max episode length (Max actions per episode)
    
    // ACTION REWARD CONSTANTS
    public static final Double REW_ACT_TL = -5.0;   // Turn Left
    public static final Double REW_ACT_FW = -1.0;   // Forward
    public static final Double REW_ACT_TR = -5.0;   // Turn Right
    public static final Double REW_ACT_GR = -10.0;  // Grab
    public static final Double REW_ACT_CL = -10.0;  // Climb
    public static final Double REW_ACT_SH = -100.0; // Shoot
    
    // CONSEQUENCE REWARDS
    public static final Double REW_WUMPUS = -2000.0;   // Step into Wumpus
    public static final Double REW_PIT = -1000.0;      // Step into Pit
    public static final Double REW_CLIMB_OUT = 10.0;   // Climb out of pit
    public static final Double REW_GRAB_GOLD = 1000.0; // Grab gold
    
    // Private members
    private final Random rand;
    
    public WumpusEnv() {
        rand = new Random();
    }    
    
    // === Private methods ===
    
    /**
     * Resolve QState action index to Reward
     * @param action QState action index
     * @return Reward of taking the action
     */
    private Double resolveActionReward(int action) {
        switch (action) {
            case 0:
                return REW_ACT_TL;
            case 1:
                return REW_ACT_FW;
            case 2:
                return REW_ACT_TR;
            case 3:
                return REW_ACT_GR;
            case 4:
                return REW_ACT_CL;
            case 5:
                return REW_ACT_SH;
            default: // This should not happen
                return 0.0;
        }
    }
    
    /**
     * Resolve special consequences from taking given action in World w
     * @param w World object
     * @param action QState action index
     * @return Reward
     */
    private Double resolveConsequenceReward(World w, int action) {
        // Get pre-action state
        boolean oldPitFlag = w.isInPit();
        
        // Do action
        w.doAction(QState.resolveToWorldAction(action));
        
        // Get post-action state
        int newX = w.getPlayerX();
        int newY = w.getPlayerY();
        boolean newPitFlag = w.isInPit();
        
        // Player has climbed out
        if (oldPitFlag && !newPitFlag) {
            return REW_CLIMB_OUT;
        }
        
        // Player has grabbed gold
        if (w.hasGold()) {
            return REW_GRAB_GOLD;
        }
        
        // Player stepped into Wumpus
        if (w.hasWumpus(newX, newY)) {
            return REW_WUMPUS;
        }
        
        // Player has fallen into pit
        if (!oldPitFlag && newPitFlag) {
            return REW_PIT;
        }
        
        return 0.0;
    }
    
    /**
     * Take action in World and observe reward from taking it
     * @param w World object
     * @param action QState action index
     * @return Action reward + Consequences reward
     */
    private Double observeAction(World w, int action) {        
        Double act_rew = resolveActionReward(action);
        Double consequence_rew = resolveConsequenceReward(w, action);
        return act_rew + consequence_rew;
    }
    
    /**
     * Select action via e-greedy Q-Value method
     * The greater eps is, the greater chance for random action
     * The lesser eps is, the greater chance for taking best Q-value action
     * @param eps e-greedy epsilon
     * @return action index
     */
    private int selectAction(QState state, Double eps) {
        Double chance = rand.nextDouble();
        
        // Using epsilon (0.0 .. 1.0), determine whether to take random action
        // ... or take best Q-value action
        if (eps > chance) {
            return rand.nextInt(6);
        } else {
            return state.argmaxAction();
        }
    } 
    
    /**
     * Make episode step using e-greedy Q-learning, updating QTable with results
     * @param w World object
     * @param action QState action index
     */
    private void step(World w, Double eps) {
        QTable qt = QTable.getInstance();
        QState state = qt.getQStateFromWorld(w); // Current state
        int action = selectAction(state, eps); // Select action in current state
        Double reward = observeAction(w, action); // Get reward for executing action from current state
        QState nextState = qt.getQStateFromWorld(w); // State after action
        Double currentVal = state.actionQValues[action];
        Double futureMaxVal = nextState.argmaxValue(); // Get maximum future reward from next state
        
        // Update Q-value for current state with selected action if there was previous Q-value
        // If there wasn't previous Q-value, then initialize it with first reward
        if (state.actionQValues[action] > Double.MIN_VALUE) {
            // Q-learning formula
            // s - current state, a - current state taken action
            // sn - next state, an[] - all next state actions
            // Q(s, a) = (1 - ALPHA) * Q(s, a) + ALPHA * ( REWARD + GAMMA * max(Q(sn, an[])) )
            state.actionQValues[action] = (1.0 - ALPHA) * currentVal + ALPHA * (reward + GAMMA * futureMaxVal);
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
     * @param eps e-greedy epsilon
     * @param map WorldMap to train on
     */
    public void trainEpisode(Double eps, WorldMap map) {
        World w = map.generateWorld();
        
        while (!w.gameOver()) {
            this.step(w, eps);
        }
    }
}
