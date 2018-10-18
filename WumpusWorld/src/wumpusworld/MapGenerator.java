package wumpusworld;

import java.util.Random;

/**
 * This class generates random Wumpus World maps.
 *
 * @author Johan Hagelbäck
 */
public class MapGenerator 
{
    /**
     * Generates a random Wumpus World map.
     * 
     * @param seed Seed for the randomizer. Same seed always results in the same random map.
     * @return Generated Wumpus World
     */
    public static WorldMap getRandomMap(int seed)
    {
        Random rnd = new Random(seed);
        WorldMap w = new WorldMap(4);
        
        addRandomWumpus(w,rnd);
        addRandomGold(w,rnd);
        addRandomPit(w,rnd);
        addRandomPit(w,rnd);
        addRandomPit(w,rnd);
        
        return w;
    }
    
    /**
     * Adds a pit to a random square.
     * 
     * @param w Wumpus World
     * @param r Randomizer
     */
    private static void addRandomPit(WorldMap w, Random r)
    {
        boolean valid = false;
        while (!valid)
        {
            int x = rnd(r);
            int y = rnd(r);
            if (!(x == 1 && y == 1) && !w.hasPit(x, y))
            {
                valid = true;
                w.addPit(x, y);
            }
        }
    }
    
    /**
     * Adds the Wumpus to a random square.
     * 
     * @param w Wumpus World
     * @param r Randomizer
     */
    private static void addRandomWumpus(WorldMap w, Random r)
    {
        boolean valid = false;
        while (!valid)
        {
            int x = rnd(r);
            int y = rnd(r);
            if (!(x == 1 && y == 1))
            {
                valid = true;
                w.addWumpus(x, y);
            }
        }
    }
    
    /**
     * Adds the gold treasure to a random square.
     * 
     * @param w Wumpus World
     * @param r Randomizer
     */
    private static void addRandomGold(WorldMap w, Random r)
    {
        boolean valid = false;
        while (!valid)
        {
            int x = rnd(r);
            int y = rnd(r);
            if (!(x == 1 && y == 1))
            {
                valid = true;
                w.addGold(x, y);
            }
        }
    }
    
    /**
     * 
     * 
     * @param rnd
     * @return 
     */
    private static int rnd(Random rnd)
    {
        return rnd.nextInt(4) + 1;
    }
}
