/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wumpusworld;

import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 *
 * @author Azeroc
 */
public class QState  implements java.io.Serializable  {
    public static final int TILE_ARR_SIZE = 16;
    public static final int Q_ARR_SIZE = 7;
    public static final int KEY_SIZE = 1 + 1 + 1 + TILE_ARR_SIZE;
    public static final int BYTE_SIZE = KEY_SIZE + (Q_ARR_SIZE * 8);
    
    // Default Q-value indicating that it is uninitialized
    public static final Double DEFAULT_VAL = 0.0;
    public static final Double ILLEGAL_VAL = -10000.0;
    
    // Special state bitmask flags
    public static final byte NORMAL          = 0;  // 0000
    public static final byte TAKE_RISKS      = 1;  // 0001
    public static final byte ON_GOLD_STATE   = 2;  // 0010
    public static final byte ON_WUMPUS_STATE = 4;  // 0100
    public static final byte IN_PIT          = 8;  // 1000
    public static final byte HAS_ARROW       = 16; // 0001 1000
    
    // Tile state bitmask flags
    public static final byte TILE_UNEXPLORED = 0; // 0000
    public static final byte TILE_EXPLORED   = 1; // 0001
    public static final byte TILE_BREEZE     = 2; // 0010
    public static final byte TILE_STENCH     = 4; // 0100
    public static final byte TILE_GLITTER    = 8; // 1000
    
    // QState action index constants
    public static final int A_MOVE_UP = 0;
    public static final int A_MOVE_RIGHT = 1;
    public static final int A_MOVE_DOWN = 2;
    public static final int A_MOVE_LEFT = 3;
    public static final int A_SHOOT = 4;
    public static final int A_GRAB = 5;
    public static final int A_CLIMB = 6;
    
    // QState's parsed data    
    public byte specialData;
    public byte playerX;
    public byte playerY;
    public byte tileData[]; // 3x3 tile data around player
    public Double actionQValues[]; // 5 Doubles for the Q values of 5 actions
    
    // QEntry's key for QTable
    // Combination of parsed data into string with exception of actionQValues
    public String getKey() {
        byte[] keyBuf = new byte[KEY_SIZE];        
        int keyItr = 0;
        
        // Special flags
        keyBuf[keyItr++] = this.specialData; 
        // PlayerX
        keyBuf[keyItr++] = this.playerX;
        // PlayerY
        keyBuf[keyItr++] = this.playerY;
        // TileData
        for (int i = 0; i < TILE_ARR_SIZE; i++) {
            keyBuf[keyItr++] = this.tileData[i];
        }
        
        return new String(keyBuf, StandardCharsets.US_ASCII);
    }
    
    /**
     * Get highest QValue possible from this state's actions
     * If no action Q-values have been initialized, then return 0.0
     * @return highest QValue
     */
    public Double argmaxValue() {
        Double max = this.actionQValues[0];
        
        for (int i = 0; i < Q_ARR_SIZE; i++) {
            Double val = this.actionQValues[i];
            max = (val > max) ? val : max;
        }
        
        return max;
    }
    
    /**
     * Get best action by highest Q value
     * @return best action
     */
    public int argmaxAction() {
        int bestAction = 0;
        Double max = this.actionQValues[0];
        
        for (int i = 0; i < Q_ARR_SIZE; i++) {
            Double val = this.actionQValues[i];        
            if (val > max) {
                bestAction = i;
                max = val;
            }
        }
        
        return (bestAction > ILLEGAL_VAL) ? bestAction : -1;
    }
    
    public int argRandomAction(Random rand) {
        int validActionCount = 0;
        int[] validActions = new int[Q_ARR_SIZE];
        
        for (int i = 0; i < Q_ARR_SIZE; i++) {
            Double qval = this.actionQValues[i];
            if (qval > ILLEGAL_VAL) {
                validActions[validActionCount] = i;
                validActionCount++;
            }            
        }
        
        if (validActionCount > 0) {
            return validActions[rand.nextInt(validActionCount)];
        } else {
            return -1;
        }
    }
    
    /**
     * Get actual end-result Q-Values (converting uninitialized values to 0.0)
     * @return QValue Double array
     */
    public Double[] getQValues() {
        Double[] values = new Double[Q_ARR_SIZE];
        for (int i = 0; i < Q_ARR_SIZE; i++) {
            Double val = this.actionQValues[i];
            values[i] = val;
        }
        return values;
    }
    
