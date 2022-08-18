package cn.susudad.enheng.server.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description RedisConfig
 * @createTime 2022/8/15
 */
@Configuration
public class RedisConfig {

  @Bean
  public RedisService redisService(StringRedisTemplate redisTemplate) {
    return new RedisService(redisTemplate);
  }
}
