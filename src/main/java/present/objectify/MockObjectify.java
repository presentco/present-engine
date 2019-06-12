package present.objectify;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.apphosting.api.ApiProxy;
import com.google.common.cache.LoadingCache;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.impl.KeyMetadata;
import com.googlecode.objectify.impl.Path;
import com.googlecode.objectify.impl.translate.CreateContext;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import present.engine.Caches;

/**
 * Creates Objectify mocks.
 *
 * @author Bob Lee
 */
public class MockObjectify {

  /** Creates a reference to the given instance. */
  public static <T> Ref<T> ref(T instance) {
    return new Ref<T>() {
      @Override public T get() {
        return instance;
      }

      @Override public boolean isLoaded() {
        return true;
      }

      @Override public Key<T> key() {
        return MockObjectify.key(instance);
      }
    };
  }

  /** Creates a key with a mock application ID. */
  public static <T> Key<T> key(T instance) {
    @SuppressWarnings("unchecked")
    KeyMetadata<T> metadata = (KeyMetadata<T>) keyMetadatas.getUnchecked(instance.getClass());
    return inMockEnvironment(() -> Key.create(metadata.getRawKey(instance)));
  }

  /** Creates a key with a mock application ID. */
  public static <T> Key<T> key(Class<? extends T> kindClass, long id) {
    KeyMetadata<T> metadata = keyMetadata(kindClass);
    return inMockEnvironment(() -> Key.create(KeyFactory.createKey(metadata.getKind(), id)));
  }

  /** Creates a key with a mock application ID. */
  public static <T> Key<T> key(Class<? extends T> kindClass, String name) {
    KeyMetadata<T> metadata = keyMetadata(kindClass);
    return inMockEnvironment(() -> Key.create(KeyFactory.createKey(metadata.getKind(), name)));
  }

  /** Creates a key with a mock application ID. */
  public static <T> Key<T> key(Key<?> parent, Class<? extends T> kindClass, long id) {
    KeyMetadata<T> metadata = keyMetadata(kindClass);
    return inMockEnvironment(() -> Key.create(KeyFactory.createKey(parent.getRaw(), metadata.getKind(), id)));
  }

  /** Creates a key with a mock application ID. */
  public static <T> Key<T> key(Key<?> parent, Class<? extends T> kindClass, String name) {
    KeyMetadata<T> metadata = keyMetadata(kindClass);
    return inMockEnvironment(() -> Key.create(KeyFactory.createKey(parent.getRaw(), metadata.getKind(), name)));
  }

  private static final ObjectifyFactory factory = new ObjectifyFactory();

  private static final LoadingCache<Class<?>, KeyMetadata<?>> keyMetadatas = Caches.create(
      type -> new KeyMetadata<>(type, new CreateContext(factory), Path.root()));

  @SuppressWarnings("unchecked")
  private static <T> KeyMetadata<T> keyMetadata(Class<? extends T> clazz) {
    return (KeyMetadata<T>) keyMetadatas.getUnchecked(clazz);
  }

  private static <T> T inMockEnvironment(Supplier<T> supplier) {
    ApiProxy.Environment original = ApiProxy.getCurrentEnvironment();
    try {
      ApiProxy.setEnvironmentForCurrentThread(mockEnvironment);
      return supplier.get();
    } finally {
      ApiProxy.setEnvironmentForCurrentThread(original);
    }
  }

  private static final ApiProxy.Environment mockEnvironment = new ApiProxy.Environment() {
    @Override public String getAppId() {
      return "mock";
    }

    @Override public String getModuleId() {
      throw new UnsupportedOperationException();
    }

    @Override public String getVersionId() {
      throw new UnsupportedOperationException();
    }

    @Override public String getEmail() {
      throw new UnsupportedOperationException();
    }

    @Override public boolean isLoggedIn() {
      throw new UnsupportedOperationException();
    }

    @Override public boolean isAdmin() {
      throw new UnsupportedOperationException();
    }

    @Override public String getAuthDomain() {
      throw new UnsupportedOperationException();
    }

    @Override public String getRequestNamespace() {
      throw new UnsupportedOperationException();
    }

    @Override public Map<String, Object> getAttributes() {
      return Collections.emptyMap();
    }

    @Override public long getRemainingMillis() {
      throw new UnsupportedOperationException();
    }
  };
}