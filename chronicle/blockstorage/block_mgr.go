package blockstorage

import (
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/blockstorage/fblockstorage"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/vo"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 1/2/20 7:31 PM
 * @Description: 
 */ 

var _ IBlockMgr = (*fblockstorage.BlockfileMgr)(nil)


type IBlockMgr interface{
	AddBlockByTransactions(txes [][]byte) error
	RetrieveBlockByNumber(blockNum uint64) (*vo.Block, error)
	Close()
}

func NewBlockMgr(dir string) IBlockMgr{
	conf:=fblockstorage.NewConf(dir,0)
	return fblockstorage.NewBlockfileMgr("inve",conf)
}
