package cluster

import(
	"testing"
	"bytes"
)


func TestEncodeAndDecodeMap(t *testing.T){
	var m map[string]string = make(map[string]string)

	var me meta
	var oneAnother meta
	var buffer *bytes.Buffer
	var err error

	m["king"]="Alan"
	m["queen"]="Athen"


	me = meta{SeqNo:99,Addr:"127.0.0.1",Items:m}

	buffer,err = encode(pingMsg,&me)
	if err != nil{
		t.Error(err)
	}

	buf := buffer.Bytes()[1:]
	err = decode(buf,&oneAnother)
	t.Logf("%v",oneAnother.Items)
}