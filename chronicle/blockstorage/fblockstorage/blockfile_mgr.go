package fblockstorage

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @Description:
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 */

import (
	"bytes"
	"fmt"
	"github.com/davecgh/go-spew/spew"
	"github.com/golang/protobuf/proto"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/log"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/utilities"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/vo"
	"github.com/pkg/errors"
	"sync"
	"sync/atomic"
)

const (
	BLOCKFILE_PREFIX = "chronicle"
)

type BlockfileMgr struct {
	rootDir           string
	conf              *Conf
	cpInfo            *checkpointInfo
	cpInfoCond        *sync.Cond
	currentFileWriter *blockFileWriter
	bcInfo            atomic.Value
	index             index
}

type checkpointInfo struct {
	latestFileChunkSuffixNum int
	latestFileChunksize      int
	isChainEmpty             bool
	lastBlockNumber          uint64
}

func NewBlockfileMgr(id string, conf *Conf) *BlockfileMgr {
	log.Debug().Msgf("initializing file-based storage for id %s", id)
	root := conf.getChainsDir()
	_, err := utilities.CreateDirIfMissing(root)
	if err != nil {
		panic(fmt.Sprintf("error creating block storage in %s: %s", root, err))
	}

	mgr := &BlockfileMgr{rootDir: root, conf: conf}
	i, err := mgr.loadCurrentInfo()

	if i == nil {
		log.Info().Msg("get block information from storage")
		if i, err = constructCheckpointInfoFromBlockFiles(root); err != nil {
			panic(fmt.Sprintf("could not build check point information from storage: %s", err))
		}
		log.Debug().Msgf("checkpoint information = {%s}", spew.Sdump(i))
	}

	writer, err := newBlockFileWriter(deriveBlockfilePath(root, i.latestFileChunkSuffixNum))
	if err != nil {
		panic(fmt.Sprintf("could not open file writer: %s", err))
	}

	err = writer.truncateFile(i.latestFileChunksize)
	if err != nil {
		panic(fmt.Sprintf("error in resizing file to known size: %s", err))
	}

	mgr.index = newIndex()

	mgr.cpInfo = i
	mgr.currentFileWriter = writer
	mgr.cpInfoCond = sync.NewCond(new(sync.Mutex))

	blockchainInfo := &vo.BlockchainInfo{Height: 0, CurrentBlockHash: nil, PreviousBlockHash: nil}
	if !i.isChainEmpty {
		mgr.syncIndex()

		lastBlockHeader, err := mgr.retrieveBlockHeaderByNumber(mgr.cpInfo.lastBlockNumber)
		if err != nil {
			panic(fmt.Sprintf("Could not retrieve header of the last block form file: %s", err))
		}
		lastBlockHeaderHash := utilities.BlockHeaderHash(lastBlockHeader)
		previousHash := lastBlockHeader.PreviousHash
		blockchainInfo = &vo.BlockchainInfo{Height: mgr.cpInfo.lastBlockNumber + 1,
			CurrentBlockHash:  lastBlockHeaderHash,
			PreviousBlockHash: previousHash}

	}
	mgr.bcInfo.Store(blockchainInfo)

	return mgr
}

func (mgr *BlockfileMgr) syncIndex() error {
	var err error
	startFileNum := 0
	startOffset := 0
	//startBlockNum:=uint64(0)
	endFileNum := mgr.cpInfo.latestFileChunkSuffixNum

	var blockBytes []byte
	var blockPlacementInfo *blockPlacementInfo

	var stream *blocksInFilesStream
	if stream, err = newBlocksInFilesStream(mgr.rootDir, startFileNum, int64(startOffset), endFileNum); err != nil {
		return err
	}

	blockIdxInfo := &blockIdxInfo{}
	for {
		if blockBytes, blockPlacementInfo, err = stream.nextBlockAndPlacement(); err != nil {
			return err
		}

		if blockBytes == nil {
			break
		}
		info, err := extractSerializedBlockInfo(blockBytes)
		if err != nil {
			return err
		}

		numBytesToShift := int(blockPlacementInfo.blockBytesOffset - blockPlacementInfo.blockStartOffset)
		for _, offset := range info.txOffsets {
			offset.locator.offset += numBytesToShift
		}

		blockIdxInfo.blockNum = info.blockHeader.Number
		blockIdxInfo.flp = &fileLocPointer{blockPlacementInfo.fileNum, locPointer{offset: int(blockPlacementInfo.blockStartOffset)}}
		blockIdxInfo.txOffsets = info.txOffsets
		blockIdxInfo.metadata = info.metadata

		mgr.index.indexBlock(blockIdxInfo)
	}

	return nil
}

