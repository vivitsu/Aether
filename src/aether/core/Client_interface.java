package aether.core;

import aether.cluster.ClusterMgr;
import aether.conf.ConfigMgr;
import java.net.SocketException;
import java.util.HashMap;

public class Client_interface {
	
	public static void Write_request(String file)
	{
		/*
		 * This function will be initiated on the write 
		 * request from client and will call the cluster 
		 * manager after creating chunks of the input file
		 */
		//call the create_chunks function for this file
	}
	

	public static void read_request(String file)
	{
		/*
		 * This function will be initiated on the read 
		 * request from client and will call the cluster 
		 * manager to get chunks of requested file
		 */
		//call the create_file function for this file
	}
	
	public HashMap<String,String> create_chunks(String file)
	{
		
		HashMap<String,String> chunk_map = new HashMap<String,String>();
		
		/*
		 * This function creates chunks of file and returns 
		 * the details in a hashmap to the requesting function.
		 * 
		 * 
		 */
		return  chunk_map;
		
	}
	
	public String create_file(HashMap<String,String> chunk_map)
	{
		String file = new String();
		/*
		 * This functions aggregates all the chunks received from the cluster, 
		 * generates a single file and returns it to the requested client.
		 */
		
		return file;
	}
}
