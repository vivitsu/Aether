/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package aether.cluster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author aniket
 */
public class ClusterTable {
    
    
    HashMap<Integer,ClusterTableRecord> table;
    private static int numTables = 0;
    
    
    /**
     * ClusterTable stores the information about the members of the cluster
     * For each node in the cluster, it stores the identifier of the node
     * and its IP address (more fields can be added as necessary)
     */
    public ClusterTable () {
        
        table = new HashMap<>();
        numTables++;
    }
    
    
    
    
    
    /**
     * Insert the entry in the cluster table for a node.
     * @param rec   Cluster Table Record for the node
     * @throws UnsupportedOperationException 
     */
    public void insertRecord (ClusterTableRecord rec) throws 
            UnsupportedOperationException {
        
        Integer key = new Integer(rec.getNodeId());
        
 
        if (table.containsKey(key)) {
            /* This should go to log. We need to change this after deciding
             * what logger to use.
             */
            System.err.println("Node entry for node " + key.toString()
                    + " already exists in the cluster table");
            throw new UnsupportedOperationException();
        }
        
        table.put(key, rec);
    }
    
    
    
    
    /**
     * Check if cluster table has a record for given node id
     * @param nodeId
     * @return boolean true if record exists, false otherwise
     */
    public boolean exists (int nodeId) {
        return table.containsKey(nodeId);
    }
    
    
    
    /**
     * Retrieve the record for node with id nodeId.
     * @param nodeId    Identifier of the node
     * @return  Cluster Table Record having the entry for the node
     *          null, if not present.
     */
    public ClusterTableRecord getRecord (int nodeId) {
        
        Integer key = new Integer(nodeId);
        if (table.containsKey(key) == false) {
            return null;
        } else {
            return table.get(key);
        }
            
    }
    
    
    
    
    /**
     * Delete the record for the node with id nodeId
     * @param nodeId    Identifier of the node for which record is to be
     *                  deleted
     */
    public void deleteRecord (int nodeId) {
        
        Integer key = new Integer(nodeId);
        if (table.containsKey(key)) {
            table.remove(key);
        }
    }
    
    
    
    
    /**
     * Print the cluster table on stdout.
     */
    public void printTable () {
        Set<Integer> keys = table.keySet();
        Iterator<Integer> it = keys.iterator();
        
        while (it.hasNext()) {
            System.out.print(table.get(it.next()).toString());
        }
    }
    
    
    /**
     * Return the number of records in the cluster table
     * @return  Int containing number of records in the table
     */
    public int getNumRecords () {
        return table.size();
    }
    
    
    /**
     * Get all the records in the cluster table
     * @return  Array of all cluster table records
     */
    public ClusterTableRecord[] getAllRecords () {
        
        int tableSize = getNumRecords();
        ClusterTableRecord[] allRecords = new ClusterTableRecord[tableSize];
        
        int i=0;
        for (Integer id: table.keySet()) {
            ClusterTableRecord rec = table.get(id);
            allRecords[i++] = rec;
        }
        
        return allRecords;
    }
    
    
    
}
