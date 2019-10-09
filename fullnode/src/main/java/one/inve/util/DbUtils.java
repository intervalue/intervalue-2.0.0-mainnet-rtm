package one.inve.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import one.inve.cfg.fullnode.Config;

/**
 * 数据库操作工具包
 */
public class DbUtils {
	public final static Logger logger = Logger.getLogger(DbUtils.class);

	public static void createDatabase(String realPath, String realDbName) {
		File dbfile = new File(realPath + realDbName);
		if (!dbfile.exists()) {
			File out = new File(realPath); // 目标文件夹
			// 生成新数据库文件
			if (!out.exists() && !out.isDirectory()) {
				if (!out.mkdirs()) {
					logger.error("create cache dir failed!!! exit...");
					System.exit(-1);
				}
			}
			// 判断是否为sqlite文件
			try {
				Files.copy(getResource("classpath:" + Config.INIT_SQLITE_FILE), dbfile.toPath(),
						StandardCopyOption.REPLACE_EXISTING); // 重命名并且复制到out文件夹
			} catch (IOException e) {
				logger.error("createDatabase error", e);
				if (dbfile.exists()) {
					dbfile.delete();
				}
				System.exit(0);
			}
		}
	}

	private static InputStream getResource(String location) throws IOException {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		InputStream in = resolver.getResource(location).getInputStream();
		byte[] byteArray = IOUtils.toByteArray(in);
		in.close();
		return new ByteArrayInputStream(byteArray);
	}
}
