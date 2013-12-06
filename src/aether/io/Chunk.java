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
    private String chunkName;
    
    
    
    
    
    /**
     * Chunk class represents a chunk of the file which can be replicated.
     * There is small metadata kept with each chunk which includes the filename,
     * and the chunk id. The chunk is serializable hence can be sent easily over
     * the network.
     * @param file  The name of the file of which this is a chunk
     * @param id    The id of the chunk. Unique in the file
     * @param data  Actual file data of max size chunksize.
     */
    public Chunk (String file, int id, byte[] data) {
        
        filename = file;
        chunkId = id;
        this.data = data.clone();
        chunkName = file + chunkId;
    }
    
    
    
    
    /**
     * Such a constructor can be extended to store metadata without actual data
     * @param file
     * @param id 
     */
    public Chunk (String file, int id) {
        filename = file;
        chunkId = id;
        chunkName = file + chunkId;
    }
    
    
    
    
    
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
    
    
    /**
     * Get the name of the chunk
     * @return  Chunk name
     */
    public String getChunkName () {
        return chunkName;
    }
    
    
    /**
     * Retrieve the data bytes in this chunk
     * @return  array of data bytes
     */
    public byte[] getData () {
        return data;
    }
}
