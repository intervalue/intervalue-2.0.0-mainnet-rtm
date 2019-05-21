package one.inve.contract.sample;

import one.inve.contract.ethplugin.config.SystemProperties;
import one.inve.contract.ethplugin.core.Transaction;
import one.inve.contract.ethplugin.db.BlockStoreDummy;
import one.inve.contract.inve.INVETransactionExecutor;
import one.inve.contract.inve.INVETransactionReceipt;
import one.inve.contract.inve.vm.program.invoke.INVEProgramInvokeFactory;
import one.inve.contract.inve.vm.program.invoke.INVEProgramInvokeFactoryImpl;
import one.inve.contract.provider.RepositoryProvider;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class TransactionTest {
    private byte[] senderAddress;

    public TransactionTest(String dbId) {
        RepositoryProvider.getTrack(dbId);
        senderAddress = "VZLSZWP6ASHOYEXWC3VIIR4JOXYN32HH".getBytes();
    } 

    public static void main(String[] args) throws IOException {
        // =================================================================
        // 构造测试中用到的合约及数据
        // =================================================================

        // A: 构造函数带固定参数的合约
        String fixedContract = "608060405234801561001057600080fd5b506040516020806103d983398101806040528101908080519060200190929190505050806002819055505061038f8061004a6000396000f300608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680633ccfd60b1461005c57806367e0badb146100735780637365870b1461009e575b600080fd5b34801561006857600080fd5b506100716100be565b005b34801561007f57600080fd5b5061008861020d565b6040518082815260200191505060405180910390f35b6100bc60048036038101908080359060200190929190505050610217565b005b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561011b57600080fd5b670de0b6b3a76400003073ffffffffffffffffffffffffffffffffffffffff16310390506051600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205414156101a2573073ffffffffffffffffffffffffffffffffffffffff163190505b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc829081150290604051600060405180830381858888f19350505050158015610209573d6000803e3d6000fd5b5050565b6000600254905090565b670de0b6b3a7640000341015151561022e57600080fd5b600073ffffffffffffffffffffffffffffffffffffffff166000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614156102c957336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555061031c565b678ac7230489e800003410151561031b57336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505b5b80600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002081905550505600a165627a7a723058209b1d7608e3cc06254fde15f20deb017b454ecf7d6fe51d1d28f4fa870b63359b0029";
        String fixedArgs = "0000000000000000000000000000000000000000000000000000000000000051";
        String testConstructorAbi = "67e0badb";

        // B: 构造函数带变长参数
        String variableContract = "608060405234801561001057600080fd5b506040516105b43803806105b4833981018060405281019080805190602001909291908051820192919050505081600281905550806003908051906020019061005a929190610062565b505050610107565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106100a357805160ff19168380011785556100d1565b828001600101855582156100d1579182015b828111156100d05782518255916020019190600101906100b5565b5b5090506100de91906100e2565b5090565b61010491905b808211156101005760008160009055506001016100e8565b5090565b90565b61049e806101166000396000f300608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680633ccfd60b1461005c57806367e0badb146100735780637365870b1461010a575b600080fd5b34801561006857600080fd5b5061007161012a565b005b34801561007f57600080fd5b50610088610279565b6040518083815260200180602001828103825283818151815260200191508051906020019080838360005b838110156100ce5780820151818401526020810190506100b3565b50505050905090810190601f1680156100fb5780820380516001836020036101000a031916815260200191505b50935050505060405180910390f35b61012860048036038101908080359060200190929190505050610326565b005b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561018757600080fd5b670de0b6b3a76400003073ffffffffffffffffffffffffffffffffffffffff16310390506051600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054141561020e573073ffffffffffffffffffffffffffffffffffffffff163190505b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc829081150290604051600060405180830381858888f19350505050158015610275573d6000803e3d6000fd5b5050565b600060606002546003808054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156103175780601f106102ec57610100808354040283529160200191610317565b820191906000526020600020905b8154815290600101906020018083116102fa57829003601f168201915b50505050509050915091509091565b670de0b6b3a7640000341015151561033d57600080fd5b600073ffffffffffffffffffffffffffffffffffffffff166000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614156103d857336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555061042b565b678ac7230489e800003410151561042a57336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505b5b80600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002081905550505600a165627a7a723058203f690f8c807b1f2601dd606a156a1080eb27d80ac79c7dd3672bb8fb753708f60029";
        String variableConstructorArgs = "0000000000000000000000000000000000000000000000000000000000000051000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000075b312c322c335d00000000000000000000000000000000000000000000000000";

        // C: 合约执行回滚
        String revertContract = "608060405234801561001057600080fd5b5061034f806100206000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680633ccfd60b146100515780637365870b14610068575b600080fd5b34801561005d57600080fd5b50610066610088565b005b610086600480360381019080803590602001909291905050506101d7565b005b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff161415156100e557600080fd5b670de0b6b3a76400003073ffffffffffffffffffffffffffffffffffffffff16310390506051600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054141561016c573073ffffffffffffffffffffffffffffffffffffffff163190505b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc829081150290604051600060405180830381858888f193505050501580156101d3573d6000803e3d6000fd5b5050565b670de0b6b3a764000034101515156101ee57600080fd5b600073ffffffffffffffffffffffffffffffffffffffff166000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16141561028957336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506102dc565b678ac7230489e80000341015156102db57336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505b5b80600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002081905550505600a165627a7a7230582088b8903289b2843797d1c7e2c7d5c45afd473af38339afaf10adb90cddf905120029";
        String revertAbi = "7365870b0000000000000000000000000000000000000000000000000000000000000051";

        // D: 测试 revert 合约
        String directRevertContract = "608060405234801561001057600080fd5b506103bb806100206000396000f300608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680633ccfd60b1461005c5780637365870b14610073578063a26388bb14610093575b600080fd5b34801561006857600080fd5b506100716100aa565b005b610091600480360381019080803590602001909291905050506101f9565b005b34801561009f57600080fd5b506100a8610345565b005b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561010757600080fd5b670de0b6b3a76400003073ffffffffffffffffffffffffffffffffffffffff16310390506051600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054141561018e573073ffffffffffffffffffffffffffffffffffffffff163190505b6000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166108fc829081150290604051600060405180830381858888f193505050501580156101f5573d6000803e3d6000fd5b5050565b670de0b6b3a7640000341015151561021057600080fd5b600073ffffffffffffffffffffffffffffffffffffffff166000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614156102ab57336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506102fe565b678ac7230489e80000341015156102fd57336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505b5b80600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000208190555050565b606e600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002081905550600080fd00a165627a7a723058205057d1d6efab320556d8760c11ac811e2c418a619d6315fb72decacffab385780029";
        String directRevertAbi = "a26388bb";

        // E: erc20 合约
        String erc20Contract = "608060405234801561001057600080fd5b5061002c33612710610031640100000000026401000000009004565b6101c0565b600073ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff161415151561006d57600080fd5b61008f8160025461019f64010000000002610d80179091906401000000009004565b6002819055506100f3816000808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205461019f64010000000002610d80179091906401000000009004565b6000808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055508173ffffffffffffffffffffffffffffffffffffffff16600073ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef836040518082815260200191505060405180910390a35050565b60008082840190508381101515156101b657600080fd5b8091505092915050565b610dcd806101cf6000396000f300608060405260043610610099576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063095ea7b31461009e57806318160ddd1461010357806323b872dd1461012e5780632ff2e9dc146101b357806339509351146101de57806370a0823114610243578063a457c2d71461029a578063a9059cbb146102ff578063dd62ed3e14610364575b600080fd5b3480156100aa57600080fd5b506100e9600480360381019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803590602001909291905050506103db565b604051808215151515815260200191505060405180910390f35b34801561010f57600080fd5b50610118610508565b6040518082815260200191505060405180910390f35b34801561013a57600080fd5b50610199600480360381019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff16906020019092919080359060200190929190505050610512565b604051808215151515815260200191505060405180910390f35b3480156101bf57600080fd5b506101c8610639565b6040518082815260200191505060405180910390f35b3480156101ea57600080fd5b50610229600480360381019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291908035906020019092919050505061063f565b604051808215151515815260200191505060405180910390f35b34801561024f57600080fd5b50610284600480360381019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610876565b6040518082815260200191505060405180910390f35b3480156102a657600080fd5b506102e5600480360381019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803590602001909291905050506108be565b604051808215151515815260200191505060405180910390f35b34801561030b57600080fd5b5061034a600480360381019080803573ffffffffffffffffffffffffffffffffffffffff16906020019092919080359060200190929190505050610af5565b604051808215151515815260200191505060405180910390f35b34801561037057600080fd5b506103c5600480360381019080803573ffffffffffffffffffffffffffffffffffffffff169060200190929190803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610b0c565b6040518082815260200191505060405180910390f35b60008073ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff161415151561041857600080fd5b81600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055508273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925846040518082815260200191505060405180910390a36001905092915050565b6000600254905090565b60006105a382600160008773ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054610b9390919063ffffffff16565b600160008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000208190555061062e848484610bb4565b600190509392505050565b61271081565b60008073ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff161415151561067c57600080fd5b61070b82600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054610d8090919063ffffffff16565b600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055508273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008773ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020546040518082815260200191505060405180910390a36001905092915050565b60008060008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020549050919050565b60008073ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff16141515156108fb57600080fd5b61098a82600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054610b9390919063ffffffff16565b600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055508273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925600160003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008773ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020546040518082815260200191505060405180910390a36001905092915050565b6000610b02338484610bb4565b6001905092915050565b6000600160008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054905092915050565b600080838311151515610ba557600080fd5b82840390508091505092915050565b600073ffffffffffffffffffffffffffffffffffffffff168273ffffffffffffffffffffffffffffffffffffffff1614151515610bf057600080fd5b610c41816000808673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054610b9390919063ffffffff16565b6000808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002081905550610cd4816000808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054610d8090919063ffffffff16565b6000808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055508173ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef836040518082815260200191505060405180910390a3505050565b6000808284019050838110151515610d9757600080fd5b80915050929150505600a165627a7a723058207b3eef45fc3e81bfd0cb410cac0b531119a10789cf0b9518ff1ca04be3fd8e740029";
        String erc20TransferAbi = "a9059cbb0000000000000000000000000000000000000d9dfd6e8ad4daf2ada04061db7b0000000000000000000000000000000000000000000000000000000000001324";
        String erc20BalanceOfAbi = "70a082310000000000000000000000000000000000000d9dfd6e8ad4daf2ada04061db7b";

        TransactionTest test = new TransactionTest("0_0");

        // =================================================================
        // 测试方法：部署合约 => 调用函数
        // =================================================================

        // 创建合约: 
        // \-- A. 固定参数合约：(fixedContract, fixedArgs)
        // \-- B. 变长参数合约：(variableContract, variableArgs)
        // \-- C. 回滚合约：(revertContract, "")
        // \-- D. 函数直接 revert：(directRevertContract, "")
        // \-- E. ERC20：(erc20Contract, "")
        byte[] contractAddr = test.createContract(erc20Contract, "");
        System.out.println("contract address is: " + new String(contractAddr));

        // A & B: 调用`getNum()`函数获取构造函数的输入值
        // test.callFunc(contractAddr, "getNum", "3b9aca00", "59d8", "00", testConstructorAbi);

        // C: 调用`bet(uint256): 81`函数测试 revert
        // test.callFunc(contractAddr, "bet", "3b9aca00", "a028", "0c7d713b49da0000", revertAbi);

        // D: 调用`testRevert()`函数测试直接 revert
        // test.callFunc(contractAddr, "testRevert", "3b9aca00", "c350", "00", directRevertAbi);

        // E: 调用`transfer(address, uint256)`函数测试转代币给 QEL7I56HOSPIWQVAZ4PFRZUTN5O2G3LT
        // \--调用`balanceOf(address)`函数测试代币是否转入
        test.callFunc(contractAddr, "transfer", "3b9aca00", "0186a0", "00", erc20TransferAbi);
        test.callFunc(contractAddr, "balanceOf", "3b9aca00", "0186a0", "00", erc20BalanceOfAbi);
    }

    /**
     * 测试部署合约，需要传入部署用的 bytecode
     * 
     * @param _bytecode
     * @return 生成的合约地址
     * @throws IOException
     */
    public byte[] createContract(String _bytecode, String _args) throws IOException {
        //构造一个交易
        //1.生成一个账户，并将其添加到repository中
        RepositoryProvider.getTrack("").createAccount(senderAddress);
        RepositoryProvider.getTrack("").addBalance(senderAddress, new BigInteger("55000000000000000000"));
        
        System.out.println("*** senderAddr:" + Hex.toHexString(senderAddress) + 
            "\n*** senderBalance:" + RepositoryProvider.getTrack("").getAccountState(senderAddress).getBalance());


        byte[] nonce = RepositoryProvider.getTrack("").getNonce(senderAddress).toByteArray();
        byte[] gasPrice = Hex.decode("3b9aca00");       // 10000000000000
        byte[] recieveAddress = null;
        byte[] endowment = Hex.decode("00"); //0
        byte[] gasLimit= Hex.decode("1e8480");  //033450 => 210000; 061a80 => 400000; 1e8480 => 2000000
        
        byte[] bytecodeData = Hex.decode(_bytecode);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write(bytecodeData);
        
        // 若构造函数不带参数
        if(!_args.equals("")){
            // 构造函数带有固定参数
            byte[] arg = Hex.decode(_args);
            outputStream.write(arg);
        }

        byte[] data = outputStream.toByteArray();

        // byte[] data = new byte[bytecodeData.length + arg.length];
        // System.out.println("data.length is: " + data.length);
        // System.arraycopy(bytecode, 0, data, 0, bytecodeData.length);
        // System.arraycopy(arg, 0, data, bytecodeData.length, arg.length);
        
        Transaction tx = new Transaction(
            nonce, 
            gasPrice,
            gasLimit, 
            recieveAddress, 
            endowment, 
            data);

        tx.setSender(senderAddress);

        //******Create the contract
        System.out.println("\n\n\n");
        System.out.println("==================== Starting creating contract! ==================");
        System.out.println("Sender Balance before execution: " + 
            RepositoryProvider.getTrack("").getAccountState(senderAddress).getBalance());

        INVETransactionExecutor executor = doExec(tx);
        
        // 交易执行的收据信息
        if(executor.getReceipt().isSuccessful()) {
            // Save contract code
            byte[] code2 = executor.getResult().getHReturn();
            RepositoryProvider.getTrack("").saveCode(tx.getContractAddress(),code2);
            System.out.println("*** contract code saved...");
        }else {
            System.out.println("*** TX execution failed, contract code not saved...");
        }

        System.out.println("*** Sender Balance:"+ RepositoryProvider.getTrack("").getAccountState(senderAddress).getBalance());

        return tx.getContractAddress();
    }

    /**
     * 执行函数调用
     * @param recieveAddr
     * @param _gasPrice
     * @param _gasLimit
     * @param _endowment
     * @param abi
     */
    public void callFunc(byte[] recieveAddr, String funName, String _gasPrice, String _gasLimit, String _endowment, String abi) {
        System.out.println("\n\n\n");
        System.out.println("==================== Starting calling contract method '" + funName + "'! ==================");
        
        byte[] nonce = RepositoryProvider.getTrack("").getNonce(senderAddress).toByteArray();
        byte[] gasPrice = Hex.decode(_gasPrice);
        byte[] gasLimit= Hex.decode(_gasLimit);
        byte[] endowment = Hex.decode(_endowment); 
        byte[] data = Hex.decode(abi);

        Transaction tx = new Transaction(
            nonce, 
            gasPrice,
            gasLimit, 
            recieveAddr, 
            endowment, 
            data);
        
        tx.setSender(senderAddress);

        INVETransactionExecutor executor = doExec(tx);
        System.out.println("Is tx reverted? |-- " + executor.getResult().isRevert());

        System.out.println("*** Contract Balance after " + funName + " is: " + 
            RepositoryProvider.getTrack("").getAccountState(tx.getReceiveAddress()).getBalance());
        System.out.println("*** Caller Balance after " + funName + "is: " + 
            RepositoryProvider.getTrack("").getAccountState(tx.getSender()).getBalance());
    }

    /**
     * 根据交易构造 TransactionExecutor 并执行合约
     * @param tx
     * @return TransactionExecutor
     */
    private INVETransactionExecutor doExec(Transaction tx) {
        INVETransactionExecutor executor = new INVETransactionExecutor(
            tx, 
            RepositoryProvider.getTrack(""),
            new BlockStoreDummy(),
            (INVEProgramInvokeFactory) new INVEProgramInvokeFactoryImpl(),
            SystemProperties.getDefault().getGenesis()).setLocalCall(false);

        executor.init();
        executor.execute();
        executor.go();
        executor.finalization();

        // 交易执行的收据信息
        INVETransactionReceipt receipt = executor.getReceipt();
        System.out.println("============== Receipt of TX: " + tx.hashCode() + "\t TX status is: " + receipt.isSuccessful());
        System.out.println(receipt.toString());

        return  executor;
    }

}