//return last block bytes,current offset,number of blocks,error
func scanForLastCompleteBlock(rootDir string, fileNum int, startingOffset int64) ([]byte, int64, int, error) {
	numBlocks := 0
	var lastBlockBytes []byte
	var blockBytes []byte
	var blockStreamP *blocksInFileStream
	var err error

	if blockStreamP, err = newBlocksInFileStream(rootDir, fileNum, startingOffset); err != nil {
		return nil, 0, 0, err
	}
	defer blockStreamP.close()

	for {
		blockBytes, err = blockStreamP.nextBlockBytes()
		if err != nil || blockBytes == nil {
			break
		}
		lastBlockBytes = blockBytes
		numBlocks++
	}

	return lastBlockBytes, blockStreamP.currentOffset, numBlocks, err

}

func (mgr *BlockfileMgr) loadCurrentInfo() (*checkpointInfo, error) {
	return nil, nil
}

func deriveBlockfilePath(dir string, suffixNum int) string {
	return dir + "/" + BLOCKFILE_PREFIX + fmt.Sprintf("%06d", suffixNum)
}

func (mgr *BlockfileMgr) AddBlockByTransactions(txes [][]byte) error {
	//block:=&vo.Block{
	//	Header:&vo.BlockHeader{},
	//	Data:&vo.BlockData{}}
	//bcInfo:=mgr.getBlockchainInfo()
	//
	//block.Header.Number = bcInfo.Height
	//block.Header.PreviousHash = bcInfo.CurrentBlockHash
	//block.Data.Data = txes
	//block.Metadata = &vo.BlockMetadata{Metadata:[][]byte{}}
	//block.Header.DataHash = utilities.BlockDataHash2(block.Data,bcInfo.CurrentBlockHash)

	block := new(vo.Block)
	block.Header = new(vo.BlockHeader)
	block.Data = new(vo.BlockData)
	block.Metadata = new(vo.BlockMetadata)
	bcInfo := mgr.getBlockchainInfo()

	block.Header.Number = bcInfo.Height
	block.Header.PreviousHash = bcInfo.CurrentBlockHash
	block.Data.Data = txes
	//block.Header.DataHash = utilities.BlockDataHash2(block.Data,bcInfo.CurrentBlockHash)

	return mgr.addBlock(block)
}

func (mgr *BlockfileMgr) addBlock(block *vo.Block) error {
	bcInfo := mgr.getBlockchainInfo()
	if bcInfo.Height != block.Header.Number {
		return errors.Errorf("block number is expected to be %d not %d", bcInfo.Height, block.Header.Number)
	}

	if !bytes.Equal(block.Header.PreviousHash, bcInfo.CurrentBlockHash) {
		return errors.Errorf("unexpected Previous block hash. Expected PreviousHash = [%x], PreviousHash referred in the latest block= [%x]",
			bcInfo.CurrentBlockHash, block.Header.PreviousHash)
	}

	blockBytes, info, err := serializeBlock(block)
	if err != nil {
		return errors.WithMessage(err, "error serializing block")
	}
	blockHeaderHash := utilities.BlockHeaderHash(block.Header)
	txOffsets := info.txOffsets
	currentOffset := mgr.cpInfo.latestFileChunksize

	blockBytesLen := len(blockBytes)
	blockBytesEncodedLen := proto.EncodeVarint(uint64(blockBytesLen))
	totalBytesToAppend := blockBytesLen + len(blockBytesEncodedLen)

	if currentOffset+totalBytesToAppend > mgr.conf.maxBlockfileSize {
		mgr.moveToNextFile()
		currentOffset = 0
	}

	err = mgr.currentFileWriter.append(blockBytesEncodedLen, false)
	if err == nil {
		//append the actual block bytes to the file
		err = mgr.currentFileWriter.append(blockBytes, true)
	}
	if err != nil {
		truncateErr := mgr.currentFileWriter.truncateFile(mgr.cpInfo.latestFileChunksize)
		if truncateErr != nil {
			panic(fmt.Sprintf("Could not truncate current file to known size after an error during block append: %s", err))
		}
		return errors.WithMessage(err, "error appending block to file")
	}

	currentCPInfo := mgr.cpInfo
	newCPInfo := &checkpointInfo{
		latestFileChunkSuffixNum: currentCPInfo.latestFileChunkSuffixNum,
		latestFileChunksize:      currentCPInfo.latestFileChunksize + totalBytesToAppend,
		isChainEmpty:             false,
		lastBlockNumber:          block.Header.Number}

	//Index block file location pointer updated with file suffex and offset for the new block
	blockFLP := &fileLocPointer{fileSuffixNum: newCPInfo.latestFileChunkSuffixNum}
	blockFLP.offset = currentOffset
	// shift the txoffset because we prepend length of bytes before block bytes
	for _, txOffset := range txOffsets {
		txOffset.locator.offset += len(blockBytesEncodedLen)
	}
	//save the index in the database
	if err = mgr.index.indexBlock(&blockIdxInfo{
		blockNum: block.Header.Number, blockHash: blockHeaderHash,
		flp: blockFLP, txOffsets: txOffsets, metadata: block.Metadata}); err != nil {
		return err
	}

	mgr.updateCheckpoint(newCPInfo)
	mgr.updateBlockchainInfo(blockHeaderHash, block)

	return nil
}

