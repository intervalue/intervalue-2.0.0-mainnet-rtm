package utilities

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @Description:
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @version: V2.0 introduce {@code InheriDecodeStringBytes} to unmarshal string
 */

import (
	"github.com/golang/protobuf/proto"
	"github.com/pkg/errors"
)

type Buffer struct {
	buf      *proto.Buffer
	Position int
}

func NewBuffer(bytes []byte) *Buffer {
	internalBuf := proto.NewBuffer(bytes)
	internalBuf.SetDeterministic(true)
	return &Buffer{internalBuf, 0}
}

func (b *Buffer) InheriDecodeVarint() (uint64, error) {
	v, err := b.buf.DecodeVarint()
	if err != nil {
		return 0, errors.Wrap(err, "error in decoding varint")
	}

	b.Position += proto.SizeVarint(v)

	return v, err
}

func (b *Buffer) InheriDecodeRawBytes(alloc bool) ([]byte, error) {
	v, err := b.buf.DecodeRawBytes(alloc)
	if err != nil {
		return nil, errors.Wrap(err, "error in decoding raw bytes")
	}

	b.Position += proto.SizeVarint(uint64(len(v))) + len(v)

	return v, err
}

func (b *Buffer) InheriDecodeStringBytes(alloc bool) ([]byte, error) {
	v, err := b.buf.DecodeStringBytes()
	if err != nil {
		return nil, errors.Wrap(err, "error in decoding raw bytes")
	}

	b.Position += proto.SizeVarint(uint64(len(v))) + len(v)

	return []byte(v), err
}
