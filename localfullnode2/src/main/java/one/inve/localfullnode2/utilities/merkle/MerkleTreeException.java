package one.inve.localfullnode2.utilities.merkle;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: MerkleTreeException
 * @Description: wrap all exception occurred in the module
 * @author Francis.Deng
 * @date Aug 20, 2019
 *
 */
public class MerkleTreeException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2130558066127736368L;

	public MerkleTreeException() {
		super();
	}

	public MerkleTreeException(String message) {
		super(message);
	}

	public MerkleTreeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MerkleTreeException(Throwable cause) {
		super(cause);
	}
}
