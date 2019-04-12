package one.inve.contract.struct;

/**
 账户相关类，系统维护一个账户信息表
 */
public class AccountInfo {
    public String accountAddress;
    public int accountType=0;//0普通账户，1合约账户
    public long balance=0;//余额
    //public String tranactionHash;//部署合约时，那笔上链交易的hash(CodeHash)
    public long nonce=0;
    public String storageRoot="";//内部变量的root根
    
    public AccountInfo(String accountAddress,int accountType,long balance,long nonce,String storageRoot){                
    	accountAddress=accountAddress;
    	accountType=accountType;     
    	balance=balance;
    	nonce=nonce;
    	storageRoot=storageRoot;
    }
}//End class
