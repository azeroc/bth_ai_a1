/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wumpusworld;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Azeroc
 */
public class QState {
    public static final int TILE_ARR_SIZE = 9;
    public static final int Q_ARR_SIZE = 5;
    public static final int KEY_SIZE = 1 + 1 + TILE_ARR_SIZE;
    public static final int BYTE_SIZE = KEY_SIZE + (Q_ARR_SIZE * 8);
    
    // Default Q-value indicating that it is uninitialized
    public static final Double DEFAULT_VAL = Double.NEGATIVE_INFINITY;
    
    // Special state bitmask flags
    public static final byte NORMAL     = 0;  // 0000 0000
    public static final byte TAKE_RISKS = 1;  // 0000 0001
    public static final byte IN_PIT     = 2;  // 0000 0010
    public static final byte ON_GOLD    = 4;  // 0000 0100
    public static final byte ON_WUMPUS  = 8;  // 0000 1000  
    public static final byte HAS_ARROW   = 16; // 0001 0000
    
    // Tile state bitmask flags
    public static final byte TILE_UNEXPLORED = 0;  // 0000 0000
    public static final byte TILE_EXPLORED   = 1;  // 0000 0001
    public static final byte TILE_BREEZE     = 2;  // 0000 0010
    public static final byte TILE_STENCH     = 4;  // 0000 0100
    public static final byte TILE_PIT        = 8;  // 0000 1000  
    public static final byte TILE_WALL       = 16; // 0001 0000
    public static final byte TILE_GLITTER    = 32; // 0010 0000
    
    // QState's parsed data    
    public byte specialData;
    public byte direction; // Player Direction (see: Player Directions constants)
    public byte tileData[]; // 3x3 tile data around player
    public Double actionQValues[]; // 5 Doubles for the Q values of 5 actions
    
    // QEntry's key for QTable
    // Combination of parsed data into string with exception of actionQValues
    public String getKey() {
        byte[] keyBuf = new byte[KEY_SIZE];        
        int keyItr = 0;
        
        // Special flags
        keyBuf[keyItr++] = this.specialData; 
        // Direction
        keyBuf[keyItr++] = this.direction; 
        // TileData
        for (int i = 0; i < TILE_ARR_SIZE; i++) {
            keyBuf[keyItr++] = this.tileData[i];
        }
        
        return new String(keyBuf, StandardCharsets.US_ASCII);
    }
    
    /**
     * Decode byte array as QState object
     * @param bytes QState in binary form
     * @return decoded QState object
     */
    public static QState decode(byte[] bytes) {
        // Control check
        if (bytes.length < BYTE_SIZE) {
            return null;
        }        
        
        byte[] doubleBuf = new byte[8];
        QState res = new QState();
        res.tileData = new byte[TILE_ARR_SIZE];
        res.actionQValues = new Double[Q_ARR_SIZE];
        int byteItr = 0;      
        
        // Special bitmask flags byte
        res.specialData = bytes[byteItr++];
        
        // Player direction byte
        res.direction = bytes[byteItr++];
        
        // Tile data
        for (int i = 0; i < TILE_ARR_SIZE; i++) {
            res.tileData[i] = bytes[byteItr++];
        }
        
        // Action Q Values
        for (int j = 0; j < Q_ARR_SIZE; j++) {          
            doubleBuf[0] = bytes[byteItr++];
            doubleBuf[1] = bytes[byteItr++];
            doubleBuf[2] = bytes[byteItr++];
            doubleBuf[3] = bytes[byteItr++];
            doubleBuf[4] = bytes[byteItr++];
            doubleBuf[5] = bytes[byteItr++];
            doubleBuf[6] = bytes[byteItr++];
            doubleBuf[7] = bytes[byteItr++];            
            res.actionQValues[j] = ByteBuffer.wrap(doubleBuf).getDouble();
        }
        
        return res;
    }
    
