package fblockstorage

import (
	"crypto/rand"
	"github.com/golang/protobuf/proto"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/utilities"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/vo"
	"github.com/stretchr/testify/assert"
	"testing"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 2/17/20 6:10 PM
 * @Description: to evaluate block,protobuf Serialization/Deserialization capability
 */

func ConstructRandBytes(t testing.TB, size int) []byte {
	value := make([]byte, size)
	_, err := rand.Read(value)
	if err != nil {
		t.Fatalf("error in gen random bytes: %s", err)
	}

	return value
}

func ConstructTestBlock(t *testing.T, previousHash []byte, blockNum uint64, numTx int, txSize int) *vo.Block {
	block := new(vo.Block)
	block.Header = new(vo.BlockHeader)
	block.Data = new(vo.BlockData)
	block.Metadata = new(vo.BlockMetadata)
	block.Header.PreviousHash = previousHash

	simulatedTxes := [][]byte{}
	for i := 0; i < numTx; i++ {
		simulatedTxes = append(simulatedTxes, ConstructRandBytes(t, txSize))
	}

	block.Data.Data = simulatedTxes

	return block
}

func TestBlockSerialization(t *testing.T) {
	block := ConstructTestBlock(t, nil, 0, 3, 20)
	block.Data.Data[0] = []byte("{\"mt\":1,\"mb\":\"JDK0,message body\"}")
	block.Data.Data[1] = []byte("{\"mt\":1,\"mb\":\"SUN-JDK1,message body\"}")
	block.Data.Data[2] = []byte("{\"mt\":2,\"mb\":\"F15,system message body\"}")

	sb, _, err := serializeBlock(block)
	assert.NoError(t, err)
	db, err := deserializeBlock(sb)
	assert.NoError(t, err)
	//skeptical "equal"
	assert.Equal(t, block.Data, db.Data)
}

func TestProtoBufSerialization(t *testing.T) {
	buf := proto.NewBuffer(nil)
	txBytesBytes := [][]byte{[]byte("{\"mt\":1,\"mb\":\"JDK0,message body\"}"), []byte("{\"mt\":1,\"mb\":\"SUN-JDK1,message body\"}"), []byte("{\"mt\":2,\"mb\":\"F15,system message body\"}")}

	if err := buf.EncodeVarint(uint64(len(txBytesBytes))); err != nil {
		assert.NoError(t, err)
	}
	for _, txBytes := range txBytesBytes {
		if err := buf.EncodeStringBytes(string(txBytes)); err != nil {
			assert.NoError(t, err)
		}
	}

	bytes := buf.Bytes()

	b := utilities.NewBuffer(bytes)
	var numItems uint64
	var err error

	if numItems, err = b.InheriDecodeVarint(); err != nil {
		assert.NoError(t, err)
	}

	for i := uint64(0); i < numItems; i++ {
		var txBytes []byte

		if txBytes, err = b.InheriDecodeStringBytes(false); err != nil {
			assert.NoError(t, err)
		}
		assert.Equal(t, txBytesBytes[i], txBytes)
		//t.Logf("%s",string(txBytes))

	}
}
