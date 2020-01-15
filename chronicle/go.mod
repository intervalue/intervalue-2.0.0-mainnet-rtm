module chronicle

require (
	github.com/davecgh/go-spew v1.1.1
	github.com/golang/protobuf v1.3.2
	github.com/pkg/errors v0.8.1
	github.com/rs/zerolog v1.17.2
	golang.org/x/crypto v0.0.0-20190308221718-c2843e01d9a2
	google.golang.org/grpc v1.23.0
)

replace (
	golang.org/x/crypto => github.com/golang/crypto v0.0.0-20190123085648-057139ce5d2b
	golang.org/x/net => github.com/golang/net v0.0.0-20190620200207-3b0461eec859
	golang.org/x/sync => github.com/golang/sync v0.0.0-20190423024810-112230192c58
	golang.org/x/sys => github.com/golang/sys v0.0.0-20190215142949-d0b11bdaac8a
	golang.org/x/text => github.com/golang/text v0.3.0
	golang.org/x/tools => github.com/golang/tools v0.0.0-20190828213141-aed303cbaa74
	golang.org/x/xerrors => github.com/golang/xerrors v0.0.0-20190717185122-a985d3407aa7
	google.golang.org/genproto => github.com/google/go-genproto v0.0.0-20200108215221-bd8f9a0ef82f
	google.golang.org/grpc => github.com/grpc/grpc-go v1.26.0
)
