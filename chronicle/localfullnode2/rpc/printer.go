package rpc

import "github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/log"

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 1/18/20 6:29 PM
 * @Description:
 */

type Printer struct {
}

func (l Printer) OnMessages(messages *StringArray) {
	for _, message := range messages.Data {
		log.Info().Msgf("message: %s", message)

	}
}

func (l Printer) OnError(e error) {
	log.Info().Msgf("error: %s", e)
}

func (l Printer) OnFinished() {
	log.Info().Msgf("finished")
}