    /**
     * Encode QState into binary array
     * @param state QState object
     * @return binary array describing QState object
     */
    public static byte[] encode(QState state) {
        // [1: PlayerD][1: HasArrow, PlayerY, PlayerX][16: TileData][6*8: 6 action Q Values]
        // Total bytes per QState entry: 1 + 1 + 16 + 6*8 = 66 bytes
        byte[] res = new byte[BYTE_SIZE];
        byte[] doubleBuf = new byte[8];
        int resItr = 0;
        
        // Special bitmask flags byte
        res[resItr++] = state.specialData;
        
        // Player direction byte
        res[resItr++] = state.direction;
        
        // TileData
        for (int i = 0; i < TILE_ARR_SIZE; i++) {
            res[resItr++] = state.tileData[i];
        }
        
        // Action Q Values
        for (int j = 0; j < Q_ARR_SIZE; j++) {
            ByteBuffer.wrap(doubleBuf).putDouble(state.actionQValues[j]);
            res[resItr++] = doubleBuf[0];
            res[resItr++] = doubleBuf[1];
            res[resItr++] = doubleBuf[2];
            res[resItr++] = doubleBuf[3];
            res[resItr++] = doubleBuf[4];
            res[resItr++] = doubleBuf[5];
            res[resItr++] = doubleBuf[6];
            res[resItr++] = doubleBuf[7];
        }
        return res;
    }
    
    /**
     * Get highest QValue possible from this state's actions
     * If no action Q-values have been initialized, then return 0.0
     * @return highest QValue
     */
    public Double argmaxValue() {
        Double max = 0.0;
        
        for (int i = 0; i < Q_ARR_SIZE; i++) {
            Double val = this.actionQValues[i];
            val = (val.equals(DEFAULT_VAL)) ? 0.0 : val;
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
        max = (max.equals(DEFAULT_VAL)) ? 0.0 : max;
        
        for (int i = 0; i < Q_ARR_SIZE; i++) {
            Double val = this.actionQValues[i];
            val = (val.equals(DEFAULT_VAL)) ? 0.0 : val;            
            if (val > max) {
                bestAction = i;
                max = val;
            }
        }
        
        return bestAction;
    }
    
    /**
     * Get actual end-result Q-Values (converting uninitialized values to 0.0)
     * @return QValue Double array
     */
    public Double[] getQValues() {
        Double[] values = new Double[Q_ARR_SIZE];
        for (int i = 0; i < Q_ARR_SIZE; i++) {
            Double val = this.actionQValues[i];
            val = (val.equals(DEFAULT_VAL)) ? 0.0 : val;
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
        data = (byte)(data | (w.hasPit(x, y) ? TILE_PIT : 0));
        data = (byte)(data | (!w.isValidPosition(x, y) ? TILE_WALL : 0));
        return data;
    }
    
    public static QState parseFromWorldState(World w) {
        // Init vars
        QState state = new QState(); 
        state.tileData = new byte[TILE_ARR_SIZE];
        state.actionQValues = new Double[Q_ARR_SIZE];
        
        // Get current world & player info
        int x = w.getPlayerX();
        int y = w.getPlayerY();
        int d = w.getDirection();
        boolean isInPit = w.isInPit();
        boolean isOnGold = w.hasGlitter(x, y);
        boolean isOnWumpus = w.hasWumpus(x, y);
        boolean hasArrow = w.hasArrow();
        
        // Set special bitmask flags of QState
        state.specialData = (byte)(state.specialData | (isInPit ? QState.IN_PIT : 0));
        state.specialData = (byte)(state.specialData | (isOnGold ? QState.ON_GOLD : 0));
        state.specialData = (byte)(state.specialData | (isOnWumpus ? QState.ON_WUMPUS : 0));
        state.specialData = (byte)(state.specialData | (hasArrow ? QState.HAS_ARROW : 0));
        
        // Set player direction
        state.direction = (byte)d;
        
        // Set tile data around player
        // [0] - top-left, [8] - bottom-right
        state.tileData[0] = parseTileData(w, x-1, y+1);
        state.tileData[1] = parseTileData(w, x,   y+1);
        state.tileData[2] = parseTileData(w, x+1, y+1);
        state.tileData[3] = parseTileData(w, x-1, y);
        state.tileData[4] = parseTileData(w, x,   y);
        state.tileData[5] = parseTileData(w, x+1, y);
        state.tileData[6] = parseTileData(w, x-1, y-1);
        state.tileData[7] = parseTileData(w, x,   y-1);
        state.tileData[8] = parseTileData(w, x+1, y-1); 
        
        // Default vals for Q Action-Values
        for (int i = 0; i < Q_ARR_SIZE; i++) {
            state.actionQValues[i] = DEFAULT_VAL;
        }
        
        return state;
    }
    
    /**
     * Resolve QState action index to World action
     * @param action action index
     * @return World action
     */
    public static String resolveToWorldAction(int action) {
        switch (action) {
            case 0:
                return World.A_MOVE;
            case 1:
                return World.A_TURN_LEFT;
            case 2:
                return World.A_GRAB;
            case 3:
                return World.A_CLIMB;
            case 4:
                return World.A_SHOOT;
            default:
                return World.A_MOVE;
        }
    }    
}