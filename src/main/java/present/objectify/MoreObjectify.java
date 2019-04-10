package present.objectify;

import com.google.appengine.api.datastore.PropertyContainer;
import com.google.appengine.api.datastore.Query;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.impl.Path;
import com.googlecode.objectify.impl.translate.SaveContext;
import com.googlecode.objectify.impl.translate.Translator;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.google.appengine.api.datastore.Query.CompositeFilterOperator.and;
import static com.google.appengine.api.datastore.Query.FilterOperator.GREATER_THAN_OR_EQUAL;
import static com.google.appengine.api.datastore.Query.FilterOperator.LESS_THAN;
import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Objectify utilities.
 *
 * @author Bob Lee (bob@present.co)
 */
public class MoreObjectify {

  private MoreObjectify() {}

  /**
   * Saves the entities if they don't already exist in the datastore. Not transactional!
   *
   * @return the entities that were saved
   * @throws IllegalArgumentException if duplicate entities are detected
   */
  public static <T> Map<Key<T>, T> saveIfAbsent(Iterable<T> entities) {
    Map<Key<T>, T> all = Maps.uniqueIndex(entities, Key::create);
    if (all.isEmpty()) return Collections.emptyMap();
    Set<Key<T>> existing = ofy().load().group(KeysOnly.class).keys(all.keySet()).keySet();
    Map<Key<T>, T> saved = all.entrySet().stream()
        .filter(e -> !existing.contains(e.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    if (saved.isEmpty()) return Collections.emptyMap();
    ofy().save().entities(saved.values());
    return saved;
  }

  /** Asynchronously loads entities for the given keys. Ignores missing entities. */
  public static <E> Iterable<E> load(Iterable<Key<E>> keys) {
    Map<Key<E>, E> map = ofy().load().keys(keys);
    // Don't touch the map in this method! It'll block.
    return Iterables.filter(() -> map.values().iterator(), Objects::nonNull);
  }

  /** Delays creation of a map. */
  public static <K, V> Map<K, V> lazyMap(Supplier<Map<K, V>> constructor) {
    Supplier<Map<K, V>> memoized = Suppliers.memoize(constructor);
    return new ForwardingMap<K, V>() {
      @Override protected Map<K, V> delegate() {
        return memoized.get();
      }
    };
  }

  /** Delays creation of a list. */
  public static <T> List<T> lazyList(Supplier<List<T>> constructor) {
    Supplier<List<T>> memoized = Suppliers.memoize(constructor);
    return new ForwardingList<T>() {
      @Override protected List<T> delegate() {
        return memoized.get();
      }
    };
  }

  private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  /** Converts an entity to a pretty-printed string. Useful for debugging. */
  public static <T> String toString(T entity) {
    if (entity == null) return null;
    ObjectifyFactory factory = ofy().factory();
    @SuppressWarnings("unchecked")
    Translator<T, PropertyContainer> root =
        factory.getTranslators().getRoot((Class<T>) entity.getClass());
    PropertyContainer container = root.save(entity, false, new SaveContext(), Path.root());
    Map<String, Object> sorted = new TreeMap<>(container.getProperties());
    Key<T> key = Key.create(entity);
    return key.getRaw() + " " + gson.toJson(sorted);
  }

  /** Returns referenced object or null if the reference itself is null. */
  public static <T> T get(Ref<T> ref) {
    return ref == null ? null : ref.get();
  }

  /** Returns a filter that matches values with the given prefix. */
  public static Query.Filter startsWith(String fieldName, String prefix) {
    return and(
        new Query.FilterPredicate(fieldName, GREATER_THAN_OR_EQUAL, prefix),
        new Query.FilterPredicate(fieldName, LESS_THAN, prefix + "\ufffd")
    );
  }
}
