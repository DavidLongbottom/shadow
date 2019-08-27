package com.net.shadow;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;



public class Socks {
	NioEventLoopGroup bossGroup=new NioEventLoopGroup(1);
	NioEventLoopGroup workerGroup=new NioEventLoopGroup();
	
    
    private void init(int port) 
    {	
    	ServerBootstrap serverBootstrap=new ServerBootstrap();
	    try {
	    	serverBootstrap.group(bossGroup, workerGroup)
	    					.channel(NioServerSocketChannel.class)
	    					.option(ChannelOption.SO_BACKLOG,1024)
	    					.childHandler(new ChannelInitializer<SocketChannel>() {
	
								@Override
								protected void initChannel(SocketChannel ch) throws Exception {
									ch.pipeline()
									.addLast(Socks5ServerEncoder.DEFAULT)
									.addLast(new Socks5InitialRequestDecoder())
									.addLast(new Socks5InitialRequestHandler())
									// 注意这个项目不做密码的验证
									.addLast(new Socks5CommandRequestDecoder())
									.addLast(new Socks5CommandRequestHandler());
									System.out.println("new initializer"); 
								}
	    					    
							});
	    	System.out.println("going to bind ");
    		ChannelFuture future=serverBootstrap.bind(port).sync();
    		System.out.println("already bind");
        	future.channel().closeFuture().sync();
		}catch (Exception e) {
			e.printStackTrace();
		} 
    	finally {
    		bossGroup.shutdownGracefully();
    		workerGroup.shutdownGracefully();
    	}
    }
    
    public static void main( String[] args )
    {
    	Socks socks=new Socks();
        System.out.println( "Hello World!" );

    	socks.init(12345);
 	   	
        System.out.println( "Hello World!" );
    }
    
    public NioEventLoopGroup getWorkerGroup() {
    	return workerGroup;
    }
    
    
}