func (mgr *BlockfileMgr) moveToNextFile() {
	cpInfo := &checkpointInfo{
		latestFileChunkSuffixNum: mgr.cpInfo.latestFileChunkSuffixNum + 1,
		latestFileChunksize:      0,
		lastBlockNumber:          mgr.cpInfo.lastBlockNumber}

	nextFileWriter, err := newBlockFileWriter(deriveBlockfilePath(mgr.rootDir, mgr.cpInfo.latestFileChunkSuffixNum))
	if err != nil {
		panic(fmt.Sprintf("Could not open writer to next file: %s", err))
	}
	mgr.currentFileWriter.close()
	mgr.currentFileWriter = nextFileWriter
	mgr.updateCheckpoint(cpInfo)
}

func (mgr *BlockfileMgr) updateCheckpoint(cpInfo *checkpointInfo) {
	mgr.cpInfoCond.L.Lock()
	defer mgr.cpInfoCond.L.Unlock()

	mgr.cpInfo = cpInfo
	mgr.cpInfoCond.Broadcast()
}

func (mgr *BlockfileMgr) fetchBlockBytes(lp *fileLocPointer) ([]byte, error) {
	stream, err := newBlocksInFileStream(mgr.rootDir, lp.fileSuffixNum, int64(lp.offset))
	if err != nil {
		return nil, err
	}
	defer stream.close()
	b, err := stream.nextBlockBytes()
	if err != nil {
		return nil, err
	}
	return b, nil
}

func (mgr *BlockfileMgr) fetchBlock(lp *fileLocPointer) (*vo.Block, error) {
	blockBytes, err := mgr.fetchBlockBytes(lp)
	if err != nil {
		return nil, err
	}
	block, err := deserializeBlock(blockBytes)
	if err != nil {
		return nil, err
	}
	return block, nil
}

func (mgr *BlockfileMgr) RetrieveBlockByNumber(blockNum uint64) (*vo.Block, error) {

	loc, err := mgr.index.getBlockLocByBlockNum(blockNum)
	if err != nil {
		return nil, err
	}
	return mgr.fetchBlock(loc)
}

func (mgr *BlockfileMgr) retrieveBlockHeaderByNumber(blockNum uint64) (*vo.BlockHeader, error) {
	loc, err := mgr.index.getBlockLocByBlockNum(blockNum)
	if err != nil {
		return nil, err
	}
	blockBytes, err := mgr.fetchBlockBytes(loc)
	if err != nil {
		return nil, err
	}
	info, err := extractSerializedBlockInfo(blockBytes)
	if err != nil {
		return nil, err
	}

	return info.blockHeader, nil
}

func (mgr *BlockfileMgr) getBlockchainInfo() *vo.BlockchainInfo {
	return mgr.bcInfo.Load().(*vo.BlockchainInfo)
}

func (mgr *BlockfileMgr) updateBlockchainInfo(latestBlockHash []byte, latestBlock *vo.Block) {
	currentBCInfo := mgr.getBlockchainInfo()
	newBCInfo := &vo.BlockchainInfo{
		Height:            currentBCInfo.Height + 1,
		CurrentBlockHash:  latestBlockHash,
		PreviousBlockHash: latestBlock.Header.PreviousHash}

	mgr.bcInfo.Store(newBCInfo)
}

func (mgr *BlockfileMgr) Close() {
	mgr.currentFileWriter.close()
}
