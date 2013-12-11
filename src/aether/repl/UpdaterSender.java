package aether.repl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import aether.cluster.ClusterMgr;
import aether.cluster.ClusterTable;
import aether.cluster.ClusterTableRecord;
import aether.net.NetMgr;

public class UpdaterSender implements Runnable {
	private static final int UPD_SND_PORT = 33990;
	

	public void run() {
		ClusterMgr cm;
		try {
			cm = ClusterMgr.getInstance();
			ClusterTableRecord[] ctr = cm.getClusterTableRecords();
			
			FileChunkMetadata fcm = FileChunkMetadata.getInstance();
			ArrayList<String> fileList = fcm.getFileList();			
			
			for (int i = 0; i < ctr.length; i++) {
				Socket s = new Socket (ctr[i].getNodeIp(), UpdaterSender.UPD_SND_PORT);
				
			}
			
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	

}
