package com.vroong.tcp.client;

import com.vroong.tcp.config.TcpClientProperties;
import com.vroong.tcp.message.strategy.HeaderStrategy;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
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
  private ObjectPool<Tuple> pool;

  public PooledTcpClient(TcpClientProperties properties) {
    super(properties);
    initPool(properties);
  }

  public PooledTcpClient(TcpClientProperties properties, HeaderStrategy strategy, boolean useTLS) {
    super(properties, strategy, useTLS);
    initPool(properties);
  }

  private void initPool(TcpClientProperties properties) {
    final PooledObjectFactory<Tuple> poolFactory = new BasePooledObjectFactory<Tuple>() {
      @Override
      public Tuple create() throws Exception {
        final Socket socket = createSocket();
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

    final GenericObjectPoolConfig<Tuple> poolConfig = new GenericObjectPoolConfig<>();
    // org.apache.commons.pool2.impl.GenericObjectPoolConfig.DEFAULT_MIN_IDLE = 0
    poolConfig.setMinIdle(properties.getPool().getMinIdle());
    // org.apache.commons.pool2.impl.GenericObjectPoolConfig.DEFAULT_MAX_IDLE = 8
    poolConfig.setMaxIdle(properties.getPool().getMaxIdle());
    // org.apache.commons.pool2.impl.GenericObjectPoolConfig.DEFAULT_MAX_TOTAL = 8
    poolConfig.setMaxTotal(properties.getPool().getMaxTotal());
    poolConfig.setTestOnBorrow(true);
    poolConfig.setTestWhileIdle(true);

    this.pool = new GenericObjectPool<>(poolFactory, poolConfig);
    try {
      this.pool.addObjects(properties.getPool().getMinIdle());
    } catch (Exception e) {
      log.error(String.format("Socket add failed: %s", e.getMessage()), e);
    }
  }

  @Override
  public byte[] send(byte[] body) throws Exception {
    final Tuple currentTuple = pool.borrowObject();
    final OutputStream writer = currentTuple.getWriter();
    final InputStream reader = currentTuple.getReader();

    strategy.write(writer, body);
    final byte[] response = strategy.read(reader);

    clearResources(currentTuple);

    return response;
  }

  private void clearResources(Tuple currentTuple) throws Exception {
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
