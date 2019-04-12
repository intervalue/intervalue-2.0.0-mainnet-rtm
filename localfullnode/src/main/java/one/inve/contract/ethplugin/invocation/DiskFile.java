package one.inve.contract.ethplugin.invocation;

import java.io.File;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: In reality,the class is abandoned.
 * @author: Francis.Deng
 * @date: 2018年12月6日 上午11:42:09
 * @version: V1.0
 */
public abstract class DiskFile implements Appendable {
	private String file;

	DiskFile(String file) {
		this.file = file;
	}

	public boolean isPrehistoric() {
		File f = new File(file);
		return !f.exists();
	}

}
