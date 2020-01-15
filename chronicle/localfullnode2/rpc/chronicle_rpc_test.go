package rpc

import (
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/log"
	"testing"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 1/9/20 10:53 PM
 * @Description: try to verify the communication mechanism is all right,along with
 * {@code ChronicleServicesServerTest.java} which is listening to port 8980 locally
 */


type Logger struct {
}

func (l Logger)OnMessages(messages *StringArray){
	for _,message := range messages.Data{
		log.Info().Msgf("message: %s",message)

	}
}

func (l Logger)OnError(e error){
	log.Info().Msgf("error: %s",e)
}

func (l Logger)OnFinished(){
	log.Info().Msgf("finished")
}
 
func TestChronicleDumperServices(t *testing.T){
	logger:=Logger{}
	chronicleDumperRestorerRPC,err := NewDumperRPCClient("127.0.0.1",8980)
	if err!=nil{
		t.Errorf("error in creating rpc client: %s",err)
		return
	}

	chronicleDumperRestorerRPC.GetStreamMessageHashes(logger)
	chronicleDumperRestorerRPC.GetStreamSysMessageHashes(logger)
	messages,err:=chronicleDumperRestorerRPC.GetMessageStreamBy([]string{"leg","uniform"})
	if err!=nil{
		log.Info().Msgf("%s",err)
		return
	}

	for _,message:=range messages{
		log.Info().Msgf("%s",string(message))
	}

	chronicleDumperRestorerRPC.Close()

}

func TestChronicleRestorerServices(t *testing.T){
	messages:=[][]byte{[]byte("printer"),[]byte("scanner")}
	chronicleDumperRestorerRPC,err := NewDumperRPCClient("127.0.0.1",8980)
	if err!=nil{
		t.Errorf("error in creating rpc client: %s",err)
		return
	}

	err=chronicleDumperRestorerRPC.PersistStream(messages)
	if err!=nil{
		t.Errorf("error in chronicleDumperRestorerRPC.PersistStream: %s",err)
		return
	}

	sysMessages:=[][]byte{[]byte("God"),[]byte("Landlord")}
	err=chronicleDumperRestorerRPC.PersistStreamSys(sysMessages)
	if err!=nil{
		t.Errorf("error in chronicleDumperRestorerRPC.PersistStreamSys: %s",err)
		return
	}

	chronicleDumperRestorerRPC.Close()
}