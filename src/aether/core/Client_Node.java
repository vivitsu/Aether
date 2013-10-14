package aether.core;

public class Client_Node {
	
	Client_interface client_int;
	
	public void Client_node()
	
	{
		client_int = new Client_interface();
	}
	
	public void read_request(String file)
	{
		/*
		 * This function makes a read request to the client interface 
		 * for the file passed in parameters
		 */
		client_int.read_request(file);
	}
	
	public void Write_request(String file)
	{
		/*
		 * This function makes a write request to the client interface 
		 * for the file passed in parameters
		 */
		client_int.Write_request(file);
	}

}
