package cn.susudad.enheng.client;

import cn.susudad.enheng.client.config.CommandConfig;
import cn.susudad.enheng.client.http.HttpClient;
import cn.susudad.enheng.client.http.HttpClientPool;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description ClientTest
 * @createTime 2022/8/17
 */
@SpringBootTest(args = {"--subdomain=asd", "--port=8080"})
public class ClientTest {

  @Autowired
  private HttpClientPool pool;


  @Test
  public void test() throws Exception {
    CountDownLatch d = new CountDownLatch(1);
    HttpClient client = pool.borrowObject();
    DefaultFullHttpRequest request2 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/test/2");
    DefaultFullHttpRequest request1 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/test/1");
    DefaultFullHttpRequest request3 = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/test/3");
    client.request(request1).onComplete((resp, error) -> {
      System.out.println(Thread.currentThread().getName());
      System.out.println(resp.content().toString(StandardCharsets.UTF_8));
    });
    client.request(request2).onComplete((resp, error) -> {
      System.out.println(Thread.currentThread().getName());
      System.out.println(resp.content().toString(StandardCharsets.UTF_8));
    });
    client.request(request3).onComplete((resp, error) -> {
      System.out.println(Thread.currentThread().getName());
      System.out.println(resp.content().toString(StandardCharsets.UTF_8));
    });
    System.out.println(Thread.currentThread().getName());
    d.await();
    pool.returnObject(client);
  }
}
