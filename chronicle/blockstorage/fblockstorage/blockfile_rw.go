package fblockstorage

import (
	"github.com/pkg/errors"
	"os"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author: Francis.Deng[francis_xiiiv@163.com]
 * @version: V1.0
 * @Date: 12/27/19 1:06 AM
 * @Description: 
 */ 
 
type blockFileWriter struct {
	filePath string
	file *os.File
}

func newBlockFileWriter(filePath string) (*blockFileWriter,error){
	w:=&blockFileWriter{filePath:filePath}

	return w,w.open()
}

func (w *blockFileWriter) truncateFile(sz int) error{
	stat,err:=w.file.Stat()
	if err!=nil{
		errors.Wrapf(err,"error in truncating file[%s] to size[%d]",w.filePath,sz)
	}
	if stat.Size()>int64(sz) {
		w.file.Truncate(int64(sz))
	}

	return nil
}

func (w *blockFileWriter) close() error{
	return errors.WithStack(w.file.Close())
}

func (w *blockFileWriter) open() error{
	file,err:=os.OpenFile(w.filePath,os.O_RDWR|os.O_APPEND|os.O_CREATE,0660)
	if err!=nil{
		return errors.Wrapf(err,"error in open block file [%s]",w.filePath)
	}

	w.file = file
	return nil
}

func (w *blockFileWriter) cloe() error{
	return errors.WithStack(w.cloe())
}

func (w *blockFileWriter) append(b []byte, sync bool) error {
	_, err := w.file.Write(b)
	if err != nil {
		return err
	}
	if sync {
		return w.file.Sync()
	}
	return nil
}