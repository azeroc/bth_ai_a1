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
    public static final int BYTE_SIZE = 66; // 1 + 1 + 16 + 6*8
    public static final int KEY_SIZE = 18;  // 1 + 1 + 16;
    
    // Default Q-value indicating that it is uninitialized
    public static final Double DEFAULT_VAL = Double.NEGATIVE_INFINITY;
    
    // Tile state bitmask flags
    public static final byte UNEXPLORED = 0;  // 0000 0000
    public static final byte EXPLORED   = 1;  // 0000 0001
    public static final byte BREEZE     = 2;  // 0000 0010
    public static final byte STENCH     = 4;  // 0000 0100
    public static final byte PIT        = 8;  // 0000 1000    
    public static final byte WUMPUS     = 16; // 0001 0000
    public static final byte GLITTER    = 32; // 0010 0000     
    
    // QState's parsed data    
    public byte hasFallenIntoPit; // Fallen-Into-Pit flag
    public byte playerD; // Player Direction (see: Player Directions constants)
    public byte playerX; // Player X axis coord (1..4)
    public byte playerY; // Player Y axis coord (1..4)
    public byte hasArrow; // Player arrow flag bit
    public byte tileData[]; // 16 tile data
    public Double actionQValues[]; // 6 Doubles for the Q values of 6 actions
    
    // QEntry's key for QTable
    // Combination of parsed data into string with exception of actionQValues
    public String getKey() {
        byte[] keyBuf = new byte[KEY_SIZE];        
        int keyItr = 0;
        
        // HasFallenIntoPit, PlayerDirection
        keyBuf[keyItr++] = (byte)((this.hasFallenIntoPit << 3) | this.playerD); 
        // HasArrow, PlayerY, PlayerX
        keyBuf[keyItr++] = (byte) ((this.hasArrow << 6) | (this.playerY << 3) | this.playerX); 
        // TileData
        for (int i = 0; i < 16; i++) {
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
        res.tileData = new byte[16];
        res.actionQValues = new Double[6];
        int byteItr = 0;      
        
        // PlayerDirection
        res.hasFallenIntoPit = (byte)((bytes[byteItr] >> 3) & 1);
        res.playerD = (byte)(bytes[byteItr] & 7);
        byteItr++;
        
        // PlayerX, PlayerY, HasArrow
        // Stored bites in the byte: 0AYY YXXX
        res.playerX = (byte)(bytes[byteItr] & 7);
        res.playerY = (byte)((bytes[byteItr] >> 3) & 7);
        res.hasArrow = (byte)((bytes[byteItr] >> 6) & 1);
        byteItr++;
        
        // Tile data
        for (int i = 0; i < 16; i++) {
            res.tileData[i] = bytes[byteItr++];
        }
        
        // Action Q Values
        for (int j = 0; j < 6; j++) {          
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
        
        // HasFallenIntoPit, PlayerDirection
        res[resItr++] = (byte)((state.hasFallenIntoPit << 3) | state.playerD);  
        // HasArrow, PlayerY, PlayerX
        res[resItr++] = (byte) ((state.hasArrow << 6) + (state.playerY << 3) + state.playerX); 
        // TileData
        for (int i = 0; i < 16; i++) {
            res[resItr++] = state.tileData[i];
        }
        // Action Q Values
        for (int j = 0; j < 6; j++) {
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
        
        for (int i = 0; i < 6; i++) {
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
        
        for (int i = 0; i < 6; i++) {
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
        Double[] values = new Double[6];
        for (int i = 0; i < 6; i++) {
            Double val = this.actionQValues[i];
            val = (val.equals(DEFAULT_VAL)) ? 0.0 : val;
            values[i] = val;
        }
        return values;
    }
    
    /**
     * Resolve QState action index to World action
     * @param action action index
     * @return World action
     */
    public static String resolveToWorldAction(int action) {
        switch (action) {
            case 0:
                return World.A_TURN_LEFT;
            case 1:
                return World.A_MOVE;
            case 2:
                return World.A_TURN_RIGHT;
            case 3:
                return World.A_GRAB;
            case 4:
                return World.A_CLIMB;
            case 5:
                return World.A_SHOOT;
            default:
                return World.A_MOVE;
        }
    }    
}