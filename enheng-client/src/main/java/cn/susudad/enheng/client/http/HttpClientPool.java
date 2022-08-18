package cn.susudad.enheng.client.http;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description HttpClientPool
 * @createTime 2022/8/18
 */
public class HttpClientPool extends GenericObjectPool<HttpClient> {

  public HttpClientPool(PooledObjectFactory<HttpClient> factory) {
    super(factory);
  }

  public HttpClientPool(PooledObjectFactory<HttpClient> factory, GenericObjectPoolConfig<HttpClient> config) {
    super(factory, config);
  }

  public HttpClientPool(PooledObjectFactory<HttpClient> factory, GenericObjectPoolConfig<HttpClient> config,
      AbandonedConfig abandonedConfig) {
    super(factory, config, abandonedConfig);
  }
}
