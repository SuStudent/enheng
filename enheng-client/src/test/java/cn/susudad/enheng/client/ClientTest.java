package cn.susudad.enheng.client;

import cn.susudad.enheng.client.http.HttpClient;
import cn.susudad.enheng.common.protocol.EnhengPromise;
import cn.susudad.enheng.common.utils.JsonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description ClientTest
 * @createTime 2022/8/17
 */
@Slf4j
@SpringBootTest(args = {"--subdomain=asd", "--port=10003", "--host=10.15.108.95"})
public class ClientTest {

  @Autowired
  private EnhengProxy enhengProxy;


  @Test
  public void test() throws Exception {
    CountDownLatch d = new CountDownLatch(1);
    HttpClient client = null;
    try {
      client = HttpClient.createClient("10.15.108.95", 10003);
      EnhengPromise<FullHttpResponse> request = client.request(req());
      HttpClient finalClient = client;
      request.onComplete((resp,e) -> {
        if (resp != null && resp.status().code() == 200) {
          System.out.println(resp.content().toString(StandardCharsets.UTF_8));
        }
        if (e != null) {
          e.printStackTrace();
        }
        d.countDown();
        try {
          finalClient.close();
        } catch (IOException ex) {
          throw new RuntimeException(ex);
        }
      });
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    d.await();
  }
  @Test
  public void test2() throws InterruptedException {
    int num = 10000;
    CountDownLatch d = new CountDownLatch(num);
    AtomicInteger success = new AtomicInteger();
    AtomicInteger error = new AtomicInteger();

    ExecutorService executorService = Executors.newFixedThreadPool(8);
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    for (int i = 0; i < num; i++) {
      executorService.submit(() -> {
        try {
          HttpClient client = HttpClient.createClient("10.15.108.95", 10003);
          EnhengPromise<FullHttpResponse> promise = client.request(req());
          promise.onComplete((resp, e) -> {
            try {
              if (resp != null && resp.status().code() == 200) {
                success.incrementAndGet();
              }
              if (e != null) {
                e.printStackTrace();
                error.incrementAndGet();
              }
              d.countDown();
            } finally {
              try {
                client.close();
              } catch (IOException ex) {
                throw new RuntimeException(ex);
              }
            }
          });
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    }
    d.await();
    stopWatch.stop();
    log.info("success={},error={},{}s", success.get(), error.get(), stopWatch.getTotalTimeSeconds());
  }

  private DefaultFullHttpRequest req() {
    Map<String, Object> m = new HashMap<>();
    m.put("qid", System.currentTimeMillis() + "");
    m.put("account", "hxtest-q110");
    String json = JsonUtils.toJson(m);
    ByteBuf buffer = Unpooled.buffer();
    buffer.writeBytes(json.getBytes(StandardCharsets.UTF_8));

    DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/im/grade/get", buffer);
    request.headers().add(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
    request.headers().add(HttpHeaderNames.CONTENT_LENGTH, buffer.readableBytes());
    return request;
  }
}
