package cn.susudad.enheng.client.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import java.util.Queue;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description HttpHandler
 * @createTime 2022/8/17
 */
@Slf4j
public class HttpDecode extends SimpleChannelInboundHandler<FullHttpResponse> {

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
    Queue<ReqInfo> queue = ctx.channel().attr(ReqInfoQueue.REQ_QUEUE).get();
    ReqInfo reqInfo = queue.poll();
    if (reqInfo == null) {
      ReferenceCountUtil.release(msg);
      return;
    }
    reqInfo.getPromise().trySuccess(msg);
  }
}
