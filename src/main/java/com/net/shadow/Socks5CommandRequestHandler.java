package com.net.shadow;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5AddressType;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;

public class Socks5CommandRequestHandler extends SimpleChannelInboundHandler<DefaultSocks5CommandRequest> {

	@Override
	protected void channelRead0(final ChannelHandlerContext pcChannelCtx, DefaultSocks5CommandRequest pcCmdRequest) throws Exception {
		System.out.println("start to read connect");
		//pcChannelCtx 是在socks 中创建的那个channel, 连通的是pc 到代理
				Bootstrap bootstrap=new Bootstrap();
				bootstrap.group(pcChannelCtx.channel().eventLoop())
						 .channel(NioSocketChannel.class)
						 .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
						 .handler(new ChannelInitializer<SocketChannel>() {
							 // 注意这个channel 是新创建的channel,这个channel就是下面b.connect获得future对应的channel是同一个。
							@Override
							protected void initChannel(SocketChannel ch) throws Exception {
								ch.pipeline().addLast(new ProxyToPC(pcChannelCtx));
							}
						  });
				System.out.println("begin connect"+pcCmdRequest.dstAddr()+pcCmdRequest.dstPort());
				ChannelFuture future=bootstrap.connect(pcCmdRequest.dstAddr(),pcCmdRequest.dstPort());
				future.addListener(new ChannelFutureListener() {
					
					public void operationComplete(ChannelFuture future) throws Exception {

						if(future.isSuccess()) {
							System.out.print("connect success");
							// 在这里建立新建立连接服务器的通道以及hander，即代理到目标地址之间的通道, 就是28行bootStrap handler 里面创建的那一个
							//SocketChannel proxyDestChannel=(SocketChannel) future.channel();
							pcChannelCtx.pipeline().addLast(new ProxyToDestHandler(future)); 
							//pcChannelCtx.pipeline().remove(Socks5CommandRequestHandler.this);
							
							pcChannelCtx.writeAndFlush(new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4));
							
						}else {
							System.out.print("connect failure");
							pcChannelCtx.writeAndFlush(new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, Socks5AddressType.IPv4));
							pcChannelCtx.close();
						}
						
					}
				});			
	}

	
	private class ProxyToDestHandler extends ChannelInboundHandlerAdapter{
		
		private ChannelFuture future;
		public ProxyToDestHandler(ChannelFuture future) {
			this.future=future;
		}
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			System.out.println("将客户端的消息转发给目标服务器端");
			future.channel().writeAndFlush(msg);
		}		
	}

	private class ProxyToPC extends ChannelInboundHandlerAdapter{
		private ChannelHandlerContext pcProxyContext;
		public ProxyToPC(ChannelHandlerContext pcProxyContext) {
			this.pcProxyContext=pcProxyContext;
		}
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			pcProxyContext.writeAndFlush(msg);
		}
		
		
	}
}
