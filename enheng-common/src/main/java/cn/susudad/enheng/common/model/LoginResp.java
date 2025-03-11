package cn.susudad.enheng.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
public class LoginResp implements Serializable {

  private static final long serialVersionUID = 2828441869451458167L;
  private boolean success;

  private int idlTimeout;

  private String desc;
}
