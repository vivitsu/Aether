package aether.client;


import aether.io.Chunk;

import java.util.Comparator;

/**
 * Implements a chunk comparator that compares the IDs of two chunks.
 * This is needed when assembling the chunks so that the sorted chunks
 * present the natural ordering of the file while merging.
 */
public class ChunkComparator implements Comparator<Chunk> {

    @Override
    public int compare(Chunk c1, Chunk c2) {

        if (c1.getChunkId() < c2.getChunkId()) {
            return -1;
        } else if (c1.getChunkId() > c2.getChunkId()) {
            return 1;
        } else {
            return 0;
        }
    }
}
