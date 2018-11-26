package wumpusworld;

import java.util.Random;

/**
 * Contains starting code for creating your own Wumpus World agent.
 * Currently the agent only make a random decision each turn.
 * 
 * @author Johan Hagelbäck
 */
public class MyAgent implements Agent
{
    // LEARNING ENVIRONMENT SETTINGS
    public static final int MAX_EP_LEN = 2500; // Max episode length (Max actions per episode)
    
    // CONSEQUENCE REWARDS
    public static final Double REW_ACTION = 0.0; // Normal reward for action which didnt trigger other special rewards
    public static final Double REW_GOLD = 10.0; // Grab gold
    public static final Double REW_EXPLORE = 1.0; // Explore unexplored tiles
    
    // Private members
    private final Random rand;    
    
    private final World w;
    
    /**
     * Creates a new instance of your solver agent.
     * 
     * @param world Current world state 
     */
    public MyAgent(World world)
    {
        this.w = world;
        this.rand = new Random();
    }
    
    /**
     * Take action in World and observe reward from taking it
     * @param w World object
     * @param action QState action index
     * @return Reward of action consequence
     */
    private Double observeAction(World w, int action) {
        // Clone pre-action state and get its coords
        World ow = w.cloneWorld();
        
        // Do action
        QState.doQStateAction(w, action);
        
        // Get post-action state
        int newX = w.getPlayerX();
        int newY = w.getPlayerY();        
        
        // Player stepped on the gold after bad-risk and wumpus checks
        if (w.hasGold()) {
            return REW_GOLD;
        }
        
        // Exploring
        if (ow.isUnknown(newX, newY)) {
            return REW_EXPLORE;
        }

        // If we got this far, then the action was moving into explored regions
        return REW_ACTION;
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
        
        // Using epsilon (0.0 .. 1.0), determine whether to take random action
        // ... or take best Q-value action
        if (eps > chance) {
            return state.argRandomAction(rand);
        } else {
            return state.argmaxAction();
        }
    }
    
    /**
     * Make action-world step using e-greedy Q-learning (or actionOverride), updating QTable with results
     * @param w World object to do step in
     * @param eps e-greedy epsilon
     * @param gamma reward discount factor
     * @param alpha learning rate
     * @param actionOverride overwrite action selection with this if non-negative
     * @return early exit signal (go to a state with no valid actions to take)
     */
    private boolean step(World w, Double eps, Double gamma, Double alpha) {
        QTable qt = QTable.getInstance();
        QState state = qt.getQStateFromWorld(w); // Current state
        
        int action = selectAction(state, eps); // Select action in current state
        if (action == -1) { // early exit signal (go to a state with no valid actions to take)
            return true;
        }
        
        Double reward = observeAction(w, action); // Get reward for executing action from current state
        QState nextState = qt.getQStateFromWorld(w); // State after action
        Double currentVal = state.actionQValues[action];
        Double futureMaxVal = nextState.argmaxValue(); // Get maximum future reward from next state
        
        // Q-learning formula
        // s - current state, a - current state taken action
        // sn - next state, an[] - all next state actions
        // Q(s, a) = (1 - ALPHA) * Q(s, a) + ALPHA * ( REWARD + GAMMA * max(Q(sn, an[])) )
        state.actionQValues[action] = (1.0 - alpha) * currentVal + alpha * (reward + gamma * futureMaxVal);
        
        // Because parseQState will give ref which is stored in QTable (one way or another)
        // ... then there is no need to re-insert it into table, we are simply
        // ... updating existing QState entry fields
        
        // No early exit yet
        return false;
    }    
   
    /**
     * Asks your solver agent to execute an action.
     */
    @Override
    public void doAction()
    {
        QTable qt = QTable.getInstance();
        QState state = qt.getQStateFromWorld(w);
        int action = state.argmaxAction();
        QState.doQStateAction(w, action);
    }
    
    /**
     * Train episode using e-greedy Q-Learning
     * @param alpha Learning rate
     * @param gamma Discount factor
     * @param eps e-greedy epsilon
     * @param map WorldMap to train on
     */
    public void trainEpisode(Double alpha, Double gamma, Double eps, WorldMap map) {
        World mapWorld = map.generateWorld();
        int actionsTaken = 0;
        
        while (!mapWorld.gameOver()) {
            boolean earlyExit = this.step(mapWorld, eps, gamma, alpha);
            actionsTaken++;

            if (earlyExit || actionsTaken > MAX_EP_LEN) break;
        }
    }
}

