package aether.repl;

import java.net.InetAddress;
import java.util.ArrayList;


class ChunkBuddyList {
	 ArrayList<InetAddress> list;
	 public ArrayList getList () {
		 return list;
	 }
	 public void addNode (InetAddress ia) {
		 list.add(ia);
	 }
}

