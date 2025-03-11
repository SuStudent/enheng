package cn.susudad.enheng.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description LoginReq
 * @createTime 2022/8/12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginReq implements Serializable {

  private static final long serialVersionUID = -6920344874107288634L;
  private String appKey;

  private String appSig;

  private String subdomain;
}
