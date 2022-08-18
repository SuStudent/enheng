package cn.susudad.enheng.client.config;

import cn.susudad.enheng.client.http.HttpClient;
import cn.susudad.enheng.client.http.HttpClientPool;
import cn.susudad.enheng.client.http.HttpClientPooledFactory;
import java.time.Duration;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description HttpPoolConfig
 * @createTime 2022/8/18
 */
@Configuration
public class HttpPoolConfig {

  @Bean
  public GenericObjectPoolConfig<HttpClient> poolConfig() {
    GenericObjectPoolConfig<HttpClient> poolConfig = new GenericObjectPoolConfig<>();
    poolConfig.setMinIdle(1);
    poolConfig.setMaxIdle(8);
    poolConfig.setMaxTotal(8);
    poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(60));
    poolConfig.setTestWhileIdle(true);
    poolConfig.setTestOnReturn(true);
    poolConfig.setTestOnBorrow(true);
    poolConfig.setTestOnCreate(true);
    poolConfig.setJmxEnabled(false);
    return poolConfig;
  }

  @Bean
  public HttpClientPooledFactory pooledFactory(CommandConfig config) {
    return new HttpClientPooledFactory(config.getHost(), config.getLocalPort());
  }

  @Bean
  public HttpClientPool httpClientPool(HttpClientPooledFactory pooledFactory) {
    return new HttpClientPool(pooledFactory, poolConfig());
  }
}
