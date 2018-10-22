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
        QState state = new QState();  
        state.tileData = new byte[16];
        state.playerD = (byte)w.getDirection();
        state.playerX = (byte)w.getPlayerX();
        state.playerY = (byte)w.getPlayerY();
        state.hasArrow = (byte)(w.hasArrow() ? 1 : 0);
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                int i = 4*y + x;
                byte td;
                if (w.isUnknown(x, y)) {
                    td = QState.UNEXPLORED;
                } else {
                    td = QState.EXPLORED;
                    td = (byte)( td | (w.hasBreeze(x, y)  ? QState.BREEZE  : 0) );
                    td = (byte)( td | (w.hasStench(x, y)  ? QState.STENCH  : 0) );
                    td = (byte)( td | (w.hasPit(x, y)     ? QState.PIT     : 0) );
                    td = (byte)( td | (w.hasWumpus(x, y)  ? QState.WUMPUS  : 0) );
                    td = (byte)( td | (w.hasGlitter(x, y) ? QState.GLITTER : 0) );
                }
                
                state.tileData[i] = td;
            }
        }                
        
        // Try to get current Q-Values from singleton QTable
        String key = state.getKey();
        QState mappedState = this.getQState(key);
        
        // Store parsed state in QTable it if it doesn't exist and return its ref as result
        // Otherwise simply return stored QTable entry ref
        if (mappedState == null) {
            state.actionQValues = new Double[6];
            state.actionQValues[0] = Double.MIN_VALUE;
            state.actionQValues[1] = Double.MIN_VALUE;
            state.actionQValues[2] = Double.MIN_VALUE;
            state.actionQValues[3] = Double.MIN_VALUE;
            state.actionQValues[4] = Double.MIN_VALUE;
            state.actionQValues[5] = Double.MIN_VALUE;
            this.storeQState(state);
            return state;
        } else {
            return mappedState;
        }
    }
}
