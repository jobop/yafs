package com.jobop.yafs.handler;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by Enzo Cotter on 2020/2/15.
 */
public class ListFileHandler extends ChannelInboundHandlerAdapter {
    private File webrootDir;

    public ListFileHandler(String webroot) {
        this.webrootDir = new File(webroot);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest request = (FullHttpRequest) msg;
        String uri = request.uri();
        uri = URLDecoder.decode(uri, "UTF-8");
        if ("/".equals(uri)) {
            String[] list = webrootDir.list();
            String jsonStr = JSON.toJSONString(list);

            ByteBuf bb = ctx.alloc().buffer();
            bb.writeBytes(jsonStr.getBytes("UTF-8"));
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, bb);

            response.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";" + HttpHeaderValues.CHARSET + "=utf8");

            response.headers().add(HttpHeaderNames.CONTENT_LENGTH, bb.readableBytes());

            ctx.channel().pipeline().writeAndFlush(response);

            //encode的时候会释放
//            bb.release();
        } else {
            ctx.fireChannelRead(msg);
        }

    }
}
