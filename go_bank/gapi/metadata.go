package gapi

import (
	"context"

	"google.golang.org/grpc/metadata"
	"google.golang.org/grpc/peer"
)

const (
	grpcGatewayUserAgent = "grpcgateway-user-agent"
	userAgentHeader      = "user-agent"
	xForwardedForHeader  = "x-forwarded-for"
)

type Metadata struct {
	UserAgent string
	ClientIp  string
}

func (server *Server) extractMetada(ctx context.Context) *Metadata {
	mtdt := &Metadata{}

	if md, ok := metadata.FromIncomingContext(ctx); ok {
		if userAgents := md.Get(grpcGatewayUserAgent); len(userAgents) > 0 {
			mtdt.UserAgent = userAgents[0]
		}

		if userAgents := md.Get(userAgentHeader); len(userAgents) > 0 {
			mtdt.UserAgent = userAgents[0]
		}

		if clientIPs := md.Get(xForwardedForHeader); len(clientIPs) > 0 {
			mtdt.ClientIp = clientIPs[0]
		}

		if peer, ok := peer.FromContext(ctx); ok {
			mtdt.ClientIp = peer.Addr.String()
		}
	}

	return mtdt
}