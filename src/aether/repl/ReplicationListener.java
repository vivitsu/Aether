package aether.repl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import aether.io.Chunk;

/**
 * This is the listener service of the replication
 * manager. It continuously 
 * */
public class ReplicationListener implements Runnable {
	public void run () {
		try {
			/*ServerSocket socketListener = new ServerSocket (Replication.REPL_PORT_LISTENER);
			Socket s = socketListener.accept();
			InputStream is = s.getInputStream();
			FileOutputStream fos = new FileOutputStream (new File ("tempfile")); 
			byte[] buffer = new byte[2048];
			
			int read = 0;
			int offset = 0;
			
			while ((read = is.read(buffer)) != -1) {
				fos.write(buffer, offset, read - offset);
				offset = 0;
			}
			
			fos.close();*/
			
			ServerSocket socketListener = new ServerSocket (Replication.REPL_PORT_LISTENER);
			while (!Thread.currentThread().isInterrupted()) {
				Socket s = socketListener.accept();
				InputStream is = s.getInputStream();
				Chunk c;
				ObjectInputStream ois = new ObjectInputStream (is);
				c = (Chunk) ois.readObject();
				FileOutputStream fos = new FileOutputStream (new File(c.getChunkName()));
				ObjectOutputStream oos = new ObjectOutputStream (fos);
				oos.writeObject(c);
				oos.close();
				ois.close();				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
