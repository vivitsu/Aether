package aether.repl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This is the listener service of the replication
 * manager. It continuously 
 * */
public class ReplicationListener implements Runnable {
	public void run () {
		try {
			ServerSocket socketListener = new ServerSocket (Replication.REPL_PORT_LISTENER);
			Socket s = socketListener.accept();
			InputStream is = s.getInputStream();
			FileOutputStream fos = new FileOutputStream (new File ("")); //TODO what is the file name
			byte[] buffer = new byte[2048];
			
			int read = 0;
			int offset = 0;
			
			while ((read = is.read(buffer)) != -1) {
				fos.write(buffer, offset, read - offset);
				offset = 0;
			}
			
			fos.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
