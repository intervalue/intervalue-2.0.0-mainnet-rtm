package one.inve.contract.stateworld;

import one.inve.contract.ethplugin.trie.TrieImpl;

/**
 * MPT 树的工厂方法
 * @author 肖毅
 * @since 2018-11-15
 */
public class TrieFactory {

    private static TrieImpl STATE_TRIE;

    public static TrieImpl getStateTrieInstance() {
        if(STATE_TRIE == null) {
            STATE_TRIE = new TrieImpl();
        }
        return STATE_TRIE;
    }
}
