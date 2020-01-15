package utilities

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 12/30/19 6:07 PM
 * @Description: 
 */

import (
	"bytes"
	"crypto/sha512"
	"encoding/gob"
	"fmt"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/vo"
	"math/big"
)

func init(){
	gob.Register(&gobHeader{})
}

type gobHeader struct {
	Number *big.Int
	DataHash []byte
	PreviousHash []byte
}

func BlockHeaderBytes(b *vo.BlockHeader) []byte {
	buf:=new(bytes.Buffer)
	encoder:=gob.NewEncoder(buf)
	gobHeader:=gobHeader{new(big.Int).SetUint64(b.Number),b.DataHash,b.PreviousHash}

	err:=encoder.Encode(&gobHeader)
	if err!=nil{
		panic(fmt.Sprintf("encoding err:%s",err))
	}

	return buf.Bytes()
}

func BlockHeaderHash(b *vo.BlockHeader) []byte {
	return sha512.New384().Sum(BlockHeaderBytes(b))
}

func BlockDataHash(b *vo.BlockData) []byte {
	return sha512.New384().Sum(bytes.Join(b.Data,nil))
}

//combine block data with previous hash pointer
func BlockDataHash2(b *vo.BlockData,PreviousHash []byte) []byte {
	var bytesBlockData [][]byte = b.Data
	if PreviousHash!=nil{
		bytesBlockData=append(b.Data,PreviousHash)
	}

	return sha512.New384().Sum(bytes.Join(bytesBlockData,nil))
}

func TxHash(txBytes []byte) string{
	return string(sha512.New384().Sum(txBytes))
}
 
