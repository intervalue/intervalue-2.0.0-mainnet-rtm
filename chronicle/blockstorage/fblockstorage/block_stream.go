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
	"bufio"
	"github.com/golang/protobuf/proto"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/chronicle/log"
	"github.com/pkg/errors"
	"io"
	"os"
)

var BadEndOfBlock = errors.New("bad end of block content")

//attempt to read blocks from multiple files
type blocksInFilesStream struct {
	rootDir           string
	currentFileNum    int
	endFileNum        int
	currentFileStream *blocksInFileStream
}

//attempt to read blocks from a file
type blocksInFileStream struct {
	fileNum       int
	file          *os.File
	reader        *bufio.Reader
	currentOffset int64
}

type blockPlacementInfo struct {
	fileNum          int
	blockStartOffset int64
	blockBytesOffset int64
}

func newBlocksInFilesStream(dir string,startFileNum int,startOffset int64,endFileNum int) (*blocksInFilesStream,error){
	blocksInFileStreamP,err := newBlocksInFileStream(dir,startFileNum,startOffset)
	if err!=nil{
		return nil,err
	}

	return &blocksInFilesStream{dir,startFileNum,endFileNum,blocksInFileStreamP},nil
}

func newBlocksInFileStream(dir string,fileNum int,startOffset int64) (*blocksInFileStream,error){
	path := deriveBlockfilePath(dir,fileNum)
	var file *os.File
	var newPos int64
	var err error
	if file,err = os.OpenFile(path,os.O_RDONLY,600);err!=nil{
		return nil,errors.Wrapf(err,"error in openning file [%s]",path)
	}

	if newPos,err = file.Seek(startOffset,0);err!=nil{
		return nil,errors.Wrapf(err,"ould not seek block file [%s] to startOffset [%d]. New position = [%d]",path,startOffset,newPos)
	}

	bs:=&blocksInFileStream{fileNum,file,bufio.NewReader(file),startOffset}
	return bs,nil
}

func (bs *blocksInFileStream) close() error{
	return errors.WithStack(bs.file.Close())
}

func (bs *blocksInFileStream) nextBlockBytes() ([]byte,error){
	nextBlockBytes,_,err := bs.nextBlockAndPlacement()
	return nextBlockBytes,err
}

//note that if there is a crash,which leads to partially writing
func (bs *blocksInFileStream) nextBlockAndPlacement()([]byte,*blockPlacementInfo,error){
	var lengthBytes []byte
	var err error
	var fileInfo os.FileInfo
	moreContent:=true

	if fileInfo,err = bs.file.Stat();err!=nil{
		return nil,nil,errors.Wrap(err,"error in getting file state")
	}
	if bs.currentOffset == fileInfo.Size() {
		log.Debug().Msgf("finish reading file num[%d]",bs.fileNum)
		return nil,nil,nil
	}
	remainingBytes := fileInfo.Size() - bs.currentOffset
	minimumBytes:=8
	if remainingBytes<int64(minimumBytes){
		minimumBytes=int(remainingBytes)
		moreContent = false
	}
	log.Debug().Msgf("peek bytes [%d],remaining bytes [%d]",minimumBytes,remainingBytes)

	if lengthBytes,err = bs.reader.Peek(minimumBytes);err!=nil{
		return nil,nil,errors.Wrapf(err,"error in peek [%d] bytes",minimumBytes)
	}
	length,n := proto.DecodeVarint(lengthBytes)

	if n == 0{
		if !moreContent{
			return nil,nil,BadEndOfBlock
		}
		panic(errors.Errorf("Error in decoding varint bytes [%#v]", lengthBytes))
	}
	bytesExpected := int64(n) + int64(length)
	if bytesExpected > remainingBytes {
		return nil,nil,BadEndOfBlock
	}

	if _,err = bs.reader.Discard(n);err!=nil {
		return nil,nil,errors.Wrapf(err,"error in discarding [%d] bytes",n)
	}
	bBlock := make([]byte,length)
	if _,err = io.ReadAtLeast(bs.reader,bBlock,int(length));err != nil{
		log.Error().Msgf("error in reading [%d] bytes from file number [%d],error %s",length,bs.fileNum,err)
		return nil,nil,errors.Wrapf(err,"error in reading [%d] bytes from file number [%d]",length,bs.fileNum)
	}

	blockPlacementInfo := &blockPlacementInfo{
		fileNum:bs.fileNum,
		blockStartOffset:bs.currentOffset,
		blockBytesOffset:bs.currentOffset+int64(n)}
	bs.currentOffset +=int64(n)+int64(length)

	return bBlock,blockPlacementInfo,nil
}


func (bs *blocksInFilesStream) nextBlockAndPlacement()([]byte,*blockPlacementInfo,error){
	var blockByes []byte
	var blockPlacement *blockPlacementInfo
	var err error

	if blockByes,blockPlacement,err =bs.currentFileStream.nextBlockAndPlacement();err!=nil{
		log.Error().Msgf("error in reading next block bytes in file num[%d],%s",bs.currentFileNum,err)
		return nil,nil,err
	}
	if blockByes==nil && bs.currentFileNum<bs.endFileNum{
		if err = bs.moveToNextBlockfileStream();err!=nil{
			return nil,nil,err
		}
		return bs.nextBlockAndPlacement()
	}

	return blockByes,blockPlacement,err
}

func (bs *blocksInFilesStream) moveToNextBlockfileStream() error{
	var err error
	if err=bs.currentFileStream.close();err!=nil{
		return err
	}
	bs.currentFileNum++
	if bs.currentFileStream,err = newBlocksInFileStream(bs.rootDir,bs.currentFileNum,0);err!=nil{
		return err
	}

	return nil
}
