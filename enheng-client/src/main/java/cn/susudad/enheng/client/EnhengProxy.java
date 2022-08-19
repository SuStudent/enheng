package cn.susudad.enheng.client;

import cn.susudad.enheng.client.config.CommandConfig;
import cn.susudad.enheng.client.http.HttpClient;
import cn.susudad.enheng.common.protocol.EnhengPromise;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import java.io.IOException;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description Proxy
 * @createTime 2022/8/18
 */
@Slf4j
@Component
public class EnhengProxy {

  @Autowired
  private CommandConfig config;

  public void process(FullHttpRequest req, BiConsumer<FullHttpResponse, Throwable> consumer) {
    try {
      HttpClient client = HttpClient.createClient(config.getHost(), config.getLocalPort());
      EnhengPromise<FullHttpResponse> promise = client.request(req);
      promise.onComplete((resp, error) -> {
        try {
          consumer.accept(resp, error);
        } finally {
          try {
            client.close();
          } catch (IOException e) {
            log.error("", e);
          }
        }
      });
    } catch (Exception e) {
      log.error("", e);
      consumer.accept(null, e);
    }
  }
}
