package aether.repl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map.Entry;

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
