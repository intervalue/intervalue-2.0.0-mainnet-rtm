package main

import (
	"flag"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/localfullnode2/core"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/log"
	"os"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 1/18/20 6:09 PM
 * @Description: main entry with some parameters[cAddress,cPort,as]
 */

func main() {
	var chronicleServerAddr string
	var chronicleServerPort int
	var as string
	var verbose bool

	flag.StringVar(&chronicleServerAddr, "cAddress", "127.0.0.1", "the address in which chronicle server is located")
	flag.IntVar(&chronicleServerPort, "cPort", 36791, "the port which chronicle server is listening to")
	flag.StringVar(&as, "as", "dumper", "take an alternative between 'dumper' and 'restorer'")
	flag.BoolVar(&verbose, "o", false, "verbose execution output")

	flag.Parse()

	if verbose {
		log.Enable()
	} else {
		log.Disable()
	}

	if as == "dumper" {
		asDumper(chronicleServerAddr, chronicleServerPort)
	} else if as == "restorer" {
		asRestorer(chronicleServerAddr, chronicleServerPort)
	} else {
		log.Error().Msgf("error in choosing model: as dumper or as restorer")
	}
}

func asDumper(addr string, port int) {
	chronicDir, err := getChronicleDir()
	if err != nil {
		log.Fatal().Msgf("error in getChronicleDir: %s", err)
		return
	}

	localfullnode2Chronicle := core.NewLocalfullnode2Chronicle(chronicDir, addr, port)
	defer localfullnode2Chronicle.Shutdown()
	localfullnode2Chronicle.HashesInMemoryDump()

}

func asRestorer(addr string, port int) {
	chronicDir, err := getChronicleDirOnly()
	if err != nil {
		log.Fatal().Msgf("error in getChronicleDir: %s", err)
		return
	}

	localfullnode2Chronicle := core.NewLocalfullnode2Chronicle(chronicDir, addr, port)
	defer localfullnode2Chronicle.Shutdown()
	localfullnode2Chronicle.Restore()
}

//clean the dir if existed
func getChronicleDir() (string, error) {
	home, _ := os.UserHomeDir()
	dir := home + string(os.PathSeparator) + "chronicleEnv"

	fileInfo, err := os.Stat(dir)
	if os.IsNotExist(err) {
		os.MkdirAll(dir, os.ModePerm)
		return dir, nil
	}
	//if err != nil {
	//	return "", err
	//}

	if os.IsExist(err) || fileInfo.IsDir() {
		os.RemoveAll(dir)
	}

	os.MkdirAll(dir, os.ModePerm)

	return dir, nil
}

func getChronicleDirOnly() (string, error) {
	home, _ := os.UserHomeDir()
	dir := home + string(os.PathSeparator) + "chronicleEnv"

	_, err := os.Stat(dir)
	if os.IsNotExist(err) {
		return "", err
	}

	return dir, nil
}
