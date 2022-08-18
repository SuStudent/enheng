package cn.susudad.enheng.client.config;

import java.util.List;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description ConfigTest
 * @createTime 2022/8/16
 */
@Component
public class CommandConfig implements InitializingBean {

  @Autowired
  private ApplicationArguments applicationArguments;

  private String subDomain;

  private int localPort;

  private String host;

  public String getSubDomain() {
    return subDomain;
  }

  public int getLocalPort() {
    return localPort;
  }

  public String getHost() {
    return host;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    List<String> subdomain = applicationArguments.getOptionValues("subdomain");
    List<String> port = applicationArguments.getOptionValues("port");
    if (CollectionUtils.isEmpty(subdomain) || CollectionUtils.isEmpty(port)) {
      throw new RuntimeException("subdomain and port is required.");
    }
    this.subDomain = subdomain.get(0);
    this.localPort = Integer.parseInt(port.get(0));

    List<String> host = applicationArguments.getOptionValues("host");
    if (!CollectionUtils.isEmpty(host)) {
      this.host = host.get(0);
    } else {
      this.host = "127.0.0.1";
    }
  }
}
