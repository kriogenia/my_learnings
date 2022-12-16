package api

import (
	"log"

	"github.com/gin-gonic/gin"
	db "github.com/kriogenia/my_learnings/go_bank/db/sqlc"
)

// Server serves HTTP requests for the banking service
type Server struct {
	store  db.Store
	router *gin.Engine
}

// NewServer creates a new HTTP server and setup routing
func NewServer(store db.Store) *Server {
	server := &Server{store: store}
	router := gin.Default()

	router.GET("/accounts/:id", server.getAccountById)
	router.GET("/accounts", server.getAccounts)
	router.POST("/accounts", server.createAccount)

	server.router = router
	return server
}

// Start run the HTTP server on a specific address
func (server *Server) Start(address string) error {
	return server.router.Run(address)
}

func handleError(ctx *gin.Context, err error, code int) bool {
	if err != nil {
		log.Println(err)
		ctx.JSON(code, gin.H{"error": err.Error()})
		return true
	}
	return false
}
