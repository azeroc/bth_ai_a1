package wumpusworld;

import java.io.*;
import java.util.Vector;
/**
 * This class read maps from a file.
 * 
 * @author Johan Hagelbäck
 */
public class MapReader
{
    private Vector<WorldMap> maps;
    private String mapFilename;
    
    /**
     * Creates a new instance of the class.
     */
    public MapReader()
    {
        mapFilename = Config.getMapfile();
        maps = new Vector<WorldMap>();
    }
    
    /**
     * Read the maps from the map file and returns a Vector
     * with the maps. The program exits if an error is
     * encountered.
     * 
     * @return A list of map objects, or empty list if none was found. 
     */
    public Vector<WorldMap> readMaps()
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(mapFilename));
            
            WorldMap wm = null;
            String line = reader.readLine();
            while (line != null)
            {
                line = line.toUpperCase();
                
                //New map
                if (line.startsWith("NEW"))
                {
                    String[] t = split(line);
                    int size = Integer.parseInt(t[1]);
                    wm = new WorldMap(size);
                }
                
                //Pit
                if (line.startsWith("P"))
                {
                    String[] t = split(line);
                    int x = Integer.parseInt(t[1]);
                    int y = Integer.parseInt(t[2]);
                    wm.addPit(x,y);
                }
                
                //Wumpus
                if (line.startsWith("W"))
                {
                    String[] t = split(line);
                    int x = Integer.parseInt(t[1]);
                    int y = Integer.parseInt(t[2]);
                    wm.addWumpus(x,y);
                }
                
                //Gold
                if (line.startsWith("G"))
                {
                    String[] t = split(line);
                    int x = Integer.parseInt(t[1]);
                    int y = Integer.parseInt(t[2]);
                    wm.addGold(x,y);
                }
                
                //End of map
                if (line.startsWith("END"))
                {
                    maps.add(wm);
                }
                
                line = reader.readLine();
            }
            
            reader.close();
        }
        catch (Exception ex)
        {
            
        }
        
        //Add some random maps
        maps.add(MapGenerator.getRandomMap(42));
        maps.add(MapGenerator.getRandomMap(1977));
        maps.add(MapGenerator.getRandomMap(1990));
        
        return maps;
    }
    
    /**
     * Splits a string with whitespace as delimiter.
     * 
     * @param line The string to split
     * @return Tokens
     */
    private String[] split(String line)
    {
        return line.split(" ");
    }
}
