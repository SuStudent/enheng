package cn.susudad.enheng.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description LoginResp
 * @createTime 2022/8/15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResp {

  private boolean success;

  private int idlTimeout;

  private String desc;
}
