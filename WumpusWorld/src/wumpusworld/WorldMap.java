package wumpusworld;

import java.util.Vector;
import java.awt.Point;

/**
 * This class represents a Wumpus World map read
 * from the file.
 * 
 * @author Johan Hagelbäck
 */
public class WorldMap
{
    private int size;
    private Point wumpus;
    private Point gold;
    private Vector<Point> pits;
    
    /**
     * Creates a new map instance.
     * 
     * @param size Size of the world map
     */
    public WorldMap(int size)
    {
        this.size = size;
        pits = new Vector<Point>();
    }
    
    /**
     * Adds the Wumpus.
     * 
     * @param x X position
     * @param y Y position
     */
    public void addWumpus(int x, int y)
    {
        wumpus = new Point(x,y);
    }
    
    /**
     * Adds the gold treasure.
     * 
     * @param x X position
     * @param y Y position
     */
    public void addGold(int x, int y)
    {
        gold = new Point(x,y);
    }
    
    /**
     * Adds a pit. The map can have any number of pits.
     * 
     * @param x X position
     * @param y Y position
     */
    public void addPit(int x, int y)
    {
        pits.add(new Point(x,y));
    }
    
    /**
     * Returns the size of the world map.
     * 
     * @return The size
     */
    public int getSize()
    {
        return size;
    }
    
    /**
     * Returns position for the Wumpus.
     * 
     * @return The position 
     */
    public Point getWumpus()
    {
        return wumpus;
    }
    
    /**
     * Returns position for the gold treasure.
     * 
     * @return The position 
     */
    public Point getGold()
    {
        return gold;
    }
    
    /**
     * Returns positions for all pits.
     * 
     * @return A list of positions for pits
     */
    public Vector<Point> getPits()
    {
        return pits;
    }
    
    /**
     * Checks if there is a pit in the specified location.
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @return True if there is a pit, false it not
     */
    public boolean hasPit(int x, int y)
    {
        for (Point p:pits)
        {
            if (p.x == x && p.y == y)
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Generates a Wumpus World instance from this map.
     * 
     * @return Wumpus World instance
     */
    public World generateWorld()
    {
        World w = new World(size);
        w.addWumpus(wumpus.x, wumpus.y);
        w.addGold(gold.x, gold.y);
        for (int i = 0; i < pits.size(); i++)
        {
            w.addPit(pits.get(i).x, pits.get(i).y);
        }
        return w;
    }
}
