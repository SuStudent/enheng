package cn.susudad.enheng.client.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.DestroyMode;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description HttpClientFactory
 * @createTime 2022/8/18
 */
@Slf4j
public class HttpClientPooledFactory extends BasePooledObjectFactory<HttpClient> {

  private final String host;

  private final int port;

  public HttpClientPooledFactory(String host, int port) {
    this.host = host;
    this.port = port;
  }

  @Override
  public HttpClient create() throws InterruptedException {
    log.debug("创建HttpClient。");
    return HttpClient.createClient(host, port);
  }

  @Override
  public PooledObject<HttpClient> wrap(HttpClient obj) {
    return new DefaultPooledObject<>(obj);
  }

  @Override
  public void destroyObject(PooledObject<HttpClient> p, DestroyMode destroyMode) throws Exception {
    log.debug("销毁HttpClient。");
    p.getObject().close();
    super.destroyObject(p, destroyMode);
  }

  @Override
  public boolean validateObject(PooledObject<HttpClient> p) {
    return p.getObject().isActive();
  }

}
