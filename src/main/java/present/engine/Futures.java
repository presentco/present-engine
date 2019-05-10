package present.engine;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * Future utilities.
 *
 * @author Bob Lee (bob@present.co)
 */
public class Futures {

  /** Converts one future type to another. */
  public static <I, O> Future<O> map(Future<I> delegate, Function<I, O> mapper) {
    return new Future<O>() {
      @Override public boolean cancel(boolean mayInterruptIfRunning) {
        return delegate.cancel(mayInterruptIfRunning);
      }

      @Override public boolean isCancelled() {
        return delegate.isCancelled();
      }

      @Override public boolean isDone() {
        return delegate.isDone();
      }

      @Override public O get() throws InterruptedException, ExecutionException {
        return mapper.apply(delegate.get());
      }

      @Override public O get(long timeout, TimeUnit unit)
          throws InterruptedException, ExecutionException, TimeoutException {
        return mapper.apply(delegate.get(timeout, unit));
      }
    };
  }
}
