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

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DscITv2Server implements DscServer {
   private final int port;
   private final List<IncomingConnectionListener> listeners = new CopyOnWriteArrayList();
   private EventLoopGroup bossGroup;
   private EventLoopGroup workerGroup;

   public DscITv2Server(int var1) {
      this.port = var1;
   }

   public void start() {
      if (this.bossGroup == null && this.workerGroup == null) {
         try {
            this.bossGroup = new NioEventLoopGroup();
            this.workerGroup = new NioEventLoopGroup();
            ServerBootstrap var1 = new ServerBootstrap();
            ((ServerBootstrap)((ServerBootstrap)var1.group(this.bossGroup, this.workerGroup).channel(NioServerSocketChannel.class)).childHandler(new DscChannelInitializer() {
               protected void inizialized(DscEndpoint var1, SocketChannel var2) {
                  Iterator var3 = DscITv2Server.this.listeners.iterator();

                  while(var3.hasNext()) {
                     IncomingConnectionListener var4 = (IncomingConnectionListener)var3.next();
                     var4.deviceConnected(var1);
                  }

               }
            }).option(ChannelOption.SO_BACKLOG, 128)).childOption(ChannelOption.SO_KEEPALIVE, true);
            var1.bind(this.port);
         } catch (RuntimeException var4) {
            try {
               this.stop(false);
            } catch (InterruptedException var3) {
               throw new AssertionError(var3);
            }

            throw var4;
         }
      } else {
         throw new IllegalStateException("already running");
      }
   }

   public void stop() throws InterruptedException {
      this.stop(true);
   }

   private void stop(boolean var1) throws InterruptedException {
      Future<?> var2 = null;
      Future<?> var3 = null;
      if (this.workerGroup != null) {
         var2 = this.workerGroup.shutdownGracefully();
         this.workerGroup = null;
      }

      if (this.bossGroup != null) {
         var3 = this.bossGroup.shutdownGracefully();
         this.bossGroup = null;
      }

      if (var1) {
         if (var2 != null) {
            var2.sync();
         }

         if (var3 != null) {
            var3.sync();
         }
      }

   }

   public void addIncomingConnectionListener(IncomingConnectionListener var1) {
      this.listeners.add(var1);
   }

   public void removeIncomingConnectionListener(IncomingConnectionListener var1) {
      this.listeners.remove(var1);
   }

}
