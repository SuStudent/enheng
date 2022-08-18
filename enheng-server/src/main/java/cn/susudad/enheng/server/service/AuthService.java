package cn.susudad.enheng.server.service;

import cn.susudad.enheng.common.model.LoginReq;
import cn.susudad.enheng.common.model.LoginResp;
import cn.susudad.enheng.common.utils.RSAUtils;
import cn.susudad.enheng.server.entity.AppClient;
import cn.susudad.enheng.server.redis.RedisService;
import cn.susudad.enheng.server.server.EnhengServerProperties;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description AuthService
 * @createTime 2022/8/15
 */
@Service
@Slf4j
public class AuthService {

  @Autowired
  private RedisService redisService;

  @Autowired
  private EnhengServerProperties properties;

  private final String APP_CLIENT_DATA = "app_client_data";

  public LoginResp login(LoginReq loginReq) {
    String appKey;
    String appSig;
    if (loginReq == null || StringUtils.isAnyBlank(appKey = loginReq.getAppKey(), appSig = loginReq.getAppSig())) {
      return LoginResp.builder().success(false).desc("key or sign is miss.").build();
    }
    AppClient client;
    try {
      String decrypt = RSAUtils.decryptByPrivateKey(appSig, properties.getPrivateKey());
      String[] dt = decrypt.split(":");
      if (!appKey.equals(dt[0]) || System.currentTimeMillis() - Long.parseLong(dt[1]) > 5 * 60 * 1000) {
        throw new RuntimeException("秘钥不正确。");
      }
      client = redisService.HASH.hGet(APP_CLIENT_DATA, loginReq.getAppKey(), AppClient.class);
      if (client == null) {
        throw new RuntimeException("用户不存在。");
      }
    } catch (Exception e) {
      log.error("验签失败。", e);
      return LoginResp.builder().success(false).desc("server error.").build();
    }

    if (client.getExpireDate().before(new Date())) {
      return LoginResp.builder().success(false).desc("appKey 已过期.").build();
    }
    if (client.isFreezing()) {
      return LoginResp.builder().success(false).desc("appKey 已冻结.").build();
    }
    return LoginResp.builder().success(true).desc("登录成功。")
        .idlTimeout((int) Math.min(properties.getIdlReadTimeout().getSeconds(), properties.getIdlWriteTimeout().getSeconds())).build();
  }
}