    public static byte parseTileData(World w, int x, int y) {
        byte data = 0;
        data = (byte)(data | (w.isUnknown(x, y) ? TILE_UNEXPLORED : 0));
        data = (byte)(data | (w.isVisited(x, y) ? TILE_EXPLORED : 0));
        data = (byte)(data | (w.hasBreeze(x, y) ? TILE_BREEZE : 0));
        data = (byte)(data | (w.hasStench(x, y) ? TILE_STENCH : 0));
        data = (byte)(data | (w.hasGlitter(x, y) ? TILE_GLITTER : 0));
        return data;
    }
    
    public static QState parseFromWorldState(World w) {
        // Init vars
        QState state = new QState(); 
        state.tileData = new byte[TILE_ARR_SIZE];
        state.actionQValues = new Double[Q_ARR_SIZE];
        
        // World & player info
        state.playerX = (byte)(w.getPlayerX());
        state.playerY = (byte)(w.getPlayerY());
        boolean isSafeExplored = w.isSafeExplored();
        boolean isOnGold = w.hasGlitter(state.playerX, state.playerY);
        boolean isOnWumpus = w.hasWumpus(state.playerX, state.playerY);
        boolean hasArrow = w.hasArrow();
        boolean inPit = w.isInPit();
        
        if (isOnGold || isOnWumpus) {
            state.playerX = 0;
            state.playerY = 0;
        }
        
        // Set special bitmask flags of QState
        state.specialData = (byte)(state.specialData | (isSafeExplored ? QState.TAKE_RISKS : 0));
        state.specialData = (byte)(state.specialData | (isOnGold ? QState.ON_GOLD_STATE : 0));
        state.specialData = (byte)(state.specialData | (isOnWumpus ? QState.ON_WUMPUS_STATE : 0));
        state.specialData = (byte)(state.specialData | (inPit ? QState.IN_PIT : 0));
        state.specialData = (byte)(state.specialData | (hasArrow ? QState.HAS_ARROW : 0));
        
        // Parse tile data
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                int index = x + (y*4);
                
                if (isOnGold || isOnWumpus) {
                    state.tileData[index] = 0;
                } else {
                    state.tileData[index] = parseTileData(w, x+1, y+1);
                }                
            }
        }
        
        // Default vals for Q Action-Values
        for (int i = 0; i < Q_ARR_SIZE; i++) {
            if (isQActionLegal(w, i)) {
                state.actionQValues[i] = QState.DEFAULT_VAL;
            } else {
                state.actionQValues[i] = QState.ILLEGAL_VAL;
            }            
        }
        
        return state;
    }
    
    public static boolean isQActionLegal(World w, int action) {
        int x = w.getPlayerX();
        int y = w.getPlayerY();
        boolean stench = w.hasStench(x, y);
        boolean wumpusConfirmed = w.isWumpusConfirmed();
        boolean safeExplored = w.isSafeExplored();
        
        // Climbing
        if (w.isInPit() && action != A_CLIMB) return false;
        if (w.isInPit() && action == A_CLIMB) return true;
        if (!w.isInPit() && action == A_CLIMB) return false;
        
        // Moving up
        if (action == A_MOVE_UP) {
            if (!w.isValidPosition(x, y+1)) {
                return false;
            }
            if (w.isMaybePitTile(x, y+1) && !safeExplored) {
                return false;
            }
            if (w.confirmedWumpusTile(x, y+1)) {
                return false;
            }
            if (stench && !wumpusConfirmed && w.isUnknown(x, y+1)) {
                return false;
            }
        }
        
        // Moving right
        if (action == A_MOVE_RIGHT) {
            if (!w.isValidPosition(x+1, y)) {
                return false;
            }
            if (w.isMaybePitTile(x+1, y) && !safeExplored) {
                return false;
            }
            if (w.confirmedWumpusTile(x+1, y)) {
                return false;
            }
            if (stench && !wumpusConfirmed && w.isUnknown(x+1, y)) {
                return false;
            }
        }
        
        // Moving down
        if (action == A_MOVE_DOWN) {
            if (!w.isValidPosition(x, y-1)) {
                return false;
            }
            if (w.isMaybePitTile(x, y-1) && !safeExplored) {
                return false;
            }
            if (w.confirmedWumpusTile(x, y-1)) {
                return false;
            }
            if (stench && !wumpusConfirmed && w.isUnknown(x, y-1)) {
                return false;
            }
        }
        
        // Moving left
        if (action == A_MOVE_LEFT) {
            if (!w.isValidPosition(x-1, y)) {
                return false;
            }
            if (w.isMaybePitTile(x-1, y) && !safeExplored) {
                return false;
            }
            if (w.confirmedWumpusTile(x-1, y)) {
                return false;
            }
            if (stench && !wumpusConfirmed && w.isUnknown(x-1, y)) {
                return false;
            }
        }
        
        // Shooting arrow
        if (action == A_SHOOT) {
            if (!w.hasArrow()) return false;
            
            // Special case (when starting in a stench)
            if (w.isUnknown(1, 2) && w.isUnknown(2, 1) && w.hasStench(1, 1)) {
                return true;
            }            
            
            if (w.confirmedWumpusTile(x, y+1)) return true;
            if (w.confirmedWumpusTile(x+1, y)) return true;
            if (w.confirmedWumpusTile(x, y-1)) return true;
            if (w.confirmedWumpusTile(x-1, y)) return true;
            return false;
        }
        
        // Grabbing gold
        if (action == A_GRAB) {
            if (!w.hasGlitter(x, y)) {
                return false;
            }
        }
        
        return true;
    }
    
    public static void doQStateAction(World w, int qstateAction) {
        switch (qstateAction) {
            case A_MOVE_UP:
                if (w.getDirection() == World.DIR_DOWN) w.doAction(World.A_TURN_LEFT);
                if (w.getDirection() == World.DIR_RIGHT) w.doAction(World.A_TURN_LEFT);
                if (w.getDirection() == World.DIR_LEFT) w.doAction(World.A_TURN_RIGHT);
                w.doAction(World.A_MOVE);
                break;
            case A_MOVE_RIGHT:
                if (w.getDirection() == World.DIR_LEFT) w.doAction(World.A_TURN_RIGHT);
                if (w.getDirection() == World.DIR_UP) w.doAction(World.A_TURN_RIGHT);
                if (w.getDirection() == World.DIR_DOWN) w.doAction(World.A_TURN_LEFT);
                w.doAction(World.A_MOVE);
                break;
            case A_MOVE_DOWN:
                if (w.getDirection() == World.DIR_UP) w.doAction(World.A_TURN_LEFT);
                if (w.getDirection() == World.DIR_LEFT) w.doAction(World.A_TURN_LEFT);
                if (w.getDirection() == World.DIR_RIGHT) w.doAction(World.A_TURN_RIGHT);
                w.doAction(World.A_MOVE);
                break;
            case A_MOVE_LEFT:
                if (w.getDirection() == World.DIR_RIGHT) w.doAction(World.A_TURN_LEFT);
                if (w.getDirection() == World.DIR_UP) w.doAction(World.A_TURN_LEFT);
                if (w.getDirection() == World.DIR_DOWN) w.doAction(World.A_TURN_RIGHT);
                w.doAction(World.A_MOVE);                
                break;
            case A_SHOOT: // Automatically lock on confirmed Wumpus tile and then shoot the arrow (will be a miss if wumpus not present)
                int playerX = w.getPlayerX();
                int playerY = w.getPlayerY();
                for (int i = 0; i < 3; i++) {
                    if (w.getDirection() == World.DIR_UP    && w.confirmedWumpusTile(playerX,   playerY+1)) break;
                    if (w.getDirection() == World.DIR_RIGHT && w.confirmedWumpusTile(playerX+1, playerY  )) break;
                    if (w.getDirection() == World.DIR_DOWN  && w.confirmedWumpusTile(playerX,   playerY-1)) break;
                    if (w.getDirection() == World.DIR_LEFT  && w.confirmedWumpusTile(playerX-1, playerY  )) break;
                    w.doAction(World.A_TURN_RIGHT);
                }
                w.doAction(World.A_SHOOT);
                break;
            case A_GRAB:
                w.doAction(World.A_GRAB);
                break;
            case A_CLIMB:
                w.doAction(World.A_CLIMB);
                break;
        }
    }
}