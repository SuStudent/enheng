package cn.susudad.enheng.client.http;

import cn.susudad.enheng.common.protocol.EnhengPromise;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description ReqInfo
 * @createTime 2022/8/18
 */
@Data
@AllArgsConstructor
public class ReqInfo {

  private FullHttpRequest request;

  private EnhengPromise<FullHttpResponse> promise;
}
