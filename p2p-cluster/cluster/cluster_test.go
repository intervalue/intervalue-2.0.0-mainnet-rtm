package cluster

import (
	"testing"
	//"time"
)

func TestStartAndShutdownCluster(t *testing.T) {
	c0, err := NewCluster("seed", "192.168.207.129", 33010)
	_ = c0
	if err != nil {
		t.Fatalf("fail to create cluster[:33010] %v", err)
		return
	}

	//time.Sleep(20 * time.Second)

	c0.Shutdown()
}

func TestStartAndJoinAndShutdownCluster(t *testing.T) {

}

func NewCluster(name string, addr string, port int) (*Cluster, error) {
	conf := &Config{
		Name:     name,
		BindAddr: addr,
		BindPort: port,
	}

	return Create(conf)
}
