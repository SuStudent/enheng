package cn.susudad.enheng.common.model;

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
public class Heartbeat {

  private String appKey;

  private Date time;

  private String msg;
}
