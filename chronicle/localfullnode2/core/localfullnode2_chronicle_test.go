package core

import (
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/log"
	"os"
	"testing"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 1/15/20 7:14 PM
 * @Description: try to verify the communication mechanism and wrappedMessage are all right,along with
 * {@code ChronicleDumperRestorerEmulatorTest.java} which is listening to port 8980 locally
 */

func TestChronicleRound(t *testing.T) {
	chronicDir, err := getChronicleDir()
	if err != nil {
		log.Fatal().Msgf("error in getChronicleDir: %s", err)
	}

	localfullnode2Chronicle := NewLocalfullnode2Chronicle(chronicDir, "127.0.0.1", 8980)
	localfullnode2Chronicle.HashesInMemoryDump()
	localfullnode2Chronicle.Restore()
	//check out localfullnode2 console that jdk version and jet fighter model are viewed

	localfullnode2Chronicle.Shutdown()

}

func getChronicleDir() (string, error) {
	home, _ := os.UserHomeDir()
	dir := home + string(os.PathSeparator) + "chronicleEnv"

	fileInfo, err := os.Stat(dir)
	if err != nil {
		return "", err
	}

	if os.IsExist(err) || fileInfo.IsDir() {
		os.RemoveAll(dir)
	}

	os.MkdirAll(dir, os.ModePerm)

	return dir, nil
}
