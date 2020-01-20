module p2p-cluster

go 1.12

replace (
	cloud.google.com/go => github.com/googleapis/google-cloud-go v0.51.0
	golang.org/x/crypto => github.com/golang/crypto v0.0.0-20200117160349-530e935923ad
	golang.org/x/exp => github.com/golang/exp v0.0.0-20200119233911-0405dc783f0a
	golang.org/x/image => github.com/golang/image v0.0.0-20200119044424-58c23975cae1
	golang.org/x/lint => github.com/golang/lint v0.0.0-20191125180803-fdd1cda4f05f
	golang.org/x/mobile => github.com/golang/mobile v0.0.0-20200119070023-0c13fd316633
	golang.org/x/net => github.com/golang/net v0.0.0-20200114155413-6afb5195e5aa
	golang.org/x/oauth2 => github.com/golang/oauth2 v0.0.0-20200107190931-bf48bf16ab8d
	golang.org/x/sync => github.com/golang/sync v0.0.0-20190911185100-cd5d95a43a6e
	golang.org/x/sys => github.com/golang/sys v0.0.0-20200117145432-59e60aa80a0c
	golang.org/x/text => github.com/golang/text v0.3.2
	golang.org/x/time => github.com/golang/time v0.0.0-20191024005414-555d28b269f0
	golang.org/x/tools => github.com/golang/tools v0.0.0-20200119215504-eb0d8dd85bcc
	google.golang.org/api => github.com/googleapis/google-api-go-client v0.15.0
	google.golang.org/appengine => github.com/golang/appengine v1.6.5
	google.golang.org/genproto => github.com/googleapis/go-genproto v0.0.0-20200117163144-32f20d992d24
	google.golang.org/grpc => github.com/grpc/grpc-go v1.26.0
)

require (
	github.com/golang/protobuf v1.3.2
	github.com/spf13/cobra v0.0.5+incompatible
	golang.org/x/net v0.0.0-20191209160850-c0dbc17a3553
	google.golang.org/grpc v1.26.0
)
