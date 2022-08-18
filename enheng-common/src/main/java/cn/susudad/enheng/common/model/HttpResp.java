package cn.susudad.enheng.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import lombok.Data;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description HttpResponse
 * @createTime 2022/8/17
 */
@Data
public class HttpResp extends HttpMsg {

  private int status;

  public static HttpResp convert(FullHttpResponse response) {
    HttpResp httpResp = new HttpResp();
    httpResp.status = response.status().code();
    httpResp.setHttpVersion(response.protocolVersion().text());
    List<Entry<String, String>> headers = new ArrayList<>();
    for (Entry<String, String> header : response.headers()) {
      headers.add(header);
    }
    httpResp.setHeaders(headers);
    byte[] bytes = new byte[response.content().readableBytes()];
    response.content().readBytes(bytes);
    httpResp.setContent(bytes);
    return httpResp;
  }

  @JsonIgnore
  public FullHttpResponse getSourceResponse() {
    ByteBuf buffer = Unpooled.EMPTY_BUFFER;
    if (getContent() != null) {
      buffer = Unpooled.buffer(getContent().length);
      buffer.writeBytes(getContent());
    }
    DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.valueOf(getHttpVersion()), HttpResponseStatus.valueOf(status), buffer);
    for (Entry<String, String> header : getHeaders()) {
      response.headers().add(header.getKey(), header.getValue());
    }
    return response;
  }
}
