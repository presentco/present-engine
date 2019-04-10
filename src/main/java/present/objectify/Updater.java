package present.objectify;

/**
 * Updates an entity.
 *
 * @author Bob Lee (bob@present.co)
 */
public interface Updater<E> {

  /**
   * Updates the given entity. Returns true if the entity was updated.
   */
  boolean update(E entity);
}
