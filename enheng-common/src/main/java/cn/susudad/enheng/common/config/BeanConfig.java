package cn.susudad.enheng.common.config;

import cn.susudad.enheng.common.utils.SnowFlake;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description BeanConfig
 * @createTime 2022/8/11
 */
@Configuration
public class BeanConfig {

  @Bean
  public SnowFlake snowFlake() {
    return new SnowFlake(0xf, 0xf);
  }
}
