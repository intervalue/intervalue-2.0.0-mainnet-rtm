package cluster

import (
	"log"
	"time"
)

type Config struct {
	Name string

	BindAddr string
	BindPort int

	ProbeInterval time.Duration
	ProbeTimeout  time.Duration

	Logger *log.Logger

	TCPTimeout time.Duration
}

func DefaultConfig() *Config {
	conf := &Config{}

	conf.ProbeTimeout = 10 * time.Second
	conf.ProbeInterval = 20 * time.Second

	conf.TCPTimeout = 30 * time.Second

	return conf
}
