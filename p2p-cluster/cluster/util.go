package cluster

import (
	"bytes"
	"encoding/gob"
)

func encode(mt msgType, in interface{}) (*bytes.Buffer, error) {
	var merged bytes.Buffer

	mainBody := bytes.NewBuffer(nil)
	encoder := gob.NewEncoder(mainBody)

	if err := encoder.Encode(in); err != nil {
		return nil, err
	}

	merged.WriteByte(byte(mt))
	_, err := merged.Write(mainBody.Bytes())

	return &merged, err
}

func decode(data []byte, out interface{}) error {
	buf := bytes.NewBuffer(data)
	decoder := gob.NewDecoder(buf)

	return decoder.Decode(out)
}
