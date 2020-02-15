package com.jobop.yafs;

import com.jobop.yafs.handler.DownFileHandler;
import com.jobop.yafs.handler.ListFileHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * Created by Enzo Cotter on 2020/2/15.
 */
public class YafsServer {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossEventLoopGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerEventLoopGroup = new NioEventLoopGroup();


        try {

            ServerBootstrap sbs = new ServerBootstrap();
            sbs.group(bossEventLoopGroup, workerEventLoopGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer() {
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new HttpRequestDecoder());

                    ch.pipeline().addLast(new HttpObjectAggregator(1024));
                    ch.pipeline().addLast(new ListFileHandler("/Users/zhengwei/Desktop/儿童编程"));
                    ch.pipeline().addLast(new DownFileHandler("/Users/zhengwei/Desktop/儿童编程"));


                    ch.pipeline().addLast(new HttpResponseEncoder());




                }
            });

            ChannelFuture future = sbs.bind(12345).sync();
            future.channel().closeFuture().sync();

        } finally {
            bossEventLoopGroup.shutdownGracefully();
            workerEventLoopGroup.shutdownGracefully();
        }

    }
}
