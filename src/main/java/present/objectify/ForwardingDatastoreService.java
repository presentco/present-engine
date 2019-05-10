package present.objectify;

import com.google.appengine.api.datastore.AsyncDatastoreService;
import com.google.appengine.api.datastore.DatastoreAttributes;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Index;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Forwards calls to a delegate datastore service. Enables intercepting select functionality.
 *
 * @author Bob Lee (bob@present.co)
 */
public abstract class ForwardingDatastoreService implements AsyncDatastoreService {

  protected final AsyncDatastoreService delegate;

  public ForwardingDatastoreService(AsyncDatastoreService delegate) {
    this.delegate = delegate;
  }

  @Override public Future<Entity> get(Key key) {
    return delegate.get(key);
  }

  @Override public Future<Entity> get(Transaction transaction, Key key) {
    return delegate.get(transaction, key);
  }

  @Override public Future<Map<Key, Entity>> get(Iterable<Key> iterable) {
    return delegate.get(iterable);
  }

  @Override public Future<Map<Key, Entity>> get(Transaction transaction, Iterable<Key> iterable) {
    return delegate.get(transaction, iterable);
  }

  @Override public Future<Key> put(Entity entity) {
    return delegate.put(entity);
  }

  @Override public Future<Key> put(Transaction transaction, Entity entity) {
    return delegate.put(transaction, entity);
  }

  @Override public Future<List<Key>> put(Iterable<Entity> iterable) {
    return delegate.put(iterable);
  }

  @Override public Future<List<Key>> put(Transaction transaction, Iterable<Entity> iterable) {
    return delegate.put(transaction, iterable);
  }

  @Override public Future<Void> delete(Key... keys) {
    return delegate.delete(keys);
  }

  @Override public Future<Void> delete(Transaction transaction, Key... keys) {
    return delegate.delete(transaction, keys);
  }

  @Override public Future<Void> delete(Iterable<Key> iterable) {
    return delegate.delete(iterable);
  }

  @Override public Future<Void> delete(Transaction transaction, Iterable<Key> iterable) {
    return delegate.delete(transaction, iterable);
  }

  @Override public Future<Transaction> beginTransaction() {
    return delegate.beginTransaction();
  }

  @Override public Future<Transaction> beginTransaction(TransactionOptions transactionOptions) {
    return delegate.beginTransaction(transactionOptions);
  }

  @Override public Future<KeyRange> allocateIds(String s, long l) {
    return delegate.allocateIds(s, l);
  }

  @Override public Future<KeyRange> allocateIds(Key key, String s, long l) {
    return delegate.allocateIds(key, s, l);
  }

  @Override public Future<DatastoreAttributes> getDatastoreAttributes() {
    return delegate.getDatastoreAttributes();
  }

  @Override public Future<Map<Index, Index.IndexState>> getIndexes() {
    return delegate.getIndexes();
  }

  @Override public PreparedQuery prepare(Query query) {
    return delegate.prepare(query);
  }

  @Override public PreparedQuery prepare(Transaction transaction, Query query) {
    return delegate.prepare(transaction, query);
  }

  @Override public Transaction getCurrentTransaction() {
    return delegate.getCurrentTransaction();
  }

  @Override public Transaction getCurrentTransaction(Transaction transaction) {
    return delegate.getCurrentTransaction(transaction);
  }

  @Override public Collection<Transaction> getActiveTransactions() {
    return delegate.getActiveTransactions();
  }
}
