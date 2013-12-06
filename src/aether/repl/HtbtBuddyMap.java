package aether.repl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
/**
 * Data structure that stores the chunk buddies 
 * of a cluster node. A heartbeat with each of the 
 * entries in this data structure is maintained
 * 
 * */
public class HtbtBuddyMap implements Runnable {
	Map<Host, HostDetails> clusterNodes;
	public HtbtBuddyMap () {
		clusterNodes = new HashMap<Host, HostDetails> ();
	}
	public Set<Map.Entry<Host, HostDetails>> getBuddyMapSet () {
		return clusterNodes.entrySet();
	}
	public void put (Host h, HostDetails hd) {
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
						//Replace with logic of what to do when dead
						System.out.println("Port " + entry.getKey() + " dead");
					}
				}
			}
			try {
				Thread.currentThread().sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void saveTime (Host h, HostDetails hd) {
		HostDetails oldDetail;
		if ( clusterNodes.get(h) != null ) {
			oldDetail = clusterNodes.get(h);
			System.out.println("Saving port + time: " + " " + (hd.getTime()- oldDetail.getTime()));
		}
		clusterNodes.put(h, hd);
	}
	

}
