package utilities

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @Description:
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 */

import (
	"github.com/golang/protobuf/proto"
	"github.com/pkg/errors"
)

type Buffer struct {
	*proto.Buffer
	Position int
}


func NewBuffer(bytes []byte) *Buffer{
	return &Buffer{proto.NewBuffer(bytes),0}
}

func (buf *Buffer) InheriDecodeVarint() (uint64,error){
	v,err:=buf.DecodeVarint()
	if err!=nil{
		return 0,errors.Wrap(err,"error in decoding varint")
	}

	buf.Position+=proto.SizeVarint(v)

	return v,err
}

func (buf *Buffer) InheriDecodeRawBytes(alloc bool) ([]byte,error){
	v,err:=buf.DecodeRawBytes(alloc)
	if err!=nil{
		return nil,errors.Wrap(err,"error in decoding raw bytes")
	}

	buf.Position+=proto.SizeVarint(uint64(len(v)))+len(v)

	return v,err
}