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
