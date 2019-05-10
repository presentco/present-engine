package present.engine.log;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import present.engine.CurrentUser;
import present.engine.Futures;
import present.engine.log.DatastoreOperation.Fields;
import present.objectify.ForwardingDatastoreService;

/**
 * Logs puts and deletes for specified kinds.
 *
 * @author Bob Lee (bob@present.co)
 */
public class LoggingDatastoreService extends ForwardingDatastoreService {

  private final Set<String> kinds;

  public LoggingDatastoreService(AsyncDatastoreService delegate, Set<String> kinds) {
    super(delegate);
    this.kinds = kinds;
  }

  @Override public Future<Key> put(Entity entity) {
    if (shouldLog(entity)) {
      Entity op = newPutOp(entity);
      return Futures.map(put(Lists.newArrayList(entity, op)), keys -> keys.get(0));
    } else {
      return delegate.put(entity);
    }
  }

  @Override public Future<Key> put(Transaction transaction, Entity entity) {
    if (shouldLog(entity)) {
      Future<Key> f = delegate.put(transaction, entity);
      Entity op = newPutOp(entity);
      delegate.put(op);
      return f;
    } else {
      return delegate.put(transaction, entity);
    }
  }

  @Override public Future<List<Key>> put(Iterable<Entity> entities) {
    try {
      return delegate.put(entities);
    } finally {
      logPuts(entities);
    }
  }

  @Override public Future<List<Key>> put(Transaction transaction, Iterable<Entity> entities) {
    try {
      return delegate.put(transaction, entities);
    } finally {
      logPuts(entities);
    }
  }

  private void logPuts(Iterable<Entity> entities) {
    List<Entity> operations = Streams.stream(entities)
        .filter(this::shouldLog)
        .map(this::newPutOp)
        .collect(Collectors.toList());
    if (!operations.isEmpty()) put(operations);
  }

  @Override public Future<Void> delete(Iterable<Key> iterable) {
    try {
      return delegate.delete(iterable);
    } finally {
      logDeletes(iterable);
    }
  }

  @Override public Future<Void> delete(Transaction transaction, Iterable<Key> iterable) {
    try {
      return delegate.delete(transaction, iterable);
    } finally {
      logDeletes(iterable);
    }
  }

  @Override public Future<Void> delete(Transaction transaction, Key... keys) {
    return delete(transaction, Arrays.asList(keys));
  }

  private void logDeletes(Iterable<Key> entities) {
    List<Entity> operations = Streams.stream(entities)
        .filter(this::shouldLog)
        .map(this::newDeleteOp)
        .collect(Collectors.toList());
    if (!operations.isEmpty()) put(operations);
  }

  private Entity newPutOp(Entity entity) {
    Entity op = newOp(entity.getKey());
    EmbeddedEntity embedded = new EmbeddedEntity();
    embedded.setPropertiesFrom(entity);
    op.setProperty(Fields.entity.getName(), embedded);
    return op;
  }

  private Entity newDeleteOp(Key key) {
    Entity op = newOp(key);
    op.setProperty(Fields.entity.getName(), null);
    return op;
  }

  private Entity newOp(Key key) {
    Entity op = new Entity(DatastoreOperation.class.getSimpleName());
    op.setProperty(Fields.userId.getName(), CurrentUser.id());
    op.setProperty(Fields.timestamp.getName(), System.currentTimeMillis());
    op.setProperty(Fields.key.getName(), key);
    return op;
  }

  private boolean shouldLog(Entity entity) {
    return kinds.contains(entity.getKind());
  }

  private boolean shouldLog(Key key) {
    return kinds.contains(key.getKind());
  }
}
