package present.media;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * @author Bob Lee
 */
public class AppEngineMediaService implements MediaService {

  private static Supplier<String> userSupplier;

  public static void setUserSupplier(Supplier<String> userSupplier) {
    AppEngineMediaService.userSupplier = userSupplier;
  }

  @Override public MediaResponse putMedia(PutMediaRequest request) throws IOException {
    if (userSupplier == null) {
      throw new IllegalStateException("Call AppEngineMediaService.setUserSupplier() first.");
    }

    throw new UnsupportedOperationException();
  }
}
