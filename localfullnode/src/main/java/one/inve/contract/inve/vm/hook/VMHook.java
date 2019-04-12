package one.inve.contract.inve.vm.hook;

import one.inve.contract.ethplugin.vm.OpCode;
import one.inve.contract.inve.vm.program.INVEProgram;

/**
 * Created by Anton Nashatyrev on 15.02.2016.
 */
public interface VMHook {

    default void startPlay(INVEProgram program) {
    }

    default void step(INVEProgram program, OpCode opcode) {
    }

    default void stopPlay(INVEProgram program) {
    }

    default boolean isEmpty() {
        return false;
    }

    VMHook EMPTY = new VMHook() {
        @Override
        public boolean isEmpty() {
            return true;
        }
    };
}
