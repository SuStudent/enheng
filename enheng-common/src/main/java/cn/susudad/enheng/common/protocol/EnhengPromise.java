package cn.susudad.enheng.common.protocol;

import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

/**
 * @author yiyi.su
 * @version 1.0.0
 * @description EnhengPromise
 * @createTime 2022/8/17
 */
@Slf4j
public class EnhengPromise<T> extends CompletableFuture<T> {

  private final static HashedWheelTimer TIMER = new HashedWheelTimer(new CustomizableThreadFactory("promise-monitor-"),
      1, TimeUnit.SECONDS, 60);

  private final Promise<T> promise = ImmediateEventExecutor.INSTANCE.newPromise();


  /**
   * @param timeoutSeconds 超时设置，小于0 永不超时。默认10秒
   */
  public EnhengPromise(Integer timeoutSeconds) {
    if (timeoutSeconds == null || timeoutSeconds == 0) {
      timeoutSeconds = 10;
    }
    if (timeoutSeconds > 0) {
      TIMER.newTimeout(timeout -> {
        if (!this.isDone()) {
          this.tryFailure(new TimeoutException("EnhengPromise timeout."));
        }
      }, timeoutSeconds, TimeUnit.SECONDS);
    }
  }

  public boolean isSuccess() {
    return promise.isSuccess();
  }

  @Override
  public boolean isDone() {
    return promise.isDone();
  }

  @Override
  public boolean isCancelled() {
    return promise.isCancelled();
  }

  public boolean trySuccess(T result) {
    if (promise.trySuccess(result)) {
      complete(result);
      return true;
    }
    return false;
  }

  public Throwable cause() {
    return promise.cause();
  }

  public boolean tryFailure(Throwable cause) {
    if (promise.tryFailure(cause)) {
      completeExceptionally(cause);
      return true;
    }
    return false;
  }

  public EnhengPromise<T> await() throws InterruptedException {
    promise.await();
    return this;
  }

  public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
    return promise.await(timeout, unit);
  }

  public T getNow() {
    return promise.getNow();
  }

  public boolean cancel(boolean mayInterruptIfRunning) {
    if (promise.cancel(mayInterruptIfRunning)) {
      return super.cancel(mayInterruptIfRunning);
    }
    return false;
  }

  public void onComplete(BiConsumer<? super T, ? super Throwable> action) {
    promise.addListener(f -> {
      if (!f.isSuccess()) {
        action.accept(null, f.cause());
        return;
      }
      action.accept((T) f.getNow(), null);
    });
  }

  public String toString() {
    return "EnhengPromise [promise=" + promise + "]";
  }
}
