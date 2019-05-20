package one.inve.contract.inve.vm.program.invoke;

import one.inve.contract.ethplugin.core.Block;
import one.inve.contract.ethplugin.core.Repository;
import one.inve.contract.ethplugin.core.Transaction;
import one.inve.contract.ethplugin.db.BlockStore;
import one.inve.contract.ethplugin.vm.DataWord;
import one.inve.contract.inve.vm.program.INVEProgram;

import java.math.BigInteger;

/**
 * @author Roman Mandeleil
 * @since 19.12.2014
 */
public interface INVEProgramInvokeFactory {

    INVEProgramInvoke createProgramInvoke(Transaction tx, Block block,
                                          Repository repository, BlockStore blockStore);

    INVEProgramInvoke createProgramInvoke(INVEProgram program, DataWord toAddress, DataWord callerAddress,
                                          DataWord inValue, DataWord inGas,
                                          BigInteger balanceInt, byte[] dataIn,
                                          Repository repository, BlockStore blockStore,
                                          boolean staticCall, boolean byTestingSuite);
}
