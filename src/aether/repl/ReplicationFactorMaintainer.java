package aether.repl;

/**
 * This is called when the heartbeat thread detects
 * that a node has failed. In this case, we extract
 * data from ChunkDistribution data structure about 
 * which nodes were stored at that chunk and 
 * start replicating these chunks
 * */
public class ReplicationFactorMaintainer {
	public ReplicationFactorMaintainer () {
		
	}	
}
