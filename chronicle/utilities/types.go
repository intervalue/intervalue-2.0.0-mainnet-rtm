package utilities

import "github.com/golang/protobuf/proto"

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 1/6/20 11:47 PM
 * @Description: to resolve complex types-mapping issues
 */


//transform byte array into 2-dimentional byte array using protobuf code.
func To2DimBytes(bytes []byte) (twoDim [][]byte) {
	twoDim = nil

	if bytes!=nil{
		buffer:=proto.NewBuffer(bytes)
		for {
			decodedBytes,err:=buffer.DecodeRawBytes(true)
			if err!=nil{
				//which indicates the end of bytes
				break
			}
			twoDim=append(twoDim,decodedBytes)
		}
	}

	return
}

func ToBytes(twoDim [][]byte) (bytes []byte){
	bytes = nil

	if twoDim!=nil{
		buffer:=proto.NewBuffer(nil)
		for _,buf := range twoDim{
			buffer.EncodeRawBytes(buf)
		}
		bytes=buffer.Bytes()
	}

	return
}
 
