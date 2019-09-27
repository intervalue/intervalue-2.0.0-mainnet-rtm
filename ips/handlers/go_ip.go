package handlers

import (
	"fmt"
	"github.com/julienschmidt/httprouter"
	"net"
	"net/http"
	"strings"
)

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Sep 11, 2019
 *
 */

//grab first ip address in the X-Forwarded-For header
//or http remote address
func GetIp(w http.ResponseWriter,r *http.Request,_ httprouter.Params){
	var ip string
	err:=r.ParseForm()
	if err!=nil{
		panic(err)
	}

	ipInHead := strings.Split(r.Header.Get("X-Forwarded-For"),",")[0]
	if ipInHead != ""{
		ip = net.ParseIP(ipInHead).String()
	} else {
		ip = strings.Split(r.RemoteAddr,":")[0]
	}

	w.Header().Set("Content-Type", "text/plain")
	fmt.Fprintf(w,ip)
}
