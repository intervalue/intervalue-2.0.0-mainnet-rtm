package core

import (
	"github.com/cosmos/cosmos-sdk/types/errors"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/blockstorage"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/localfullnode2/rpc"
	"github.com/rs/zerolog/log"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 1/13/20 10:24 PM
 * @Description: is comprised of IBlockMgr, ChronicleDumperRestorerRPC
 */

type Localfullnode2Chronicle struct {
	blockstorage.IBlockMgr
	rpc.IChronicleDumperRestorerRPC
}

func NewLocalfullnode2Chronicle(dir, rAddr string, rPort int) *Localfullnode2Chronicle {
	rpcClient, err := rpc.NewDumperRPCClient(rAddr, rPort)
	if err != nil {
		log.Err(errors.Wrapf(err, "error in create NewDumperRPCClient:%s", err))
		return nil
	}
	return &Localfullnode2Chronicle{blockstorage.NewBlockMgr(dir), rpcClient}
}

func (localfullnode2Chronicle *Localfullnode2Chronicle) done() {
	log.Info().Msg(">====>                                   ")
	log.Info().Msg(">=>   >=>                                ")
	log.Info().Msg(">=>    >=>    >=>     >==>>==>    >==>   ")
	log.Info().Msg(">=>    >=>  >=>  >=>   >=>  >=> >>   >=> ")
	log.Info().Msg(">=>    >=> >=>    >=>  >=>  >=> >>===>>=>")
	log.Info().Msg(">=>   >=>   >=>  >=>   >=>  >=> >>       ")
	log.Info().Msg(">====>        >=>     >==>  >=>  >====>  ")
}

func (localfullnode2Chronicle *Localfullnode2Chronicle) Shutdown() {
	localfullnode2Chronicle.IBlockMgr.Close()
	localfullnode2Chronicle.IChronicleDumperRestorerRPC.Close()
}
