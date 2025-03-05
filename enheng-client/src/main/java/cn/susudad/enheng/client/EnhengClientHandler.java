package cn.susudad.enheng.client;

import cn.susudad.enheng.common.config.SpringContext;
import cn.susudad.enheng.common.model.HttpReq;
import cn.susudad.enheng.common.model.HttpResp;
import cn.susudad.enheng.common.protocol.AbstractHandler;
import cn.susudad.enheng.common.protocol.EnhengMessage;
import cn.susudad.enheng.common.protocol.MsgTypeEnum;
import cn.susudad.enheng.common.serialize.SerializedContext;
import cn.susudad.enheng.common.utils.ExecutorService;
import cn.susudad.enheng.common.utils.MessageUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description EnhengClientHandler
 * @createTime 2022/8/12
 */
@Slf4j
public class EnhengClientHandler extends AbstractHandler<EnhengMessage> {

  private EnhengClientProperties properties;

  private EnhengProxy enhengProxy;

  public EnhengClientHandler(EnhengClientProperties clientProperties) {
    this.properties = clientProperties;
    this.enhengProxy = SpringContext.getBean(EnhengProxy.class);
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    log.info("连接成功。");
    super.channelActive(ctx);
    ctx.pipeline().addBefore(EnhengClientHandler.class.getSimpleName(), EnhengLoginHandler.class.getSimpleName(), new EnhengLoginHandler(properties));
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, EnhengMessage enhengMessage) throws Exception {
    if (enhengMessage.getHeader().getMsgType() == MsgTypeEnum.SERVICE_REQ.getType()) {
      HttpReq httpReq = SerializedContext.deserialize(enhengMessage, HttpReq.class);
      FullHttpRequest sourceRequest = httpReq.getSourceRequest();
      StopWatch stopWatch = new StopWatch();
      stopWatch.start(sourceRequest.uri());
      ExecutorService.getInstance().execute(() -> {
        enhengProxy.process(sourceRequest, (resp, error) -> {
          FullHttpResponse response = resp;
          if (error != null || resp == null) {
            log.error("代理异常。", error);
            response = new DefaultFullHttpResponse(sourceRequest.protocolVersion(), HttpResponseStatus.BAD_GATEWAY);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
          }
          HttpResp httpResp = HttpResp.convert(response);
          stopWatch.stop();
          if (response.status().code() != 200) {
            log.warn("proxy：uri={}, status={}, {}ms", sourceRequest.uri(), response.status().code(), stopWatch.getTotalTimeMillis());
          } else {
            log.info("proxy：uri={}, status={}, {}ms", sourceRequest.uri(), response.status().code(), stopWatch.getTotalTimeMillis());
          }
          EnhengMessage respMessage = MessageUtils.buildRespMessage(enhengMessage, MsgTypeEnum.SERVICE_RESP, httpResp);
          ctx.writeAndFlush(respMessage);
        });
      });
    }

  }
}
