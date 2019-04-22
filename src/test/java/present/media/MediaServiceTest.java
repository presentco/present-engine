package present.media;

import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import java.io.IOException;
import java.net.URL;
import okio.ByteString;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import present.engine.CurrentUser;
import present.engine.Uuids;

import static org.junit.Assert.assertEquals;

public class MediaServiceTest {

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
      new LocalTaskQueueTestConfig(), new LocalBlobstoreServiceTestConfig(),
      new LocalDatastoreServiceTestConfig());

  private Closeable objectify;

  @Before public void setup() throws IOException {
    helper.setUp();
    objectify = ObjectifyService.begin();
  }

  @After public void tearDown() throws Exception {
    objectify.close();
    helper.tearDown();
  }

  @Test public void testUpload() throws IOException {
    CurrentUser.setIdSupplier(() -> Uuids.NULL);
    Media media = Media.upload(Uuids.NULL, "image/jpeg",
        ByteString.of(ByteStreams.toByteArray(getClass().getResourceAsStream("/test.png"))));
    System.out.println(media.url());
  }

  @Test public void testCopy() throws IOException {
    CurrentUser.setIdSupplier(() -> Uuids.NULL);
    String url = "https://www.google.com/images/branding/googleg/1x/googleg_standard_color_128dp.png";
    Media media = Media.copy(Uuids.NULL, url);
    assertEquals(url, media.sourceUrl);
    System.out.println(media.url());
  }
}
