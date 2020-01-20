package one.inve.localfullnode2.utilities.rocks;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.Options;
import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.localfullnode2.utilities.PathUtils;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: RocksDBAllPrinter
 * @Description: troubleshooting class which provides a approach to printing all
 *               records in rocksdb
 *               <p>
 *               java -classpath localfullnode2-2.0.0.jar
 *               one.inve.localfullnode2.utilities.rocks.RocksDBAllPrinter 0_0
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 19, 2020
 *
 */
public class RocksDBAllPrinter {

	private static final Logger logger = LoggerFactory.getLogger(RocksDBAllPrinter.class);

	protected static RocksDB rocksDB;

	public static void main(String[] args) {
		if (args.length > 0 && startRocksDB(args[0])) {
			Map<String, String> m = new HashMap<>();
			ReadOptions ro = new ReadOptions();
			RocksIterator iter = rocksDB.newIterator(ro);
			BufferedOutputStream buff;
			try {
				File file = new File("rocksdb.output");
				if (file.exists()) {
					file.delete();
				}

				buff = new BufferedOutputStream(new FileOutputStream("rocksdb.output"));
				for (iter.seekToFirst(); iter.isValid(); iter.next()) {
					buff.write(iter.key());
					buff.write("--".getBytes());
					buff.write(iter.value());
					buff.write("\r\n".getBytes());
				}

				buff.flush();
				buff.close();
			} catch (IOException e) {
				logger.error("error: %s", e.toString());
				e.printStackTrace();
			}

			closeRocksDB();
			logger.info("check out the file('rocksdb.output') in current directory");
		} else {
			logger.info(
					"usage: java -classpath localfullnode2-2.0.0.jar one.inve.localfullnode2.utilities.rocks.RocksDBAllPrinter [dbId]");
		}

	}

	private static boolean startRocksDB(String dbId) {
		try {
			String rocksDBPath = PathUtils.getDataFileDir() + dbId;
			RocksDB.loadLibrary();

			List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
			Options options = new Options();
			options.setCreateIfMissing(true);

			List<byte[]> cfs = RocksDB.listColumnFamilies(options, rocksDBPath);
			if (cfs.size() > 0) {
				for (byte[] cf : cfs) {
					columnFamilyDescriptors.add(new ColumnFamilyDescriptor(cf, new ColumnFamilyOptions()));
				}
			} else {
				columnFamilyDescriptors
						.add(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, new ColumnFamilyOptions()));
			}

			List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
			DBOptions dbOptions = new DBOptions();
			dbOptions.setCreateIfMissing(true);

			rocksDB = RocksDB.open(dbOptions, rocksDBPath, columnFamilyDescriptors, columnFamilyHandles);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return false;
		}

		return true;

	}

	public static void closeRocksDB() {
		rocksDB.close();
	}

}
