package present.media;

import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import java.io.IOException;
import java.net.URL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import present.engine.CurrentUser;
import present.engine.Uuids;

public class MediaServiceTest {

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
      new LocalTaskQueueTestConfig(), new LocalBlobstoreServiceTestConfig(),
      new LocalDatastoreServiceTestConfig());

  @Before public void setup() throws IOException {
    helper.setUp();
  }

  @After public void tearDown() throws Exception {
    helper.tearDown();
  }

  @Test public void testUpload() throws IOException {
    try (Closeable closeable = ObjectifyService.begin()) {
      CurrentUser.setIdSupplier(() -> Uuids.NULL);
      Media media = Media.upload(Uuids.NULL, "image/jpeg",
          new URL(
              "https://www.google.com/images/branding/googleg/1x/googleg_standard_color_128dp.png"));
      System.out.println(media.url());
    }
  }
}
