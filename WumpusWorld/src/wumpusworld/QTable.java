/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wumpusworld;

import java.util.Dictionary;

/**
 *
 * @author Azeroc
 */
public class QTable {
    private static final QTable QTABLE_SINGLETON = new QTable();    
    private Dictionary<String, QState> _table;
    
    // Public methods
    /**
     * Get instance of the global QTable singleton
     * @return 
     */
    public QTable getInstance() {
        return QTABLE_SINGLETON;
    }
    
    /**
     * Store/Replace QState in QTable
     * @param state QState object
     */
    public void storeQState(QState state) {
        this._table.put(state.getKey(), state);
    }
}
