package present.engine;

import java.util.function.Supplier;

/**
 * Provides access to the current user ID.
 *
 * @author Bob Lee
 */
public class CurrentUser {

  private static Supplier<String> idSupplier;

  /** Provides access to the current user. */
  public static void setIdSupplier(Supplier<String> userSupplier) {
    idSupplier = userSupplier;
  }

  /**
   * Returns the current user's ID.
   *
   * @throws IllegalStateException if setIdSupplier() hasn't been called.
   */
  public static String id() {
    if (idSupplier == null) {
      throw new IllegalStateException("Call Media.setUserIdSupplier() first.");
    }
    return idSupplier.get();
  }
}
