package cn.susudad.enheng.common.exception;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description ChannelCloseException
 * @createTime 2022/8/18
 */
public class ChannelCloseException extends RuntimeException {

  public ChannelCloseException(String msg) {
    super(msg);
  }
}
