package one.inve.localfullnode2.utilities.merkle;

/**
 * 
* Copyright © INVE FOUNDATION. All rights reserved.
   
* @ClassName: INodeContent    
* @Description: represent the data that is stored in the node 
* @author Francis.Deng    
* @date Aug 20, 2019    
*
 */
public interface INodeContent {
	byte[] hash();
	boolean equals(INodeContent content);
}
