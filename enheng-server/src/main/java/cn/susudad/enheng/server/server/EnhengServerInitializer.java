package cn.susudad.enheng.server.server;

import cn.susudad.enheng.common.codec.EnhengMessageDecode;
import cn.susudad.enheng.common.codec.EnhengMessageEncode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description EnhengServerInitializer
 * @createTime 2022/8/11
 */
public class EnhengServerInitializer extends ChannelInitializer<Channel> {

  private EnhengServerProperties serverProperties;

  public EnhengServerInitializer(EnhengServerProperties serverProperties) {
    this.serverProperties = serverProperties;
  }

  @Override
  protected void initChannel(Channel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
    pipeline.addLast(IdleStateHandler.class.getSimpleName(),
        new IdleStateHandler((int) serverProperties.getIdlReadTimeout().getSeconds(), (int) serverProperties.getIdlWriteTimeout().getSeconds(), 0));
    // WriteTimeoutHandler 其实为写入操作的耗时超时时间，非写入空闲超时时间
    pipeline.addLast(WriteTimeoutHandler.class.getSimpleName(), new WriteTimeoutHandler((int) serverProperties.getIoWriteTimeout().getSeconds()));
    pipeline.addLast(LengthFieldBasedFrameDecoder.class.getSimpleName(), new LengthFieldBasedFrameDecoder(serverProperties.getMaxFrameLength(), 5, 4, -9, 0));
    pipeline.addLast(EnhengMessageDecode.class.getSimpleName(), new EnhengMessageDecode());
    pipeline.addLast(EnhengMessageEncode.class.getSimpleName(), new EnhengMessageEncode());
    pipeline.addLast(EnhengServerHandler.class.getSimpleName(), new EnhengServerHandler());
  }
}
