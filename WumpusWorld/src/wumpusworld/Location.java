package wumpusworld;

/**
 * This class is used by the agent when searchin for new
 * squares to move to. It contains the position for a 
 * square, and a priority. Lower priovalues = higher
 * priority.
 * 
 * @author Johan Hagelbäck
 */
public class Location implements Comparable
{
    /**
     * X position
     */
    public int x;
    /**
     * Y position
     */
    public int y;
    /**
     * Priority value
     */
    public int prio;
    
    /**
     * Creates a new Location instance.
     * 
     * @param x X position
     * @param y Y position
     */
    public Location(int x, int y)
    {
        this.x = x;
        this.y = y;
        this.prio = 0;
    }
    
    /**
     * Creates a new Location instance.
     * 
     * @param x X position
     * @param y Y position
     * @param prio Priority value
     */
    public Location(int x, int y, int prio)
    {
        this.x = x;
        this.y = y;
        this.prio = prio;
    }
    
    /**
     * Used for sorting lists of possible goals. Lower prio
     * value = higher priority.
     * 
     * @param o Location object.
     * @return 1 if prio(o) > prio(this). -1 if prio(o) > prio(this). 0 otherwise.
     */
    public int compareTo(Object o)
    {
        Location l = (Location)o;
        if (l.prio < prio) return 1;
        if (l.prio > prio) return -1;
        return 0;
    }
}
