package cn.susudad.enheng.client;

import cn.susudad.enheng.common.codec.EnhengMessageDecode;
import cn.susudad.enheng.common.codec.EnhengMessageEncode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.WriteTimeoutHandler;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description EnhengClientInitializer
 * @createTime 2022/8/11
 */
public class EnhengClientInitializer extends ChannelInitializer<Channel> {

  private EnhengClientProperties clientProperties;

  public EnhengClientInitializer(EnhengClientProperties clientProperties) {
    this.clientProperties = clientProperties;
  }

  @Override
  protected void initChannel(Channel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
//    pipeline.addLast(new LoggingHandler(LogLevel.INFO));
    // WriteTimeoutHandler 其实为写入操作的耗时超时时间，非写入空闲超时时间
    pipeline.addLast(WriteTimeoutHandler.class.getSimpleName(), new WriteTimeoutHandler((int) clientProperties.getIoWriteTimeout().getSeconds()));
    pipeline.addLast(LengthFieldBasedFrameDecoder.class.getSimpleName(), new LengthFieldBasedFrameDecoder(10 * 1024 * 1024, 5, 4, -9, 0));
    pipeline.addLast(EnhengMessageDecode.class.getSimpleName(), new EnhengMessageDecode());
    pipeline.addLast(EnhengMessageEncode.class.getSimpleName(), new EnhengMessageEncode());
    pipeline.addLast(EnhengLoginHandler.class.getSimpleName(), new EnhengLoginHandler(clientProperties));
  }

}
