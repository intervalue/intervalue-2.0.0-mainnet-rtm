package core

import (
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/localfullnode2/rpc"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/log"
	"strings"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 1/13/20 10:58 PM
 * @Description: doubtful whether worked well in massive messages case
 */

var (
	//messagesHashes    []string
	//sysMessagesHashes []string

	hashes []string
)

type lazyMessageHashesHandler struct {
}

func (h lazyMessageHashesHandler) OnMessages(mHashes *rpc.StringArray) {
	for _, mHash := range mHashes.Data {
		log.Debug().Msgf("message hash: %s", mHash)
		//messagesHashes = append(messagesHashes, mHash)
		hashes = append(hashes, mHash)

	}
}

func (h lazyMessageHashesHandler) OnError(e error) {
	log.Error().Msgf("error: %s", e)
}

func (h lazyMessageHashesHandler) OnFinished() {
	//log.Info().Msgf("finished")
}

type lazySysMessageHashesHandler struct {
}

func (h lazySysMessageHashesHandler) OnMessages(mHashes *rpc.StringArray) {
	for _, mHash := range mHashes.Data {
		log.Debug().Msgf("system message hash: %s", mHash)
		//sysMessagesHashes = append(sysMessagesHashes, mHash)
		hashes = append(hashes, mHash)

	}
}

func (h lazySysMessageHashesHandler) OnError(e error) {
	log.Error().Msgf("error: %s", e)
}

func (h lazySysMessageHashesHandler) OnFinished() {
	log.Info().Msgf("finished")
}

//collect all hashes(of message and system message) in memory slice
//retrieve wrapped message list by hashes
//meanwhile,dump list into chronicle
func (localfullnode2Chronicle *Localfullnode2Chronicle) HashesInMemoryDump() {
	messagesHashes := lazyMessageHashesHandler{}

	const _accumulateThreshold int = 5

	err := localfullnode2Chronicle.GetStreamMessageHashes(messagesHashes)
	if err != nil {
		log.Error().Msgf("GetStreamMessageHashe error:[%s]", err)
		return
	}

	localfullnode2Chronicle.GetStreamSysMessageHashes(messagesHashes)
	if err != nil {
		log.Error().Msgf("GetStreamSysMessageHashes error:[%s]", err)
		return
	}
	//after {@var hashes} was filled with hashes

	if len(hashes) > 0 {
		_lastOneIndex := len(hashes) - 1
		hashesArray := []string{}

		for index, messageHash := range hashes {
			if (index+1)%_accumulateThreshold == 0 || index == _lastOneIndex {
				hashesArray = append(hashesArray, messageHash)

				messages, err := localfullnode2Chronicle.GetMessageStreamBy(hashesArray)
				if err != nil {
					log.Error().Msgf("localfullnode2Chronicle.GetMessageStreamBy error:%s", err)
					return
				}

				//attemptToPrintWrappedMessages(messages)
				//attemptToPrintRawWrappedMessages(messages)

				if messages == nil {
					log.Error().Msgf("No any message body was found this time: [%s]", strings.Join(hashesArray, ","))
				} else {
					localfullnode2Chronicle.AddBlockByTransactions(messages)
				}
				hashesArray = hashesArray[0:0]
			} else {
				hashesArray = append(hashesArray, messageHash)
			}

		}

		localfullnode2Chronicle.done()
	}

	//if len(sysMessagesHashes) > 0 {
	//	_lastOneIndex := len(sysMessagesHashes) - 1
	//	hashesArray := []string{}
	//
	//	for index, messageHash := range sysMessagesHashes {
	//		if (index+1)%_accumulate == 0 || index == _lastOneIndex {
	//			hashesArray = append(hashesArray, messageHash)
	//
	//			messages, err := localfullnode2Chronicle.GetMessageStreamBy(hashesArray)
	//			if err != nil {
	//				log.Error().Msgf("localfullnode2Chronicle.GetMessageStreamBy error:%s", err)
	//				return
	//			}
	//
	//			localfullnode2Chronicle.AddBlockByTransactions(messages)
	//			hashesArray = hashesArray[0:0]
	//		} else {
	//			hashesArray = append(hashesArray, messageHash)
	//		}
	//
	//	}
	//}
}

func attemptToPrintWrappedMessages(messages [][]byte) {
	for _, message := range messages {
		wrappedMessage := &rpc.WrappedMessage{}
		err := wrappedMessage.XXX_Unmarshal(message)

		if err == nil {
			log.Debug().Msgf("%d - %s", wrappedMessage.MessageType, string(wrappedMessage.MessageBody))
		} else {
			log.Error().Msgf("not a WrappedMessage obviously,quit the process")
			break
		}

	}

}

func attemptToPrintRawWrappedMessages(messages [][]byte) {
	for _, message := range messages {
		log.Debug().Msgf("%s", string(message))

	}

}
