package fblockstorage

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @Description:
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 */

import (
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/log"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/vo"
	"github.com/pkg/errors"
	"io/ioutil"
	"os"
	"strconv"
	"strings"
)


func constructCheckpointInfoFromBlockFiles(rootDir string) (*checkpointInfo, error) {
	var lastFileNum int
	var err error
	var lastBlockBytes []byte
	var lastBlock *vo.Block
	var lastBlockNumber uint64
	var offset int64
	var numBlocks int

	if lastFileNum,err=retrieveLastFileSuffix(rootDir);err!=nil{
		return nil,err
	}
	log.Debug().Msgf("found last file number [%d]",lastFileNum)

	if lastFileNum == -1 {
		cp := &checkpointInfo{0,0,true,0}
		log.Debug().Msg("no block file found")
		return cp,nil
	}

	fileInfo := getFileInfoOrPanic(rootDir,lastFileNum)
	if lastBlockBytes,offset,numBlocks,err = scanForLastCompleteBlock(rootDir,lastFileNum,0);err!=nil{
		log.Error().Msgf("error in scanning last file [%s],%s",fileInfo.Name(),err)
	}

	if lastBlockBytes != nil{
		if lastBlock,err = deserializeBlock(lastBlockBytes);err!=nil{
			log.Debug().Msgf("error in deserializing last block,%s",err)
			return nil,err
		}
		lastBlockNumber = lastBlock.Header.Number
	}

	cpInfo := &checkpointInfo{
		lastBlockNumber:          lastBlockNumber,
		latestFileChunksize:      int(offset),
		latestFileChunkSuffixNum: lastFileNum,
		isChainEmpty:             lastFileNum == 0 && numBlocks == 0,
	}

	return cpInfo, nil
}

//while file has biggest suffix number in the root directory
func retrieveLastFileSuffix(root string) (int,error){
	lastFileNum := -1
	filesInfo,err := ioutil.ReadDir(root)
	if err != nil{
		return lastFileNum,errors.Wrapf(err,"error in reading dir [%s]",root)
	}

	for _,fileInfo := range filesInfo{
		name := fileInfo.Name()
		if fileInfo.IsDir() || !isBlockFileName(name){
			log.Debug().Msgf("skip file [%s]",name)
			continue
		}

		fileSuffix := strings.TrimPrefix(name,BLOCKFILE_PREFIX)
		fileNum,err := strconv.Atoi(fileSuffix)
		if err != nil{
			return -1,err
		}

		if fileNum > lastFileNum{
			lastFileNum = fileNum
		}
	}

	return lastFileNum,err
}

func isBlockFileName(name string) bool{
	return strings.HasPrefix(name,BLOCKFILE_PREFIX)
}

func getFileInfoOrPanic(dir string,fileNum int) os.FileInfo{
	blockFilePath := deriveBlockfilePath(dir,fileNum)
	fileInfo,err := os.Lstat(blockFilePath)
	if err != nil{
		panic(errors.Wrapf(err,"error retrieving file info for file number %d",fileNum))
	}
	return fileInfo
}