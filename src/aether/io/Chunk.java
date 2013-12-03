/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.io;

import java.io.Serializable;

/**
 *
 * @author aniket
 */
public class Chunk implements Serializable {
    
    private String filename;
    private int chunkId;
    private byte[] data;
    
    
    
    /**
     * Get the chunk id of this chunk
     * @return  Chunk id
     */
    public int getChunkId () {
        return chunkId;
    }
    
    
    /**
     * Get the name of the file of which this is a chunk
     * @return 
     */
    public String getFileName () {
        return filename;
    }
    
    
    /**
     * Get the length of data (number of bytes) contained in this chunk
     * @return data size
     */
    public int getDataLength () {
        return data.length;
    }
}
