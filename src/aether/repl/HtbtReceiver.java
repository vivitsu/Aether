package aether.repl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class HtbtReceiver implements Runnable{
	HtbtBuddyMap buddyMap;
	public HtbtReceiver () {
		buddyMap = new HtbtBuddyMap();
		new Thread(buddyMap).start();
	}
	public void run () {
		try {
			DatagramSocket ds = new DatagramSocket(Replication.HTBT_RCV_PORT_NUMBER);				
			byte[] receive = new byte[256];
			DatagramPacket dpr = new DatagramPacket(receive, receive.length);
			while (!Thread.currentThread().isInterrupted()) {
				ds.receive(dpr);
				buddyMap.saveTime (new Host(dpr.getAddress(),dpr.getPort()), new HostDetails (System.currentTimeMillis()));
				String message = new String(receive, 0, dpr.getLength(),
						"UTF-8");
				System.out.println("S : got " + message);
			}			

		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//System.out.println("Retrying ");
			//continue;
		}
	}
}
