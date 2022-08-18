package cn.susudad.enheng.server.server;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * @author yiyi.su
 * @version 1.0.0
 * @description EnhengProperties
 * @createTime 2022/8/11
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "enheng.server")
public class EnhengServerProperties {

  private int port = 7000;
  private Duration ioWriteTimeout = Duration.ofSeconds(10);
  private Duration idlReadTimeout = Duration.ofSeconds(60);
  private Duration idlWriteTimeout = Duration.ofSeconds(60);
  private String privateKey;
}
