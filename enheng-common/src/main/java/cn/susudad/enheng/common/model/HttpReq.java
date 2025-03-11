package cn.susudad.enheng.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import lombok.Data;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description HttpReq
 * @createTime 2022/8/17
 */
@Data
public class HttpReq extends HttpMsg {

  private static final long serialVersionUID = -8768763584460675316L;
  private String method;
  private String uri;

  public static HttpReq convert(FullHttpRequest request) {
    HttpReq httpReq = new HttpReq();
    httpReq.method = request.method().name();
    httpReq.setHttpVersion(request.protocolVersion().text());
    List<HeaderEntry> headers = new ArrayList<>();
    for (Entry<String, String> header : request.headers()) {
      headers.add(new HeaderEntry(header.getKey(), header.getValue()));
    }
    httpReq.setHeaders(headers);
    httpReq.uri = request.uri();
    byte[] bytes = new byte[request.content().readableBytes()];
    request.content().readBytes(bytes);
    httpReq.setContent(bytes);
    return httpReq;
  }

  @JsonIgnore
  public FullHttpRequest getSourceRequest() {
    ByteBuf buffer = Unpooled.EMPTY_BUFFER;
    if (getContent() != null) {
      buffer = Unpooled.buffer(getContent().length);
      buffer.writeBytes(getContent());
    }
    DefaultFullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.valueOf(getHttpVersion()), HttpMethod.valueOf(this.method),
        this.uri, buffer);
    for (HeaderEntry header : getHeaders()) {
      fullHttpRequest.headers().add(header.getKey(), header.getValue());
    }
    return fullHttpRequest;
  }

}
