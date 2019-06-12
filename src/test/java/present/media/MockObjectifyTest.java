package present.media;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import org.junit.Test;
import present.objectify.MockObjectify;

import static org.junit.Assert.assertEquals;

public class MockObjectifyTest {

  @Test public void ref() {
    MyEntity entity = new MyEntity(42);
    Ref<MyEntity> ref = MockObjectify.ref(entity);
    assertEquals(entity, ref.get());
    assertEquals(42, ref.key().getId());
    assertEquals("MyEntity", ref.key().getKind());
  }

  @Test public void key() {
    assertEquals(42, MockObjectify.key(MyEntity.class, 42).getId());
    assertEquals("foo", MockObjectify.key(MyEntity.class, "foo").getName());
  }

  @Entity public static class MyEntity {
    @Id long id;

    public MyEntity(long id) {
      this.id = id;
    }
  }
}
