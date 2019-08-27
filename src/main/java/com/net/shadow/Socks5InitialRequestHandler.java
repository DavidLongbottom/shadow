package com.net.shadow;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksVersion;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5InitialResponse;

public class Socks5InitialRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5InitialRequest> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DefaultSocks5InitialRequest msg) throws Exception {
		
		System.out.println("start to read init request");

		if(msg.decoderResult().isFailure()) {
			System.out.println("不是ss5协议");
			ctx.fireChannelRead(msg);
		} else {
			if(msg.version().equals(SocksVersion.SOCKS5)) {	
				System.out.println("sock5 version");
				Socks5InitialResponse initialResponse = new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH);
				ctx.writeAndFlush(initialResponse);
			}else {
				System.out.println("not sock5");	
			}
		}
	}
}
