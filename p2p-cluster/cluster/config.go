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
}

func DefaultConfig() *Config {
	conf := &Config{}

	conf.ProbeTimeout = 25 * time.Second
	conf.ProbeInterval = 80 * time.Second

	return conf
}
