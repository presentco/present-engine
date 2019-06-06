package present.rpc;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import present.engine.Uuids;

/**
 * Creates Present RPC clients that invoke RPCs asynchronously via the App Engine task queue.
 * RPC methods always return null as they're executed asynchronously.
 *
 * @author Bob Lee (bob@present.co)
 */
public class RpcQueue<T> {

  private static final Logger logger = LoggerFactory.getLogger(RpcQueue.class);

  private static ThreadLocal<Map<String, List<TaskOptions>>> localBatches = new ThreadLocal<>();

  private final Class<T> service;
  private TaskOptions options = TaskOptions.Builder.withDefaults();
  private String queueName = "default";
  private final Map<String, String> headers = new HashMap<>();
  private String host;

  private RpcQueue(Class<T> service) {
    this.service = service;
  }

  /** Convenience method, equivalent to: {@code RpcQueue.to(service).create()} */
  public static <T> T create(Class<T> service) {
    return RpcQueue.to(service).create();
  }

  /** Starts building an RPC queue for the given service. */
  public static <T> RpcQueue<T> to(Class<T> service) {
    return new RpcQueue<>(service);
  }

  /** Optionally sets the task options. Use to add delays, etc. */
  public RpcQueue<T> with(TaskOptions options) {
    this.options = options;
    return this;
  }

  /** Don't retry the task if it fails. */
  public RpcQueue<T> noRetries() {
    options = options.retryOptions(RetryOptions.Builder.withTaskRetryLimit(0));
    return this;
  }

  /** Optionally sets the queue name. */
  public RpcQueue<T> in(String queueName) {
    this.queueName = queueName;
    return this;
  }

  /** Optional host URL to execute the task against. */
  public RpcQueue<T> host(String host) {
    this.host = host;
    return this;
  }

  /** Adds an HTTP header. */
  public RpcQueue<T> header(String name, String value) {
    this.headers.put(name, value);
    return this;
  }

  /** Creates a client that enqueues RPCs. */
  public T create() {
    return service.cast(Proxy.newProxyInstance(service.getClassLoader(),
        new Class<?>[] { service }, new Handler(this)));
  }

  /**
   * Batches RPC tasks enqueued during {@code r}. Does not enqueue tasks if {@code r} throws.
   *
   * @throws IllegalStateException if already in a batch
   */
  public static void batch(Runnable r) {
    Preconditions.checkState(localBatches.get() == null, "Already in a batch.");
    localBatches.set(new HashMap<>());
    try {
      r.run();
      Map<String, List<TaskOptions>> batches = localBatches.get();
      if (!batches.isEmpty()) {
        logger.info("Enqueueing {} tasks.", batches.size());
        batches.forEach((queueName, batch) -> {
          // We can only enqueue 100 tasks at a time.
          Lists.partition(batch, 100).forEach(chunk -> {
            QueueFactory.getQueue(queueName).add(chunk);
          });
        });
      }
    } finally {
      localBatches.remove();
    }
  }

  private static class Handler implements InvocationHandler {

    private final String serviceName;
    private final Map<String, RpcMethod> methods;
    private final TaskOptions task;
    private final String queueName;
    private final Map<String, String> headers;
    private final String host;

    private Handler(RpcQueue<?> rpcQueue) {
      methods = RpcMethod.mapFor(rpcQueue.service);
      this.serviceName = rpcQueue.service.getSimpleName();
      this.task = new TaskOptions(rpcQueue.options);
      this.queueName = rpcQueue.queueName;
      this.headers = new HashMap<>(rpcQueue.headers);
      this.host = rpcQueue.host;
    }

    @Override public Object invoke(Object proxy, Method javaMethod, Object[] args)
        throws Throwable {
      String methodName = javaMethod.getName();
      Object argument = args[0];
      String url = "/" + serviceName + "/" + methodName;
      String traceId = Uuids.newUuid().replace("-", "");
      TaskOptions task = new TaskOptions(this.task);
      RpcEncoding encoding = RpcEncoding.PROTO;
      byte[] argumentBytes = encoding.encode(argument);
      task.url(url)
        .method(TaskOptions.Method.POST)
        .header("Accept", "text/plain")
        .header("X-Cloud-Trace-Context", traceId + "/0;o=1")
        .payload(argumentBytes, encoding.contentType);
      if (this.host != null) task.header("Host", this.host);
      this.headers.forEach(task::header);
      Map<String, List<TaskOptions>> batches = localBatches.get();
      if (batches != null) {
        List<TaskOptions> batch = batches.computeIfAbsent(this.queueName,
            (key) -> new ArrayList<>());
        batch.add(task);
      } else {
        QueueFactory.getQueue(this.queueName).add(task);
      }
      return null;
    }
  }
}
