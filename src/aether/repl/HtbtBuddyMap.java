package aether.repl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Data structure that stores the chunk buddies 
 * of a cluster node. A heartbeat with each of the 
 * entries in this data structure is maintained
 * 
 * */
public class HtbtBuddyMap implements Runnable {
	private static HtbtBuddyMap hbm; 
	Map<Host, HostDetails> clusterNodes;
	public HtbtBuddyMap () {
		clusterNodes = new ConcurrentHashMap<Host, HostDetails> ();
	}
	
	public synchronized static HtbtBuddyMap getInstance () {
		if (hbm == null) {
			hbm = new HtbtBuddyMap ();
		}
		
		return hbm;
	}
	public synchronized Set<Map.Entry<Host, HostDetails>> getBuddyMapSet () {
		return clusterNodes.entrySet();
	}
	public synchronized void put (Host h, HostDetails hd) {
		clusterNodes.put(h, hd);
	}
	public HostDetails get (Host h) {
		return clusterNodes.get(h);
	}	
	public void run() {
		while (true) {
			long difference;
			for (Entry<Host, HostDetails> entry : clusterNodes.entrySet()) {
				synchronized (entry) {
					difference = System.currentTimeMillis() - entry.getValue().getTime();
					if ((difference / 1000) > Replication.TIMEOUT_BEFORE_DEAD) {
						
						//remove entry from the heartbeat metadata data structure
						clusterNodes.remove(entry.getKey());
						

						CD cd = CD.getInstance();
						cd.removeChunk(entry.getKey());
						
						System.out.println("Port " + entry.getKey() + " dead");
					}
				}
			}
			try {
				Thread.currentThread().sleep(5000);
			} catch (InterruptedException e) {
				System.out.println("Interrupted exception at HtbtBuddy Map");
				e.printStackTrace();
			}
		}
	}
	public synchronized void saveTime (Host h, HostDetails hd) {
		HostDetails oldDetail;
		if ( clusterNodes.get(h) != null ) {
			oldDetail = clusterNodes.get(h);
			System.out.println("Saving port + time: " + " " + (hd.getTime()- oldDetail.getTime()));
		}
		clusterNodes.put(h, hd);
	}
	

}
