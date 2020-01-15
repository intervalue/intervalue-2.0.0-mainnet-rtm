package fblockstorage

import (
	"fmt"
	"math/rand"
	"testing"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 12/30/19 10:16 PM
 * @Description: 
 */ 
 

func NewTransactions(t testing.TB,numTx int,txSize int) [][]byte {
	simuTxes := [][]byte{}
	for i:=0;i<numTx;i++ {
		simuTxes = append(simuTxes,randBytes(t,txSize))
	}

	return simuTxes
}

func randBytes(t testing.TB, size int) []byte{
	v:=make([]byte,size)
	_,err:=rand.Read(v)
	if err!= nil{
		t.Fatal(fmt.Sprintf("error in generating random bytes:%s",err))
	}

	return v
}