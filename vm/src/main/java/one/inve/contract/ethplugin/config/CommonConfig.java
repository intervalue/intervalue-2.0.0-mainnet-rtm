/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package one.inve.contract.ethplugin.config;

import one.inve.contract.ethplugin.core.Repository;
import one.inve.contract.ethplugin.crypto.HashUtil;
import one.inve.contract.ethplugin.datasource.*;
import one.inve.contract.ethplugin.datasource.inmem.HashMapDB;
import one.inve.contract.ethplugin.datasource.leveldb.LevelDbDataSource;
import one.inve.contract.ethplugin.datasource.rocksdb.RocksDbDataSource;
import one.inve.contract.ethplugin.db.DbFlushManager;
import one.inve.contract.ethplugin.db.RepositoryRoot;
import one.inve.contract.ethplugin.db.StateSource;
import one.inve.contract.ethplugin.vm.DataWord;
import one.inve.contract.ethplugin.vm.program.ProgramPrecompile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;

import java.util.HashSet;
import java.util.Set;

@Configuration
@ComponentScan(
		basePackages = "one.inve.contract",
        excludeFilters = @ComponentScan.Filter(NoAutoscan.class))
public class CommonConfig {
    private static final Logger logger = LoggerFactory.getLogger("general");
    private Set<DbSource> dbSources = new HashSet<>();

    private static CommonConfig defaultInstance;

    public static CommonConfig getDefault() {
        if (defaultInstance == null && !SystemProperties.isUseOnlySpringConfig()) {
            defaultInstance = new CommonConfig() {
                @Override
                public Source<byte[], ProgramPrecompile> precompileSource() {
                    return null;
                }
            };
        }
        return defaultInstance;
    }

    @Bean
    public SystemProperties systemProperties() {
        return SystemProperties.getSpringDefault();
    }

    @Bean
    public Repository defaultRepository() {
        return new RepositoryRoot(stateSource(), null);
    }

    /**
     * A source of nodes for state trie and all contract storage tries. <br/>
     * This source provides contract code too. <br/><br/>
     *
     * Picks node by 16-bytes prefix of its key. <br/>
     * Within {@link NodeKeyCompositor} this source is a part of ref counting workaround<br/><br/>
     *
     * {@link StateSource} is intended for inner usage only
     *
     * @see NodeKeyCompositor
     * @see RepositoryRoot#RepositoryRoot(Source, byte[])
     */
    @Bean
    public Source<byte[], byte[]> trieNodeSource() {
        DbSource<byte[]> db = blockchainDB();
        Source<byte[], byte[]> src = new PrefixLookupSource<>(db, NodeKeyCompositor.PREFIX_BYTES);
        return new XorDataSource<>(src, HashUtil.sha3("state".getBytes()));
    }

    @Bean
    public StateSource stateSource() {
        StateSource stateSource = new StateSource(blockchainSource("state"),
                systemProperties().databasePruneDepth() >= 0);

        dbFlushManager().addCache(stateSource.getWriteCache());

        return stateSource;
    }

    @Scope("prototype")
    public Source<byte[], byte[]> cachedDbSource(String name) {
        AbstractCachedSource<byte[], byte[]> writeCache = new AsyncWriteCache<byte[], byte[]>(blockchainSource(name)) {
            @Override
            protected WriteCache<byte[], byte[]> createCache(Source<byte[], byte[]> source) {
                WriteCache.BytesKey<byte[]> ret = new WriteCache.BytesKey<>(source, WriteCache.CacheType.SIMPLE);
                ret.withSizeEstimators(MemSizeEstimator.ByteArrayEstimator, MemSizeEstimator.ByteArrayEstimator);
                ret.setFlushSource(true);
                return ret;
            }
        }.withName(name);
        dbFlushManager().addCache(writeCache);
        return writeCache;
    }

    @Scope("prototype")
    public Source<byte[], byte[]> blockchainSource(String name) {
        return new XorDataSource<>(blockchainDbCache(), HashUtil.sha3(name.getBytes()));
    }

    @Bean
    public AbstractCachedSource<byte[], byte[]> blockchainDbCache() {
        WriteCache.BytesKey<byte[]> ret = new WriteCache.BytesKey<>(
                new BatchSourceWriter<>(blockchainDB()), WriteCache.CacheType.SIMPLE);
        ret.setFlushSource(true);
        return ret;
    }

    public DbSource<byte[]> keyValueDataSource(String name) {
        return keyValueDataSource(name, DbSettings.DEFAULT);
    }

    @Scope("prototype")
    @Primary
    public DbSource<byte[]> keyValueDataSource(String name, DbSettings settings) {
        String dataSource = systemProperties().getKeyValueDataSource();
        try {
            DbSource<byte[]> dbSource;
            if ("inmem".equals(dataSource)) {
                dbSource = new HashMapDB<>();
            } else if ("leveldb".equals(dataSource)){
                dbSource = levelDbDataSource();
            } else {
                dataSource = "rocksdb";
                dbSource = rocksDbDataSource();
            }
            dbSource.setName(name);
            dbSource.init(settings);
            dbSources.add(dbSource);
            return dbSource;
        } finally {
            logger.info(dataSource + " key-value data source created: " + name);
        }
    }

    @Bean
    @Scope("prototype")
    protected LevelDbDataSource levelDbDataSource() {
        return new LevelDbDataSource();
    }

    @Bean
    @Scope("prototype")
    protected RocksDbDataSource rocksDbDataSource() {
        return new RocksDbDataSource();
    }

    @Bean
    @Lazy
    public DbSource<byte[]> headerSource() {
        return keyValueDataSource("headers");
    }

    @Bean
    public Source<byte[], ProgramPrecompile> precompileSource() {

        StateSource source = stateSource();
        return new SourceCodec<byte[], ProgramPrecompile, byte[], byte[]>(source,
                new Serializer<byte[], byte[]>() {
                    public byte[] serialize(byte[] object) {
                        DataWord ret = DataWord.of(object);
                        DataWord addResult = ret.add(DataWord.ONE);
                        return addResult.getLast20Bytes();
                    }
                    public byte[] deserialize(byte[] stream) {
                        throw new RuntimeException("Shouldn't be called");
                    }
                }, new Serializer<ProgramPrecompile, byte[]>() {
                    public byte[] serialize(ProgramPrecompile object) {
                        return object == null ? null : object.serialize();
                    }
                    public ProgramPrecompile deserialize(byte[] stream) {
                        return stream == null ? null : ProgramPrecompile.deserialize(stream);
                    }
        });
    }

    @Bean
    public DbSource<byte[]> blockchainDB() {
        DbSettings settings = DbSettings.newInstance()
                .withMaxOpenFiles(systemProperties().getConfig().getInt("database.maxOpenFiles"))
                .withMaxThreads(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));

        return keyValueDataSource("blockchain", settings);
    }

    @Bean
    public DbFlushManager dbFlushManager() {
        return new DbFlushManager(systemProperties(), dbSources, blockchainDbCache());
    }
}
