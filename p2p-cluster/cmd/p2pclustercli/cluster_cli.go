package main

import (
	"fmt"
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/p2p-cluster/ic"
	"github.com/spf13/cobra"
	"log"
	"strconv"
	"strings"
	"time"
	"google.golang.org/grpc"
	"context"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @Description: cluster client tool,it's a good example to demonstrate how to invoke Cluster via grpc
 * @author: Francis.Deng
 * @version: V1.0
 */

var (
	icport int32

	rootCmd = &cobra.Command{
		Use: "p2pclustercli",
		Short:"p2p cluster client tool",
	}

	setMetaCmd = &cobra.Command{
		Use:"meta",
		Short:"set meta in the form of 'k1=v1 k2=v2 ...)'",
		Run: func(cmd *cobra.Command, args []string) {
			meta := make(map[string]string)
			if len(args) == 0 {
				cmd.Help()
				return
			}

			for _,arg := range args{
				if strings.Contains(arg,"="){
					parts := strings.Split(arg,"=")
					meta[parts[0]] = parts[1]
				}
			}

			boardcastMeta(meta)
		},
	}

	printP2PMember = &cobra.Command{
		Use:"printm",
		Short:"print alive or suspected member",
		Run: func(cmd *cobra.Command, args []string) {
			responseFindAliveMembers,responseFindSuspectedMembers := getAliveAndSuspectedFindMembers()

			if responseFindAliveMembers != nil{
				print("alive",responseFindAliveMembers)
			}
			if responseFindSuspectedMembers != nil{
				print("suspected",responseFindSuspectedMembers)
			}

		},
	}
)

func print(tp string,responseFindMembers *ic.ResponseFindMembers){
	findMembers := responseFindMembers.FindMember
	for _,findMember := range findMembers {
		fmt.Printf("find %s node: %s %v\n",tp,findMember.Addr,findMember.Meta)
	}
	fmt.Println("")
}

func init(){
	rootCmd.PersistentFlags().Int32Var(&icport,"icp",0,"inter communication port")

	rootCmd.AddCommand(setMetaCmd,printP2PMember)
}

//
func main() {
	rootCmd.Execute()
}

func boardcastMeta(meta map[string]string){
	conn,err := grpc.Dial(":"+strconv.Itoa(int(icport)),grpc.WithInsecure())
	if err != nil {
		log.Fatalf("did not connect: %v", err)
	}
	defer conn.Close()
	cc :=ic.NewClusterClient(conn)

	ctx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()

	requestUpdateMetaPtr:=new(ic.RequestUpdateMeta)

	for k,v := range meta{
		m := ic.MetaData{
			Key:k,
			Value:v,
		}

		requestUpdateMetaPtr.Meta=append(requestUpdateMetaPtr.Meta,&m)
	}
	cc.UpdateMeta(ctx,requestUpdateMetaPtr)
}

func getAliveAndSuspectedFindMembers()(responseFindAliveMembers *ic.ResponseFindMembers,responseFindSuspectedMembers *ic.ResponseFindMembers){
	conn,err := grpc.Dial(":"+strconv.Itoa(int(icport)),grpc.WithInsecure())
	if err != nil {
		log.Fatalf("did not connect: %v", err)
	}
	defer conn.Close()
	cc :=ic.NewClusterClient(conn)

	ctx, cancel := context.WithTimeout(context.Background(), time.Second)
	defer cancel()

	responseFindAliveMembers,_ = cc.FindAliveMembers(ctx,&ic.RequestFindMembers{})
	responseFindSuspectedMembers,_ = cc.FindSuspectedMembers(ctx,&ic.RequestFindMembers{})

	return
}
