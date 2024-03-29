package com.kuing.netty.reactor.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.io.BufferedInputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeneralSocketConnectNettyServerTest {


     static class Server {
         private final int port = 80;

         public Server() throws Exception {
             EventLoopGroup bossGroup = new NioEventLoopGroup();
             EventLoopGroup workerGroup = new NioEventLoopGroup();

             ServerBootstrap b = new ServerBootstrap();
             b.group(bossGroup, workerGroup).option(ChannelOption.SO_KEEPALIVE, true).channel(NioServerSocketChannel.class)
                     .option(ChannelOption.SO_BACKLOG, 128).handler(new LoggingHandler(LogLevel.INFO))
                     .childHandler(new ChannelInitializer<SocketChannel>() {
                         @Override
                         public void initChannel(SocketChannel sc) throws Exception {
                             sc.pipeline().addLast(new StringEncoder());// 发送字符串的编码器。
                             sc.pipeline().addLast(new StringDecoder());// 接收到字符串的解码器。
                             sc.pipeline().addLast(new MyServerHandler());
                         }
                     });

             // 绑定端口，开始接收进来的连接。
             ChannelFuture cf = b.bind(port).sync();

             // 等待服务器关闭Socket。
             cf.channel().closeFuture().sync();
         }

         class MyServerHandler extends ChannelInboundHandlerAdapter {

             private final Logger logger = Logger.getLogger(MyServerHandler.class.getName());

             @Override
             public void channelActive(ChannelHandlerContext ctx) throws Exception {
                 System.out.println("连接激活");
                 ctx.writeAndFlush("hello,world!"); // 若没有StringEncoder，则发送不出去字符串。
                 System.out.println("写入完成");
             }

             @Override
             public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

             }

             @Override
             public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

             }

             @Override
             public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                 logger.log(Level.WARNING, "发生错误，关闭链接。", cause);
                 ctx.close();
             }
         }
     }
    static class Client {
        public Client() throws Exception {
            Socket socket = new Socket("localhost", 80);

            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());

            byte[] buf = new byte[256];

            System.out.println("开始读数据...");
            int count = bis.read(buf);
            System.out.println("读取数据数量:" + count);
            System.out.println(new String(buf));

            bis.close();
            socket.close();
        }

    }
    public static void main(String[] args) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        new Server();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
            Thread.sleep(20000);
            System.out.println("开始执行");
            new Client();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
