package cn.susudad.enheng.common.codec;

import cn.susudad.enheng.common.protocol.EnhengMessage;
import cn.susudad.enheng.common.protocol.MessageHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description EnhengMessageEncode
 * @createTime 2022/8/11
 */
@Slf4j
public class EnhengMessageEncode extends MessageToMessageEncoder<EnhengMessage> {

  @Override
  protected void encode(ChannelHandlerContext ctx, EnhengMessage msg, List<Object> out) throws Exception {
    if (msg == null) {
      log.error("msg is null.");
      throw new EncoderException("msg is null.");
    }
    MessageHeader header = msg.getHeader();
    if (header == null) {
      log.error("msg.header is null.");
      throw new EncoderException("msg.header is null.");
    }
    ByteBuf sendBuf = Unpooled.buffer();
    sendBuf.writeShort(header.getMagic());
    sendBuf.writeByte(header.getVersion());
    sendBuf.writeByte(header.getSerType());
    sendBuf.writeByte(header.getMsgType());
    sendBuf.writeInt(0); // Length 占位
    sendBuf.writeLong(header.getMsgSeq());
    sendBuf.writeInt(header.getAttachLength());
    if (header.getAttachLength() > 0) {
      sendBuf.writeBytes(header.getAttach().getBytes(StandardCharsets.UTF_8));
    }
    if (msg.getBody() != null && msg.getBody().length > 0) {
      sendBuf.writeBytes(msg.getBody());
    }
    sendBuf.setInt(5, sendBuf.readableBytes());
    out.add(sendBuf);
  }

}
