package cn.susudad.enheng.common.model;

import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description HeartbeatReq
 * @createTime 2022/8/16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Heartbeat implements Serializable {
  private static final long serialVersionUID = -4830526091716508958L;

  private String appKey;

  private Date time;

  private String msg;
}
