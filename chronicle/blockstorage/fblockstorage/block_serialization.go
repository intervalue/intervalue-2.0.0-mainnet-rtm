package fblockstorage

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @Description:
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @version: V2.0 utilize {@Code EncodeStringBytes} in the class {@Code Buffer} instead of {@Code EncodeRawBytes}
 */

import (
	"github.com/golang/protobuf/proto"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/log"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/utilities"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/vo"
	"github.com/pkg/errors"
)

type serializedBlockInfo struct {
	blockHeader *vo.BlockHeader
	txOffsets   []*txIndex
	metadata    *vo.BlockMetadata
}

type txIndex struct {
	txID    string
	locator *locPointer
}

func extractSerializedBlockInfo(blockBytes []byte) (*serializedBlockInfo, error) {
	serializedBlockInfo := &serializedBlockInfo{}
	var err error
	b := utilities.NewBuffer(blockBytes)
	serializedBlockInfo.blockHeader, err = extractHeader(b)
	if err != nil {
		return nil, err
	}

	_, serializedBlockInfo.txOffsets, err = extractData(b)
	if err != nil {
		return nil, err
	}

	serializedBlockInfo.metadata, err = extractMetadata(b)
	if err != nil {
		return nil, err
	}
	return serializedBlockInfo, err
}

func deserializeBlock(blockBytes []byte) (*vo.Block, error) {
	block := &vo.Block{}
	var err error

	b := utilities.NewBuffer(blockBytes)
	if block.Header, err = extractHeader(b); err != nil {
		return nil, err
	}

	if block.Data, _, err = extractData(b); err != nil {
		return nil, err
	}

	if block.Metadata, err = extractMetadata(b); err != nil {
		return nil, err
	}

	return block, nil
}

func extractHeader(b *utilities.Buffer) (*vo.BlockHeader, error) {
	header := &vo.BlockHeader{}
	var err error

	if header.Number, err = b.InheriDecodeVarint(); err != nil {
		return nil, errors.Wrapf(err, "error in decoding header number")
	}

	if header.DataHash, err = b.InheriDecodeRawBytes(false); err != nil {
		return nil, errors.Wrapf(err, "error in decoding header data hash")
	}

	if header.PreviousHash, err = b.InheriDecodeRawBytes(false); err != nil {
		return nil, errors.Wrapf(err, "error in decoding header previous hash")
	}

	if len(header.PreviousHash) == 0 {
		header.PreviousHash = nil
	}

	return header, nil
}

func extractData(b *utilities.Buffer) (*vo.BlockData, []*txIndex, error) {
	var numItems uint64
	var txIndexes []*txIndex
	var err error
	data := &vo.BlockData{}

	if numItems, err = b.InheriDecodeVarint(); err != nil {
		return nil, nil, errors.Wrapf(err, "error in decoding the length of tx")
	}

	for i := uint64(0); i < numItems; i++ {
		var txBytes []byte
		var txID string

		txOffset := b.Position
		//if txBytes,err = b.InheriDecodeRawBytes(false);err!=nil{
		if txBytes, err = b.InheriDecodeStringBytes(false); err != nil {
			return nil, nil, errors.Wrapf(err, "error in decoding transaction")
		}
		log.Debug().Msgf("txBytes: %s", string(txBytes))

		//todo: how to calculate tx id
		txID = utilities.TxHash(txBytes)

		data.Data = append(data.Data, txBytes)
		indexP := &txIndex{txID, &locPointer{txOffset, b.Position - txOffset}}
		txIndexes = append(txIndexes, indexP)
	}

	return data, txIndexes, nil
}

func extractMetadata(b *utilities.Buffer) (*vo.BlockMetadata, error) {
	metadata := &vo.BlockMetadata{}
	var numItems uint64
	var metadataEntry []byte
	var err error
	if numItems, err = b.InheriDecodeVarint(); err != nil {
		return nil, errors.Wrap(err, "error in decoding the length of block metadata")
	}
	for i := uint64(0); i < numItems; i++ {
		if metadataEntry, err = b.InheriDecodeRawBytes(false); err != nil {
			//QM
			return nil, errors.Wrap(err, "error in decoding the block metadata")
			//metadataEntry = []byte{}
		}
		metadata.Metadata = append(metadata.Metadata, metadataEntry)
	}
	return metadata, nil
}

func addHeaderBytes(blockHeader *vo.BlockHeader, buf *proto.Buffer) error {
	if err := buf.EncodeVarint(blockHeader.Number); err != nil {
		return errors.Wrapf(err, "error encoding the block number [%d]", blockHeader.Number)
	}
	if err := buf.EncodeRawBytes(blockHeader.DataHash); err != nil {
		return errors.Wrapf(err, "error encoding the data hash [%v]", blockHeader.DataHash)
	}
	if err := buf.EncodeRawBytes(blockHeader.PreviousHash); err != nil {
		return errors.Wrapf(err, "error encoding the previous hash [%v]", blockHeader.PreviousHash)
	}

	log.Debug().Msgf("buffer size after new block header: %d", len(buf.Bytes()))

	return nil
}

func addDataBytesAndConstructTxIndexInfo(blockData *vo.BlockData, buf *proto.Buffer) ([]*txIndex, error) {
	var txOffsets []*txIndex

	if err := buf.EncodeVarint(uint64(len(blockData.Data))); err != nil {
		return nil, errors.Wrapf(err, "error encoding the length of block data")
	}
	for _, txBytes := range blockData.Data {
		offset := len(buf.Bytes())

		//if err:=buf.EncodeRawBytes(txBytes);err!=nil{
		if err := buf.EncodeStringBytes(string(txBytes)); err != nil {
			return nil, errors.Wrapf(err, "error encoding the transaction")
		}

		txIndex := &txIndex{locator: &locPointer{offset, len(buf.Bytes()) - offset}}
		txOffsets = append(txOffsets, txIndex)
	}

	log.Debug().Msgf("buffer size after new block data: %d", len(buf.Bytes()))

	return txOffsets, nil
}

func addMetadataBytes(blockMetadata *vo.BlockMetadata, buf *proto.Buffer) error {
	numItems := uint64(0)
	if blockMetadata != nil {
		numItems = uint64(len(blockMetadata.Metadata))
	}
	if err := buf.EncodeVarint(numItems); err != nil {
		return errors.Wrap(err, "error encoding the length of metadata")
	}
	for _, b := range blockMetadata.Metadata {
		if err := buf.EncodeRawBytes(b); err != nil {
			return errors.Wrap(err, "error encoding the block metadata")
		}
	}

	log.Debug().Msgf("buffer size after new block metadata: %d", len(buf.Bytes()))

	return nil
}

func serializeBlock(block *vo.Block) ([]byte, *serializedBlockInfo, error) {
	buf := proto.NewBuffer(nil)
	buf.SetDeterministic(true)
	var err error

	info := &serializedBlockInfo{}
	info.blockHeader = block.Header
	info.metadata = block.Metadata

	if err = addHeaderBytes(block.Header, buf); err != nil {
		return nil, nil, err
	}

	if info.txOffsets, err = addDataBytesAndConstructTxIndexInfo(block.Data, buf); err != nil {
		return nil, nil, err
	}

	if err = addMetadataBytes(block.Metadata, buf); err != nil {
		return nil, nil, err
	}

	return buf.Bytes(), info, nil

}
