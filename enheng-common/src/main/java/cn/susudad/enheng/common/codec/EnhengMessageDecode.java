package cn.susudad.enheng.common.codec;

import cn.susudad.enheng.common.protocol.EnhengMessage;
import cn.susudad.enheng.common.protocol.MessageHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
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
public class EnhengMessageDecode extends ByteToMessageDecoder {


  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (in == null) {
      throw new DecoderException("ByteBuf is null.");
    }
    EnhengMessage enhengMessage = new EnhengMessage();
    MessageHeader header = new MessageHeader();
    header.setMagic(in.readShort());
    header.setVersion(in.readByte());
    header.setSerType(in.readByte());
    header.setMsgType(in.readByte());
    header.setLength(in.readInt());
    header.setMsgSeq(in.readLong());

    int attachLength = in.readInt();
    if (attachLength > 0) {
      ByteBuf attach = in.readBytes(attachLength);
      header.setAttach(attach.toString(StandardCharsets.UTF_8));
    }
    enhengMessage.setHeader(header);
    int bodyLength = in.readableBytes();
    if (bodyLength > 0) {
      byte[] body = new byte[bodyLength];
      in.readBytes(body);
      enhengMessage.setBody(body);
    }
    out.add(enhengMessage);
  }

}
