package one.inve.contract.struct;
/**
 放置编译后，部署需要的bytecode和abi等信息
 */
public class ContractMetadata {
    byte[]  abi;
    byte[] bin;//bytecode
    public String version="";
    //一些操作函数
}//End class
