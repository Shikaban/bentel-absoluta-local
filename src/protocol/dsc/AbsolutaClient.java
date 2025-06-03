package protocol.dsc;

import com.google.common.base.Preconditions;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import protocol.dsc.commands.DscCommand;
import protocol.dsc.commands.UserActivity;
import protocol.dsc.session.DscChannelInitializer;
import protocol.dsc.session.DscEndpoint;
import protocol.dsc.session.SessionInfo;
import protocol.dsc.transport.command_handlers.HandshakeHandler;
import protocol.dsc.transport.command_handlers.OpenSessionHandler;
import protocol.dsc.transport.command_handlers.PollHandler;
import protocol.dsc.transport.command_handlers.RequestAccessHandler;
import protocol.dsc.transport.command_handlers.SoftwareVersionHandler;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;

public class AbsolutaClient implements ITv2Client {
   private final String hostname;
   private final int port;

   public AbsolutaClient(String var1, int var2) {
      this.hostname = (String)Preconditions.checkNotNull(var1);
      this.port = var2;
   }

   public void connect(ITv2Client.Callback var1) throws InterruptedException {
      Preconditions.checkNotNull(var1);
      InetSocketAddress var2 = new InetSocketAddress(this.hostname, this.port);
      System.out.println("INFO: connecting to " + var2);
      NioEventLoopGroup var3 = new NioEventLoopGroup();

      try {
         Bootstrap var4 = new Bootstrap();
         var4.group(var3);
         var4.channel(NioSocketChannel.class);
         var4.option(ChannelOption.SO_KEEPALIVE, true);
         var4.handler(new AbsolutaClient.ChannelInitializer(var1));
         ChannelFuture var5 = var4.connect(var2).sync();
         Channel var6 = var5.channel();
         var6.closeFuture().sync();
      } finally {
         System.out.println("INFO: disconnected from " + var2);
         var3.shutdownGracefully();
      }

   }

   private class ChannelInitializer extends DscChannelInitializer {
      private final ITv2Client.Callback callback;

      ChannelInitializer(ITv2Client.Callback var2) {
         this.callback = var2;
      }

      protected SessionInfo getOwnInfo() {
         SessionInfo var1 = new SessionInfo();
         var1.setClient(true);
         var1.setDeviceTypeOrVendorID(143);
         var1.setDeviceId(0);
         var1.setSoftwareVersion("0100");
         var1.setProtocolVersion("0203");
         var1.setTxSize(50);
         var1.setRxSize(1024);
         var1.setEncryptionType(0);
         var1.setIdentifierOrInitKey("00000000");
         var1.setSoftwareVersionFields("35 00 00 1E 02 03 00 00 01 03 01");
         return var1;
      }

      protected Queue<HandshakeHandler<?>> buildHandshakeHandlers() {
         Queue<HandshakeHandler<?>> var1 = new ArrayDeque<HandshakeHandler<?>>(3);
         var1.add(new OpenSessionHandler());
         var1.add(new RequestAccessHandler());
         var1.add(new SoftwareVersionHandler());
         return var1;
      }

      protected int writerIdleTimeSeconds() {
         return 5;
      }

      protected PollHandler.PollFactory pollFactory() {
         return new PollHandler.PollFactory() {
            public DscCommand createPoll() {
               UserActivity var1 = new UserActivity();
               var1.setPartitionNumber((Integer)null);
               var1.setType(4);
               return var1;
            }
         };
      }

      protected void inizialized(DscEndpoint var1, SocketChannel var2) {
         this.callback.connected(var1);
      }
   }
}
