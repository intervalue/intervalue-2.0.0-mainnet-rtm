package one.inve.localfullnode2.utilities.rocks;

import java.util.ArrayList;
import java.util.List;

import org.rocksdb.ColumnFamilyDescriptor;
import org.rocksdb.ColumnFamilyHandle;
import org.rocksdb.ColumnFamilyOptions;
import org.rocksdb.DBOptions;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.localfullnode2.utilities.PathUtils;

/**
 * 
 * Copyright © INVE FOUNDATION. All rights reserved.
 * 
 * @ClassName: RocksDBValueByKey
 * @Description: troubleshooting class which provides a approach to get values
 *               by keys in rocksdb
 *               <p>
 *               java -classpath localfullnode2-2.0.0.jar
 *               one.inve.localfullnode2.utilities.rocks.RocksDBValueByKey 0_0
 *               33AOWttvUncH5ZtU3Jn/bbr4dte7j6BPq7hCxS2TMPIf4OSIJzWbN8UKDZPInQAID9m11XmVnHsmThhyUrpwwYuCc=
 *               <p>
 *               java -classpath localfullnode2-2.0.0.jar
 *               one.inve.localfullnode2.utilities.rocks.RocksDBValueByKey 0_1
 *               'smsgs$t$transaction_fee_tx1'
 *               <p>
 *               output:Result is byte[0]:smsgs$t$transaction_fee_tx1=
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Jan 19, 2020
 *
 */
public class RocksDBValueByKey {

	private static final Logger logger = LoggerFactory.getLogger(RocksDBValueByKey.class);

	protected static RocksDB rocksDB;

	public static void main(String[] args) {
		if (args.length > 1 && startRocksDB(args[0])) {
			try {
				for (int index = 1; index < args.length; index++) {
					String arg = args[index];
					byte[] valueBytes = rocksDB.get(arg.getBytes());

					if (valueBytes != null && valueBytes.length > 0) {
						System.out.println("Result is:" + arg + "=" + new String(valueBytes));
					} else {
						if (valueBytes == null) {
							System.out.println("Result is null:" + arg + "=");
						} else if (valueBytes.length == 0) {
							System.out.println("Result is byte[0]:" + arg + "=");
						}
					}
				}

			} catch (RocksDBException e) {
				logger.error("error: %s", e.toString());
				e.printStackTrace();
			}

			closeRocksDB();
		} else {
			logger.info(
					"usage: java -classpath localfullnode2-2.0.0.jar one.inve.localfullnode2.utilities.rocks.RocksDBValueByKey [dbId] [key1] [key2]...");
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
