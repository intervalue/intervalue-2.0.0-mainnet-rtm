package fblockstorage

import (
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/vo"
	"github.com/pkg/errors"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 12/26/19 11:07 PM
 * @Description: 
 */

type locPointer struct {
	offset      int
	bytesLength int
}

type fileLocPointer struct {
	fileSuffixNum int
	locPointer
}

type blockIdxInfo struct {
	blockNum  uint64
	blockHash []byte
	flp       *fileLocPointer
	txOffsets []*txIndex
	metadata  *vo.BlockMetadata
}


type index interface {
	indexBlock(blockIdxInfo *blockIdxInfo) error
	getBlockLocByBlockNum(blockNum uint64) (*fileLocPointer, error)
}

type blockIndex struct {
	fileLocPointerByBlockNum map[uint64]*fileLocPointer
}

func newIndex() *blockIndex{
	fileLocPointerByBlockNum := make(map[uint64]*fileLocPointer)

	return &blockIndex{fileLocPointerByBlockNum}
}

func (bi *blockIndex) indexBlock(blockIdxInfo *blockIdxInfo) error{
	flp:=blockIdxInfo.flp
	blockNum:=blockIdxInfo.blockNum

	bi.fileLocPointerByBlockNum[blockNum]=flp

	return nil

}

func (bi *blockIndex) getBlockLocByBlockNum(blockNum uint64) (*fileLocPointer, error){
	flp,ok := bi.fileLocPointerByBlockNum[blockNum]
	if !ok{
		return nil,errors.New("found no record at all")
	}

	return flp,nil
}
