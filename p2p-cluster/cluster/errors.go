package cluster

import (
	"fmt"
)

type NoPongError struct {
	Addr string
}

func (npe NoPongError) Error() string {
	return fmt.Sprintf("No response from node %s", npe.Addr)
}
