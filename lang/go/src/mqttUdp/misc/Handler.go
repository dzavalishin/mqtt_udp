package misc

import (
	"errors"
	"log"
)

type Err int

const (
	Other     Err = 0
	Memory    Err = 1 ///< ENOMEM
	Establish Err = 2 ///< Unable to open socket or bind
	IO        Err = 3 ///< Net io
	Proto     Err = 4 ///< Broken packet
	Timeout   Err = 5
	Invalid   Err = 6 ///< Invalid parameter value.
)

func (s Err) String() string {
	switch s {
	case Other:
		return "other"
	case Memory:
		return "memory"
	case Establish:
		return "establish"
	case IO:
		return "IO"
	case Proto:
		return "proto"
	case Timeout:
		return "timeout"
	case Invalid:
		return "invalid"
	}
	return "unknown"
}

func DetailedGlobalErrorHandler(err Err, rc error, what string, where string) error {
	// TODO

	log.Printf("Error %s rc %s: %s in %s ", err.String(), rc.Error(), what, where)

	return rc
}

func GlobalErrorHandler(err Err, what string, where string) error {
	// TODO

	var rc = errors.New(what)

	return DetailedGlobalErrorHandler(err, rc, what, where)
}
