package wumpusworld;

import java.io.*;

/**
 * Reads the config.txt settings file.
 * 
 * @author admin
 */
public class Config 
{
    /**
     * Checks the options for the application ('gui', 'sim' or 'simdb').
     * 
     * @return Option. Default is 'gui'. 
     */
    public static String getOption()
    {
        String option = "gui";
        try
        {
            BufferedReader r = new BufferedReader(new FileReader("config.txt"));
            String line = r.readLine();
            while (line != null)
            {
                if (line.startsWith("Option="))
                {
                    String[] tokens = line.split("=");
                    if (tokens[1].equalsIgnoreCase("gui")) option = "gui";
                    if (tokens[1].equalsIgnoreCase("sim")) option = "sim";
                    if (tokens[1].equalsIgnoreCase("simulator")) option = "sim";
                    if (tokens[1].equalsIgnoreCase("simdb")) option = "simdb";
                    if (tokens[1].equalsIgnoreCase("simulatordb")) option = "simdb";
                }
                line = r.readLine();
            }
            r.close();
        }
        catch (Exception ex)
        {
            option = "gui";
        }
        return option;
    }
    
    /**
     * Returns the path to the map file.
     * 
     * @return Path to map file, or empty string if not found.
     */
    public static String getMapfile()
    {
        String mapfile = "";
        try
        {
            BufferedReader r = new BufferedReader(new FileReader("config.txt"));
            String line = r.readLine();
            while (line != null)
            {
                if (line.startsWith("Mapfile="))
                {
                    String[] tokens = line.split("=");
                    mapfile = tokens[1];
                }
                line = r.readLine();
            }
            r.close();
        }
        catch (Exception ex)
        {
            mapfile = "";
        }
        return mapfile;
    }
}
