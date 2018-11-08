/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wumpusworld;

import java.util.HashMap;

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
        QState parsedState = QState.reducedStateFromWorld(w);
        
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
}
