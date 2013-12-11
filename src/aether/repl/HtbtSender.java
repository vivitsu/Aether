package aether.repl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class HtbtSender implements Runnable {
	HtbtBuddyMap buddymap;
	public HtbtSender (HtbtBuddyMap hbm) {
		buddymap = hbm;
	}
	public void setBuddyMap (HtbtBuddyMap hbm) {
		buddymap = hbm;
	}
	public void run () {
		DatagramSocket ds;
		try {
			ds = new DatagramSocket(Replication.HTBT_SND_PORT_NUMBER);
			byte[] send = new byte[256];
			while (true) {

				for (Entry<Host, HostDetails> entry : buddymap.getBuddyMapSet()) {

					String mess = "from Sender";// + Integer.toString(port);
					send = mess.getBytes("UTF-8");
					DatagramPacket dps = new DatagramPacket(send, send.length,
							entry.getKey().getIPAddress(), entry.getKey().getPort());
					System.out.println(entry.getKey().getIPAddress());
					ds.send(dps);
				}
				System.out.println("Sent from sender");
				
				
				////////////////////sender start
				FileChunkMetadata fcm = FileChunkMetadata.getInstance();
				Set<Map.Entry<String, ArrayList<ChunkMetadata>>> entryFiles = fcm.getFileChunkMetadataSet();
					
				for (Entry<Host, HostDetails> entryHost : buddymap.getBuddyMapSet()) {
					Host h = entryHost.getKey();
					ArrayList<String> fileList = fcm.getFileList();
					Socket s = new Socket (h.getIPAddress(), UpdaterReceiver.UPD_RCV_PORT);
					
					ObjectOutputStream oos = new ObjectOutputStream (s.getOutputStream());
					oos.writeObject(fcm);
					
				}
				/////////////////////////////sender end
				
				////////////////////////////receiver start
				
				ServerSocket ss = new ServerSocket (UpdaterReceiver.UPD_RCV_PORT);
				Socket sock = ss.accept();
				ObjectInputStream ois = new ObjectInputStream (sock.getInputStream());
				try {
					FileChunkMetadata fcmeta = FileChunkMetadata.getInstance();
					FileChunkMetadata buddyMetadata = (FileChunkMetadata) ois.readObject();
					for (Entry<String, ArrayList<ChunkMetadata>> entry : buddyMetadata.getFileChunkMetadataSet()) {
						if (fcmeta.fileChunkMap.containsKey(entry.getKey())) {
							ArrayList<ChunkMetadata> cmlist = fcmeta.fileChunkMap.get(entry.getKey());
							cmlist.addAll(entry.getValue());
							
						}
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				////////////////////////////receiver end
				
				Thread.currentThread().sleep(5000);
			}
		} catch (SocketException e) {
			System.out.println("Socket Exception at HtbtSender");
			e.printStackTrace();
		} catch (UnknownHostException e) {
			System.out.println("Unknown host exception at Htbt Sender");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			System.out.println("Unsupported Encoding exception at Htbt Sender");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException at Htbt sender");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Interrupted exception at Htbt Sender");
			e.printStackTrace();
		}
		
	}
}
