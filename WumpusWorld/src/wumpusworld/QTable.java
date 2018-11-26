/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wumpusworld;

import java.util.HashMap;
import java.io.*;
import java.util.Collections;

/**
 *
 * @author Azeroc
 */
public class QTable {
    private static final QTable QTABLE_SINGLETON = new QTable();    
    private final HashMap<String, QState> _table;
    
    public QTable() {
        _table = new HashMap<>();
    }
    
    // Public methods
    /**
     * Get instance of the global QTable singleton
     * @return 
     */
    public static QTable getInstance() {
        return QTABLE_SINGLETON;
    }
    
    /**
     * Store/Replace QState in QTable
     * @param state QState object
     */
    public void storeQState(QState state) {
        this._table.put(state.getKey(), state);
    }
    
    /**
     * Get QState via key, returns null if QState at key doesn't exist
     * @param key QState key
     * @return QState object
     */
    public QState getQState(String key) {
        return _table.get(key);
    }
    
    /**
     * Parse QState from world and get full version from Q-table
     * @param w World object
     * @return World current state's QState
     */
    public QState getQStateFromWorld(World w) {
        QState parsedState = QState.parseFromWorldState(w);
        
        // Try to get current Q-Values from singleton QTable
        String key = parsedState.getKey();
        QState mappedState = this.getQState(key);
        
        // Store parsed state in QTable it if it doesn't exist and return its ref as result
        // Otherwise simply return stored QTable entry ref
        if (mappedState == null) {          
            this.storeQState(parsedState);            
            return parsedState;
        } else {            
            return mappedState;
        }
    }
    
    /**
     * Get stored QState count
     * @return QState count
     */
    public int getQStateCount() {
        return _table.size();
    }
    
    public void saveToFile()
    {
        try 
        {
            FileOutputStream fileOut =
         new FileOutputStream("wumpus.ser");
         ObjectOutputStream out = new ObjectOutputStream(fileOut);
         out.writeObject(_table);
          System.out.printf("> saved... %d objects", _table.size() );
         out.close();
         fileOut.close();
          }
        catch (IOException i) {
         i.printStackTrace();
           }
    }
    
     
    public void loadFromFile()
    {
        try 
        {
            FileInputStream fileOut =
         new FileInputStream("wumpus.ser");
         ObjectInputStream in = new ObjectInputStream(fileOut);
         HashMap<String, QState> tab;
         tab = (HashMap) in.readObject();
           System.out.printf("> loaded... %d objects", tab.size() );
           
           _table.clear();
           _table.putAll(tab);
         
         in.close();
         fileOut.close();
          }
        catch (IOException i) {
         i.printStackTrace();
           }
        catch (ClassNotFoundException c) {
         System.out.println("HashMap class not found");
         c.printStackTrace();
         return;
      }
    }
}
