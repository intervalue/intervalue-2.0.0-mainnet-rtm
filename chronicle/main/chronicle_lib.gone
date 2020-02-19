package main

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 1/2/20 7:22 PM
 * @build flag: go build -o libchroniclejni.so -buildmode=c-shared chronicle_lib.go
 */

import "C"
import (
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/blockstorage"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/log"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/utilities"
	"sync"
	"unsafe"
)

var once sync.Once
var inst blockstorage.IBlockMgr

//export Init
func Init(dir string) {
	log.Debug().Msgf("Init([%s])",dir)

	once.Do(func(){
		inst=blockstorage.NewBlockMgr(dir)
	})
}

//export AddBlock
func AddBlock(bytes []byte) (isBlockAdded bool){
	log.Debug().Msgf("AddBlock(): bytes,%s",string(bytes))
	isBlockAdded = false

	if bytes!= nil{
		txes:=utilities.To2DimBytes(bytes)
		if txes!=nil{
			err := inst.AddBlockByTransactions(txes)
			if err==nil{
				isBlockAdded = true
			}
		}
	}

	return
}

//export Close
func Close(){
	log.Debug().Msg("Close()")
	inst.Close()
}

//export RetrieveTransactionsByBlockNumber1
func RetrieveTransactionsByBlockNumber1(iBlockNum int) (unsafe.Pointer,bool){
	//log.Debug().Msgf("sBlockNum: %d",iBlockNum)
	//log.Debug().Msgf("RetrieveTransactionsByBlockNumber([%#v])",iBlockNum)
	////attemptToConvBlockNum,err := strconv.Atoi(sBlockNum)
	////blockNum:=uint64(iBlockNum)
	////if err!=nil{
	////	log.Debug().Msgf("fail to convert [%s] into int type",sBlockNum)
	////	return nil,false
	////}
	//
	//blockNum:=uint64(iBlockNum)
	//var result = true
	//block,err:=inst.RetrieveBlockByNumber(blockNum)
	//if block==nil || err!=nil{
	//	log.Debug().Msgf("fail to get block object for block num [%d]",blockNum)
	//	return nil,false
	//}
	//log.Debug().Msgf("block.Data.Data: %#v",block)
	//encodedBytes:=utilities.ToBytes(block.Data.Data)
	//
	////return encodedBytes,result
	//return unsafe.Pointer(&encodedBytes[0]),result
	log.Debug().Msgf("sBlockNum: %d",iBlockNum)
	return nil,true
}

//export Add
func Add(a, b int) int {
	log.Debug().Msgf("a:%d   b:%d",a,b)
	return a + b
}

//export GetTransactionsByBlockNumber
func GetTransactionsByBlockNumber(iBlockNum int) (unsafe.Pointer,bool){
	log.Debug().Msgf("iBlockNum: %d",iBlockNum)
	log.Debug().Msgf("RetrieveTransactionsByBlockNumber([%#v])",iBlockNum)
	//attemptToConvBlockNum,err := strconv.Atoi(sBlockNum)
	//blockNum:=uint64(iBlockNum)
	//if err!=nil{
	//	log.Debug().Msgf("fail to convert [%s] into int type",sBlockNum)
	//	return nil,false
	//}

	blockNum:=uint64(iBlockNum)
	var result = true
	block,err:=inst.RetrieveBlockByNumber(blockNum)
	if block==nil || err!=nil{
		log.Debug().Msgf("fail to get block object for block num [%d]",blockNum)
		return nil,false
	}
	log.Debug().Msgf("block.Data.Data: %#v",block)
	encodedBytes:=utilities.ToBytes(block.Data.Data)

	//return encodedBytes,result
	return unsafe.Pointer(&encodedBytes[0]),result
}


func main(){}