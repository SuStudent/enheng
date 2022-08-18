package cn.susudad.enheng.server.entity;

import java.util.Date;
import lombok.Data;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description AppClient
 * @createTime 2022/8/15
 */
@Data
public class AppClient {

  private String appKey;

  private String desc;

  private Date expireDate;

  private boolean freezing;

}
