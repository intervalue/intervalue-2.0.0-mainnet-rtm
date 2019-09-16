package main

import (
	"github.com/intervalue/intervalue-2.0.0-mainnet-rtm/ips/handlers"
	"github.com/julienschmidt/httprouter"
	"log"
	"net/http"
	"os"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Sep 11, 2019
 *
 */

// optinal environment varible: "IPS-PORT"
func main(){
	router := httprouter.New()
	router.GET("/",handlers.GetIp)

	router.NotFound = http.HandlerFunc(handlers.NotFound)
	router.MethodNotAllowed = http.HandlerFunc(handlers.MethodNotAllowed)

	port := os.Getenv("IPS-PORT")
	if port == ""{
		port = "30911"
	}

	log.Println("Starting http server on port :",port)
	log.Fatal(http.ListenAndServe(":"+port,router))
}
