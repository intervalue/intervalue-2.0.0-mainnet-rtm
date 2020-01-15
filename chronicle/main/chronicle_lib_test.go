package main

import (
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/log"
	"testing"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 1/7/20 5:46 PM
 * @Description: 
 */

func TestFetchBlock(t *testing.T){
	Init("/home/francis/chronicleEnv")
	bytes,b := RetrieveTransactionsByBlockNumber1(1)
	if b{
		log.Debug().Msgf("bytes: %v", bytes)
	}
	Close()
}
 
