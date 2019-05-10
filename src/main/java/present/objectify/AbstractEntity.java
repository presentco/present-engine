package present.objectify;

import com.google.common.base.Suppliers;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Result;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.OnLoad;
import com.googlecode.objectify.annotation.OnSave;
import java.util.function.Consumer;
import java.util.function.Supplier;
import present.engine.AppEngine;

/**
 * Supports entity implementations.
 *
 * @author Bob Lee
 */
public abstract class AbstractEntity<T extends AbstractEntity<T>> {

  /** Create time: Set when entity instantiated. */
  public long createdTime = now();

  /** Update time: Set on entity save() or delete() */
  public long updatedTime;

  /** Deleted time */
  public Long deletedTime;

  /** True if the entity was just created and has not been persisted yet. */
  @Ignore public boolean nascent = true;

  /** Saves this entity. */
  public Result<Key<T>> save() {
    return ObjectifyService.ofy().save().entity(getThis());
  }

  @OnSave private void updateTime() {
    this.updatedTime = now();
  }

  @OnLoad private void removeNascent() {
    this.nascent = false;
  }

  /** Sets {@link #deletedTime}. */
  public T delete() {
    this.deletedTime = now();
    return getThis();
  }

  public boolean isDeleted() { return this.deletedTime != null; }

  private static long now() { return System.currentTimeMillis(); }

  @Ignore private final Supplier<Key<T>> keySupplier
      = Suppliers.memoize(() -> Key.create(getThis()));

  public Key<T> key() {
    return keySupplier.get();
  }

  public T reload() {
    return ObjectifyService.ofy().load().key(key()).now();
  }

  protected abstract T getThis();

  public String consoleUrl() {
    Key<?> key = key();
    // Note: Dev local console does not support edit view of entities.
    String project = AppEngine.applicationId();
    String consoleBaseUrl = "https://console.cloud.google.com/datastore/entities/edit";
    return consoleBaseUrl + "?key=" + key.toWebSafeString() + "&project=" + project
        + "&kind=" + key.getKind();
  }

  public boolean inTransaction(Updater<T> updater) {
    // Update this copy. Note: This copy will still be stale in other regards.
    updater.update(getThis());
    return ObjectifyService.ofy().transact(() -> {
      T latest = reload();
      if (latest == null || latest == this) {
        // This object isn't in the datastore yet or it's the same instance (because we're
        // running in a larger transaction).
        save();
        return true;
      } else {
        // Update version in the datastore.
        if (updater.update(latest)) {
          latest.save();
          return true;
        }
      }
      return false;
    });
  }

  public void inTransaction(Consumer<T> updater) {
    inTransaction(t -> {
      updater.accept(t);
      return true;
    });
  }

  @Override public String toString() {
    return debugString();
  }

  public String debugString() {
    return MoreObjectify.toString(this);
  }
}
