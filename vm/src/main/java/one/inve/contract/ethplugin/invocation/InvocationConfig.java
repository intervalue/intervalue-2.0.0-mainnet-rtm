package one.inve.contract.ethplugin.invocation;

import one.inve.contract.ethplugin.config.CommonConfig;
import one.inve.contract.ethplugin.config.DefaultConfig;
import one.inve.contract.ethplugin.core.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.io.File;
import java.io.IOException;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: has power of eth via {@link DefaultConfig},{@link CommonConfig}
 * @author: Francis.Deng
 * @date: 2018年11月23日 上午9:39:49
 * @version: V1.0
 */
@Configuration
@Import(DefaultConfig.class)
public class InvocationConfig {
	@Autowired
	private ApplicationContext ctx;

	@Bean
	public Appendable getPersistableByteBuf() throws IOException {
		int sizeOf128M = 0x8FFFFFF;
		// PersistableByteBuf pbb = new PersistableByteBuf(sizeOf128M,
		// "mapping-buf");
		// File directory = new File("mapping-buf");
		File directory = new File(getMappingBufferDir());
		OnlyOneRetainmentBinFile pbb = new OnlyOneRetainmentBinFile(directory.getCanonicalPath());
		// pbb.initialize();

		return pbb;
	}

	@Bean
	public Repository defaultInvocationRepository() throws IOException {
		Repository r = ctx.getBean("defaultRepository", Repository.class);
		// byte[] bytes = getPersistableByteBuf().readLast();
		byte[] bytes = getPersistableByteBuf().readFirst();

		return r.getSnapshotTo(bytes).startTracking();
	}

	// -Dmapping.buf.dir=configurePath designates $configurePath/mapping-buf
	protected String getMappingBufferDir() {
		String mappingBufferDir = System.getProperty("mapping.buf.dir", ".");
		if (!new File(mappingBufferDir).exists()) {
			new File(mappingBufferDir).mkdirs();
		}
		if (!mappingBufferDir.substring(mappingBufferDir.length() - 1).equals(File.separator)) {
			mappingBufferDir = mappingBufferDir + File.separator + "mapping-buf";
		} else {
			mappingBufferDir = mappingBufferDir + "mapping-buf";
		}

		return mappingBufferDir;
	}
}
