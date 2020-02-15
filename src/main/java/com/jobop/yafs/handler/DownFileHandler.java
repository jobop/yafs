package com.jobop.yafs.handler;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import io.netty.handler.codec.http.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.net.URLDecoder;

/**
 * Created by Enzo Cotter on 2020/2/15.
 */
public class DownFileHandler extends ChannelInboundHandlerAdapter {
    private File webrootDir;

    public DownFileHandler(String webroot) {
        this.webrootDir = new File(webroot);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest request = (FullHttpRequest) msg;
        String uri = request.uri();
        uri = URLDecoder.decode(uri, "UTF-8");
        if (uri.startsWith("/download")) {

            final String fileName = uri.replace("/download/", "");


            File[] list = webrootDir.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    return name.equals(fileName);
                }
            });

            if (list.length == 1) {
                File dest = list[0];
                FileInputStream inputStream = new FileInputStream(dest);
                HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                resp.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_OCTET_STREAM);
                resp.headers().add(HttpHeaderNames.CONTENT_LENGTH, inputStream.available());
                //写出头
                ctx.channel().writeAndFlush(resp);

                //写出内容(直接写出文件到sock，0拷贝，不用拷贝到用户内存的byteBuf再写出到内核)
                FileRegion fr = new DefaultFileRegion(dest, 0, inputStream.available());
                ctx.channel().writeAndFlush(fr);
                ctx.channel().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            }
        }

    }
}
