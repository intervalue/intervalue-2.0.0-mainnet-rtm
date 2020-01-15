package core

import "github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/log"

/**
*
* Copyright © INVE FOUNDATION. All rights reserved.
*
* @author: Francis.Deng[francis_xiiiv@163.com]
* @version: V1.0
* @Date: 1/13/20 11:02 PM
* @Description:
* @Note: assume that localfullnode2 services are able to differentiate message type,
         which means the [][]byte is wrapped message
*/

func (localfullnode2Chronicle *Localfullnode2Chronicle) restore() {
	var blockNum uint64 = 0

	for {
		block, err := localfullnode2Chronicle.RetrieveBlockByNumber(blockNum)
		if err != nil {
			log.Fatal().Msgf("error in localfullnode2Chronicle.RetrieveBlockByNumber(%s)", blockNum)
			return
		}

		if block == nil {
			log.Info().Msgf("reach the end of block[%s]", blockNum)
			return
		}

		blockData := block.Data.Data

		err = localfullnode2Chronicle.PersistStream(blockData)
		if err != nil {
			log.Error().Msgf("error in localfullnode2Chronicle.PersistStream: %s", err)
			return
		}

		blockNum++
	}

	localfullnode2Chronicle.done()
}
