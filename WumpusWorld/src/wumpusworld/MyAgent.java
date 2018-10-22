package wumpusworld;

/**
 * Contains starting code for creating your own Wumpus World agent.
 * Currently the agent only make a random decision each turn.
 * 
 * @author Johan Hagelbäck
 */
public class MyAgent implements Agent
{
    private final World w;
    
    /**
     * Creates a new instance of your solver agent.
     * 
     * @param world Current world state 
     */
    public MyAgent(World world)
    {
        w = world;   
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
        w.doAction(QState.resolveToWorldAction(action));
    }
}

