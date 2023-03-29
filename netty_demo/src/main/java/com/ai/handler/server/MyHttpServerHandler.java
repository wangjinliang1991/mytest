package com.ai.handler.server;


import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
public class MyHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final HttpDataFactory HTTP_DATA_FACTORY = new DefaultHttpDataFactory(DefaultHttpDataFactory.MAXSIZE);

    static {
        DiskFileUpload.baseDirectory = "/opt/netty/fileupload";
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
        HttpMethod method = fullHttpRequest.method();
        if (HttpMethod.GET.equals(method)) {
            parseGet(fullHttpRequest);
        } else if (HttpMethod.POST.equals(method)) {
            parsePost(fullHttpRequest);
        } else {
            log.error("{} method is not supported, please change http method to get or post!", method);
        }

        // response
        StringBuilder sb = new StringBuilder();
        sb.append("<html>").append("<head>").append("</head>").append("<body>")
                .append("<h3>success</h3>").append("/body").append("</html>");
        writeResponse(ctx, fullHttpRequest, HttpResponseStatus.OK, sb.toString());
    }

    private void writeResponse(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest, HttpResponseStatus status, String string) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        response.content().writeBytes(string.getBytes(StandardCharsets.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8");
        HttpUtil.setContentLength(response, response.content().readableBytes());
        boolean keepAlive = HttpUtil.isKeepAlive(fullHttpRequest);
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, "keep-alive");
            ctx.writeAndFlush(response);
        } else {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void parsePost(FullHttpRequest fullHttpRequest) {
        // content-type
        String contentType = getContentType(fullHttpRequest);
        switch (contentType) {
            case "application/json":
                parseJson(fullHttpRequest);
                break;
            case "application/x-www-form-urlencoded":
                parseFormData(fullHttpRequest);
                break;
            case "multipart/form-data":
                parseMultipart(fullHttpRequest);
                break;
            default:
        }
    }

    private void parseMultipart(FullHttpRequest fullHttpRequest) {
        HttpPostRequestDecoder postRequestDecoder = new HttpPostRequestDecoder(HTTP_DATA_FACTORY, fullHttpRequest);
        // check if multipart
        if (postRequestDecoder.isMultipart()) {
            // get data of body
            List<InterfaceHttpData> bodyHttpDatas = postRequestDecoder.getBodyHttpDatas();
            bodyHttpDatas.forEach(dataItem -> {
                // check form type
                InterfaceHttpData.HttpDataType httpDataType = dataItem.getHttpDataType();
                if (httpDataType.equals(InterfaceHttpData.HttpDataType.Attribute)) {
                    // normal form data
                    Attribute attribute = (Attribute) dataItem;
                    try {
                        log.info("form item name: {}, value: {}", attribute.getName(), attribute.getValue());
                    } catch (IOException e) {
                        log.error("get form item data error, msg={}", e.getMessage());
                    }
                } else if (httpDataType.equals(InterfaceHttpData.HttpDataType.FileUpload)) {
                    // file upload to disk
                    FileUpload fileUpload = (FileUpload) dataItem;
                    // original file name
                    String filename = fileUpload.getFilename();
                    String name = fileUpload.getName();
                    log.info("file name: {}, form item name: {}", filename, name);
                    //file save to disk
                    if (fileUpload.isCompleted()) {
                        String path = DiskFileUpload.baseDirectory + File.separator + filename;
                        try {
                            fileUpload.renameTo(new File(path));
                        } catch (IOException e) {
                            log.error("file upload fails, msg={}", e.getMessage());
                        }
                    }
                }
            });
        }
    }

    private void parseFormData(FullHttpRequest fullHttpRequest) {
        // uri body both have data
        parseKVstr(fullHttpRequest.uri(), true);
        parseKVstr(fullHttpRequest.content().toString(StandardCharsets.UTF_8), false);
    }

    private void parseJson(FullHttpRequest fullHttpRequest) {
        String jsonStr = fullHttpRequest.content().toString(StandardCharsets.UTF_8);
        //deserialize
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        // print
        jsonObject.entrySet().stream().forEach(entry -> {
            log.info("json key={}, value= {}", entry.getKey(), entry.getValue());
        });
    }


    private String getContentType(FullHttpRequest fullHttpRequest) {
        HttpHeaders headers = fullHttpRequest.headers();
        String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
        return contentType.split(";")[0];
    }

    private void parseGet(FullHttpRequest fullHttpRequest) {
        // get parameter via url
        parseKVstr(fullHttpRequest.uri(), true);
    }

    private void parseKVstr(String str, boolean hasPath) {
        // parse kv via queryStringDecoder
        QueryStringDecoder qsd = new QueryStringDecoder(str, StandardCharsets.UTF_8, hasPath);
        Map<String, List<String>> parameters = qsd.parameters();
        // encapsulate parameter
        if (parameters != null && parameters.size() > 0) {
            parameters.entrySet().stream().forEach(entry -> {
                log.info("parameter name: {}, parameter value: {}", entry.getKey(), entry.getValue());
            });
        }
    }
}
