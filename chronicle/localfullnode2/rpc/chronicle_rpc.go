package rpc

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 1/8/20 7:47 PM
 * @Description: encapsulate how to build grpc client(grpc access point by chronicle)
 */

import (
	"context"
	"fmt"
	"github.com/golang/protobuf/ptypes/empty"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/log"
	"github.com/pkg/errors"
	"google.golang.org/grpc"
	"io"
)

var (
	ctx=context.Background()
	_ IChronicleDumperRestorerRPC=&ChronicleDumperRestorerRPC{}
)



type IChronicleDumperRestorerRPC interface {
	GetStreamMessageHashes(handler IAfterStreamRecvHandler) error
	GetStreamSysMessageHashes(handler IAfterStreamRecvHandler) error
	GetMessageStreamBy(messageHashes []string) ([][]byte,error)

	PersistStream([][]byte) error
	PersistStreamSys([][]byte) error

	Close()
}




type IAfterStreamRecvHandler interface {
	OnMessages(messages *StringArray)
	OnError(e error)
	OnFinished()
}


//support both non-steam(client) and steam(steamClient) communication
type ChronicleDumperRestorerRPC struct {
	steamClient ChronicleDumperRestorerStreamRPCClient
	client      ChronicleDumperRestorerRPCClient
	cc *grpc.ClientConn
}

//attempt to connect addr:port via grpc
func NewDumperRPCClient(addr string,port int) (*ChronicleDumperRestorerRPC,error){
	cc, err := grpc.Dial(fmt.Sprintf("%s:%d",addr,port), grpc.WithInsecure())
	if err!=nil{
		return nil,errors.Wrapf(err,"fail in initialization")
	}
	//defer cc.Close()

	sc:=NewChronicleDumperRestorerStreamRPCClient(cc)
	c:=NewChronicleDumperRestorerRPCClient(cc)

	dumperRestorer:=&ChronicleDumperRestorerRPC{sc,c,cc}

	return dumperRestorer,nil
}

//dumper function

func (dumperRestorer *ChronicleDumperRestorerRPC) GetStreamMessageHashes(handler IAfterStreamRecvHandler) error{
	stream,err:=dumperRestorer.steamClient.GetMessageHashes(ctx,new(empty.Empty))
	if err!=nil{
		return errors.Wrapf(err,"dumperRestorer.steamClient.GetStreamMessageHashes: %s",err)
	}

	for{
		messageHashes,err:=stream.Recv()
		if err==io.EOF{
			handler.OnFinished()
			break
		}
		if err!=nil{
			handler.OnError(err)
			continue
		}

		handler.OnMessages(messageHashes)
	}

	return nil
}

func (dumperRestorer *ChronicleDumperRestorerRPC) GetStreamSysMessageHashes(handler IAfterStreamRecvHandler) error{
	stream,err:=dumperRestorer.steamClient.GetSysMessageHashes(ctx,new(empty.Empty))
	if err!=nil{
		return errors.Wrapf(err,"dumperRestorer.steamClient.GetMessageHashes: %s",err)
	}

	for{
		messageHashes,err:=stream.Recv()
		if err==io.EOF{
			handler.OnFinished()
			break;
		}
		if err!=nil{
			handler.OnError(err)
			continue
		}

		handler.OnMessages(messageHashes)
	}

	return nil
}

func (dumperRestorer *ChronicleDumperRestorerRPC) GetMessageStreamBy(messageHashes []string) ([][]byte,error){
	messagesHashesArray:=StringArray{
		Data:messageHashes,
	}

	byteStream,err:=dumperRestorer.client.GetMessageStreamBy(ctx,&messagesHashesArray)
	if err!=nil{
		return nil,errors.Wrapf(err,"dumperRestorer.client.GetMessageStreamBy: %s",err)
	}

	return byteStream.Data,nil
}

func (dumperRestorer *ChronicleDumperRestorerRPC) Close(){
	dumperRestorer.cc.Close()
}

//restorer function

func (dumperRestorer *ChronicleDumperRestorerRPC) PersistStream(bytesBytes [][]byte) error{
	stream,err:=dumperRestorer.steamClient.Persist(context.Background())
	byteStream:=&ByteStream{}
	if err!=nil{
		return errors.Wrapf(err,"dumperRestorer.steamClient.PersistStream: %s",err)
	}

	for _,bytes:=range bytesBytes{
		byteStream.Data=append(byteStream.Data,bytes)
	}

	if (len(bytesBytes)>0){
		stream.Send(byteStream)
	}

	_,err=stream.CloseAndRecv()
	if err!= nil{
		log.Error().Msgf("error in stream.CloseSend: %s",err)
		return errors.Wrapf(err,"error in stream.CloseSend: %s",err)
	}

	return nil
}

func (dumperRestorer *ChronicleDumperRestorerRPC) PersistStreamSys(bytesBytes [][]byte) error{
	stream,err:=dumperRestorer.steamClient.PersistSys(context.Background())
	byteStream:=&ByteStream{}
	if err!=nil{
		return errors.Wrapf(err,"dumperRestorer.steamClient.PersistStreamSys: %s",err)
	}

	for _,bytes:=range bytesBytes{
		byteStream.Data=append(byteStream.Data,bytes)
	}

	if (len(bytesBytes)>0){
		stream.Send(byteStream)
	}

	_,err=stream.CloseAndRecv()
	if err!= nil{
		log.Error().Msgf("error in %v.CloseSend: %s",stream,err)
		return errors.Wrapf(err,"error in %v.CloseSend: %s",stream,err)
	}

	return nil
}