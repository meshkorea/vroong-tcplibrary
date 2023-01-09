package com.vroong.tcp.client;

import com.vroong.tcp.TcpUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

@Slf4j
public class PooledTcpClient extends AbstractTcpClient {

  @Getter(AccessLevel.PRIVATE)
  private final ObjectPool<Tuple> pool;

  private Tuple currentTuple;

  public PooledTcpClient(String host, int port, Charset charset, int minIdle, int maxIdle, int maxTotal) {

    final GenericObjectPoolConfig<Tuple> config = new GenericObjectPoolConfig<>();
    // org.apache.commons.pool2.impl.GenericObjectPoolConfig.DEFAULT_MIN_IDLE = 0
    config.setMinIdle(minIdle);
    // org.apache.commons.pool2.impl.GenericObjectPoolConfig.DEFAULT_MAX_IDLE = 8
    config.setMaxIdle(maxIdle);
    // org.apache.commons.pool2.impl.GenericObjectPoolConfig.DEFAULT_MAX_TOTAL = 8
    config.setMaxTotal(maxTotal);
    config.setTestOnBorrow(true);
    config.setTestWhileIdle(true);

    final PooledObjectFactory<Tuple> factory = new BasePooledObjectFactory<Tuple>() {
      @Override
      public Tuple create() throws Exception {
        final Socket socket = createSocket(host, port, connectionTimeout, readTimeout);
        final BufferedOutputStream writer = new BufferedOutputStream(socket.getOutputStream());
        final BufferedInputStream reader = new BufferedInputStream(socket.getInputStream());

        return new Tuple(socket, writer, reader);
      }

      @Override
      public PooledObject<Tuple> wrap(Tuple obj) {
        return new DefaultPooledObject<>(obj);
      }

      @Override
      public boolean validateObject(PooledObject<Tuple> p) {
        final Socket socket = p.getObject().getSocket();
        if (socket != null) {
          final boolean isWriterAlive = !socket.isOutputShutdown();
          final boolean isReaderAlive = !socket.isInputShutdown();
          final boolean isConnectionAlive = socket.isConnected();
          return isWriterAlive && isReaderAlive && isConnectionAlive;
        }

        return false;
      }
    };

    this.pool = new GenericObjectPool<>(factory, config);
    try {
      this.pool.addObjects(minIdle);
    } catch (Exception e) {
      log.error(String.format("Socket add failed: %s", e.getMessage()), e);
    }

    this.charset = charset;
  }

  @Override
  public void write(byte[] message) throws Exception {
    currentTuple = pool.borrowObject();
    System.out.println("write: " + currentTuple.toString());

    final OutputStream writer = currentTuple.getWriter();
    writer.write(message);
    writer.flush();
  }

  @Override
  public byte[] read() throws Exception {
    System.out.println("reader: " + currentTuple.toString());
    final InputStream reader = currentTuple.getReader();
    final byte[] rawMessage = TcpUtils.readLine(reader);

    clearResources();

    return rawMessage;
  }

  private void clearResources() throws Exception {
    final Socket socket = currentTuple.getSocket();
    if (log.isDebugEnabled()) {
      log.debug("Returning socket: socket={}, srcPort={}", socket.getRemoteSocketAddress(), socket.getLocalPort());
    }

    pool.returnObject(currentTuple);

    if (log.isDebugEnabled()) {
      log.debug("ConnectionPool state: numIdle={}, numActive={}, returned={isConnectionAlive={}, isWriterAlive={}, isReaderAlive={}}",
          pool.getNumIdle(), pool.getNumActive(), socket.isConnected(), !socket.isOutputShutdown(),
          !socket.isInputShutdown());
    }
  }

  @Getter
  @AllArgsConstructor
  @ToString
  public static class Tuple {

    private Socket socket;
    private OutputStream writer;
    private InputStream reader;
  }
}
