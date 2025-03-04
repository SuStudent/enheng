package cn.susudad.enheng.server.http;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description HttpServerProperties
 * @createTime 2022/8/16
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "http.server")
public class HttpServerProperties {

  private int port;

  private int coreThreadSize = Runtime.getRuntime().availableProcessors();

  private int maxThreadSize = Runtime.getRuntime().availableProcessors() * 2;

  private int proxyTimeoutSecond = 10;
}
