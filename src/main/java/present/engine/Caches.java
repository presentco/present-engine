package present.engine;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.function.Function;

/**
 * Cache utilities.
 *
 * @author Bob Lee
 */
public class Caches {

  /** Creates an in-memory cache backed by the given function. */
  public static <K, V> LoadingCache<K, V> create(Function<K, V> function) {
    return CacheBuilder.newBuilder().build(CacheLoader.from(function::apply));
  }
}
