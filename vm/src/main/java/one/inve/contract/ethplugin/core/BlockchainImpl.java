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
package one.inve.contract.ethplugin.core;

import one.inve.contract.ethplugin.config.CommonConfig;
import one.inve.contract.ethplugin.config.SystemProperties;
import one.inve.contract.ethplugin.crypto.HashUtil;
import one.inve.contract.ethplugin.db.BlockStore;
import one.inve.contract.ethplugin.db.DbFlushManager;
import one.inve.contract.ethplugin.db.PruneManager;
import one.inve.contract.ethplugin.db.StateSource;
import one.inve.contract.ethplugin.trie.Trie;
import one.inve.contract.ethplugin.trie.TrieImpl;
import one.inve.contract.ethplugin.util.RLP;
import one.inve.contract.ethplugin.vm.program.invoke.ProgramInvokeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BlockchainImpl implements Blockchain, one.inve.contract.ethplugin.facade.Blockchain {

    @Autowired @Qualifier("defaultRepository")
    private Repository repository;

    @Autowired
    protected BlockStore blockStore;

    @Autowired
    ProgramInvokeFactory programInvokeFactory;

    @Autowired
    EventDispatchThread eventDispatchThread;

    @Autowired
    CommonConfig commonConfig = CommonConfig.getDefault();

    @Autowired
    PruneManager pruneManager;

    @Autowired
    StateSource stateDataSource;

    @Autowired
    DbFlushManager dbFlushManager;

    SystemProperties config;

    @Autowired
    public BlockchainImpl(final SystemProperties config) {
        this.config = config;
    }

    public static byte[] calcTxTrie(List<Transaction> transactions) {

        Trie txsState = new TrieImpl();

        if (transactions == null || transactions.isEmpty())
            return HashUtil.EMPTY_TRIE_HASH;

        for (int i = 0; i < transactions.size(); i++) {
            txsState.put(RLP.encodeInt(i), transactions.get(i).getEncoded());
        }
        return txsState.getRootHash();
    }

    public Repository getRepository() {
        return repository;
    }

    @Override
    public BlockStore getBlockStore() {
        return blockStore;
    }

    @Override
    public synchronized void close() {
        blockStore.close();
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

}
