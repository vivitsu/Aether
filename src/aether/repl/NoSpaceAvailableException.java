package aether.repl;

/**
 * This exception is thrown when searching for a node
 * with available space to store the file chunk. If 
 * no node in the cluster has space for the file chunk,
 * this exception is thrown. 
 * */
class NoSpaceAvailableException extends Exception {
	public NoSpaceAvailableException () {
		
	}
	public NoSpaceAvailableException (String message) {
		super(message);
	}
	
}
