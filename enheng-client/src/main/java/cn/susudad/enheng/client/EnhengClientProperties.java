package cn.susudad.enheng.client;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description EnhengClientProperties
 * @createTime 2022/8/11
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "enheng.client")
public class EnhengClientProperties {

  private Duration ioWriteTimeout = Duration.ofSeconds(10);
  private String remoteHost;
  private int remotePort;
  private String appKey;
  private String appSecret;
}
