package fblockstorage

import (
	"bytes"
	"github.com/davecgh/go-spew/spew"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/log"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/vo"
	"math/rand"
	"os"
	"strconv"
	"testing"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 12/31/19 5:30 PM
 * @Description: 
 */ 


type NewTransactionsFunction func() [][]byte

func newTrans(ntf NewTransactionsFunction) *BlockfileMgr{
	//blockfileMgr:=getBlockfileMgr()
	//blockfileMgr.addBlockByTransactions(NewTransactions(t,2,1024))
	//blockfileMgr.close()

	blockfileMgr:=GetBlockfileMgr()
	blockfileMgr.AddBlockByTransactions(ntf())

	return blockfileMgr
}

func fetchUpdated(blockfileMgr *BlockfileMgr) (*vo.Block, error){
	info:=blockfileMgr.getBlockchainInfo()

	log.Debug().Msg(spew.Sdump(info))
	return blockfileMgr.RetrieveBlockByNumber(info.GetHeight()-1)
}

func TestAppendAndFetch(t *testing.T){
	randInt:=rand.Int()
	hello:="讲述一只猴子海盗勇敢地冒险,"+strconv.Itoa(randInt)
	log.Debug().Msg(hello)

	blockfileMgr:=newTrans(func () [][]byte{
		var outcome=[][]byte{}
		outcome=append(outcome, []byte(hello))
		return outcome
	})

	block,err:=fetchUpdated(blockfileMgr)
	if err!=nil {
		t.Errorf("error:%s",err)
	}

	for _,txBytes:=range block.Data.Data{
		if !bytes.Equal([]byte(hello),txBytes){
			log.Error().Msgf("%s(in) is not equal to %s(out)",hello,string(txBytes))
		}
	}

	blockfileMgr.Close()


}

func GetBlockfileMgr() *BlockfileMgr {
	blockStorageDir:=buildBlockStorageDir()
	conf:=NewConf(blockStorageDir,0)

	return NewBlockfileMgr("chronicle",conf)
}


func buildBlockStorageDir() string{
	home,_:=os.UserHomeDir()
	dir:=home + string(os.PathSeparator) + "chronicleEnv"

	_,err:=os.Stat(dir)
	if os.IsNotExist(err){
		os.MkdirAll(dir,os.ModePerm)
	}

	return dir
}
