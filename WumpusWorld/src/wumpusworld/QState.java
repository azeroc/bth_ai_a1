/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wumpusworld;

import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Azeroc
 */
public class QState {
    public static final int BYTE_SIZE = 66; // 1 + 1 + 16 + 6*8
    public static final int KEY_SIZE = 18;  // 1 + 1 + 16;
    
    // Tile state bitmask flags
    public static final byte UNEXPLORED = 0;  // 0000 0000
    public static final byte EMPTY      = 1;  // 0000 0001
    public static final byte BREEZE     = 2;  // 0000 0010
    public static final byte STENCH     = 4;  // 0000 0100
    public static final byte PIT        = 8;  // 0000 1000    
    public static final byte WUMPUS     = 16; // 0001 0000
    public static final byte GLITTER    = 32; // 0010 0000     
    
    // QState's parsed data    
    public byte playerD; // Player Direction (1 = Right, 2 = Up, 3 = Left, 4 = Down)
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
        
        // PlayerDirection
        keyBuf[keyItr++] = this.playerD; 
        // HasArrow, PlayerY, PlayerX
        keyBuf[keyItr++] = (byte) ((this.hasArrow << 7) + (this.playerY << 3) + this.playerX); 
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
        res.playerD = bytes[byteItr++];
        
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
        
        // PlayerDirection
        res[resItr++] = state.playerD; 
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
}