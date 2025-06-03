package protocol.dsc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import protocol.dsc.session.DscChannelInitializer;
import protocol.dsc.session.DscEndpoint;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DscITv2Server implements DscServer {
   private final int port;
   private final List<IncomingConnectionListener> listeners = new CopyOnWriteArrayList<>();
   private EventLoopGroup bossGroup;
   private EventLoopGroup workerGroup;

   public DscITv2Server(int port) {
      this.port = port;
   }

   @SuppressWarnings("deprecation")
   public void start() {
      if (this.bossGroup == null && this.workerGroup == null) {
         try {
               this.bossGroup = new NioEventLoopGroup();
               this.workerGroup = new NioEventLoopGroup();
               ServerBootstrap bootstrap = new ServerBootstrap();
               bootstrap.group(this.bossGroup, this.workerGroup)
                     .channel(NioServerSocketChannel.class)
                     .childHandler(new DscChannelInitializer() {
                           @Override
                           protected void inizialized(DscEndpoint endpoint, SocketChannel channel) {
                              for (IncomingConnectionListener listener : listeners) {
                                 listener.deviceConnected(endpoint);
                              }
                           }
                     })
                     .option(ChannelOption.SO_BACKLOG, 128)
                     .childOption(ChannelOption.SO_KEEPALIVE, true);
               bootstrap.bind(this.port);
         } catch (RuntimeException ex) {
               try {
                  this.stop(false);
               } catch (InterruptedException ie) {
                  throw new AssertionError(ie);
               }
               throw ex;
         }
      } else {
         throw new IllegalStateException("Server is already running");
      }
   }

   public void stop() throws InterruptedException {
      this.stop(true);
   }

   private void stop(boolean wait) throws InterruptedException {
      Future<?> workerFuture = null;
      Future<?> bossFuture = null;
      if (this.workerGroup != null) {
         workerFuture = this.workerGroup.shutdownGracefully();
         this.workerGroup = null;
      }
      if (this.bossGroup != null) {
         bossFuture = this.bossGroup.shutdownGracefully();
         this.bossGroup = null;
      }
      if (wait) {
         if (workerFuture != null) {
               workerFuture.sync();
         }
         if (bossFuture != null) {
               bossFuture.sync();
         }
      }
   }

   public void addIncomingConnectionListener(IncomingConnectionListener listener) {
      this.listeners.add(listener);
   }

   public void removeIncomingConnectionListener(IncomingConnectionListener listener) {
      this.listeners.remove(listener);
   }
}
