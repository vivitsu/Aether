package aether.repl;

import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import aether.cluster.ClusterTable;
import aether.cluster.ClusterTableRecord;
import aether.net.NetMgr;

public class UpdaterSender implements Runnable {
	private static final int UPD_SND_PORT = 33990;
	

	public void run() {
		ClusterTable ct = ClusterTable.getInstance ();
		ClusterTableRecord[] ctr = ct.getAllRecords();
		
		FileChunkMetadata fcm = FileChunkMetadata.getInstance();
		ArrayList<String> fileList = fcm.getFileList();
		
		InetAddress localAddress = NetMgr.getLocalIp();
		for (int i = 0; i < ctr.length; i++) {
			Socket s = new Socket (ctr[i].getNodeIp(), UpdaterSender.UPD_SND_PORT);
		}
		
		
	}
	
	

}
