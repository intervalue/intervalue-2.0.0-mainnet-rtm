
package utilities

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
	"io"
	"io/ioutil"
	"os"
	"path"
	"strings"

	"github.com/pkg/errors"
)


// CreateDirIfMissing creates a dir for dirPath if not already exists. If the dir is empty it returns true
func CreateDirIfMissing(dirPath string) (bool, error) {
	// if dirPath does not end with a path separator, it leaves out the last segment while creating directories
	if !strings.HasSuffix(dirPath, "/") {
		dirPath = dirPath + "/"
	}
	log.Debug().Msgf("CreateDirIfMissing [%s]", dirPath)
	logDirStatus("Before creating dir", dirPath)
	err := os.MkdirAll(path.Dir(dirPath), 0755)
	if err != nil {
		log.Debug().Msgf("Error creating dir [%s]", dirPath)
		return false, errors.Wrapf(err, "error creating dir [%s]", dirPath)
	}
	logDirStatus("After creating dir", dirPath)
	return DirEmpty(dirPath)
}

// DirEmpty returns true if the dir at dirPath is empty
func DirEmpty(dirPath string) (bool, error) {
	f, err := os.Open(dirPath)
	if err != nil {
		log.Debug().Msgf("Error opening dir [%s]: %+v", dirPath, err)
		return false, errors.Wrapf(err, "error opening dir [%s]", dirPath)
	}
	defer f.Close()

	_, err = f.Readdir(1)
	if err == io.EOF {
		return true, nil
	}
	err = errors.Wrapf(err, "error checking if dir [%s] is empty", dirPath)
	return false, err
}

// FileExists checks whether the given file exists.
// If the file exists, this method also returns the size of the file.
func FileExists(filePath string) (bool, int64, error) {
	fileInfo, err := os.Stat(filePath)
	if os.IsNotExist(err) {
		return false, 0, nil
	}
	if err != nil {
		return false, 0, errors.Wrapf(err, "error checking if file [%s] exists", filePath)
	}
	return true, fileInfo.Size(), nil
}

// ListSubdirs returns the subdirectories
func ListSubdirs(dirPath string) ([]string, error) {
	subdirs := []string{}
	files, err := ioutil.ReadDir(dirPath)
	if err != nil {
		return nil, errors.Wrapf(err, "error reading dir %s", dirPath)
	}
	for _, f := range files {
		if f.IsDir() {
			subdirs = append(subdirs, f.Name())
		}
	}
	return subdirs, nil
}

func logDirStatus(msg string, dirPath string) {
	exists, _, err := FileExists(dirPath)
	if err != nil {
		log.Error().Msgf("Error checking for dir existence")
	}
	if exists {
		log.Debug().Msgf("%s - [%s] exists", msg, dirPath)
	} else {
		log.Debug().Msgf("%s - [%s] does not exist", msg, dirPath)
	}
}
