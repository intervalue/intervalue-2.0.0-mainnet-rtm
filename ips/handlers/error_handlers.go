package handlers

import "net/http"

/**
 *
 * Copyright © INVE FOUNDATION. All rights reserved.
 *
 * @author Francis.Deng [francis_xiiiv@163.com]
 * @date Sep 11, 2019
 *
 */

func MethodNotAllowed(w http.ResponseWriter, r *http.Request) {
	w.WriteHeader(405)
}

// NotFound renders a not found response for invalid API endpoints.
func NotFound(w http.ResponseWriter, r *http.Request) {
	w.WriteHeader(404)
}