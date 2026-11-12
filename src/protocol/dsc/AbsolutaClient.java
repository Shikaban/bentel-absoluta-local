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
import java.util.logging.Logger;

public class AbsolutaClient implements ITv2Client {
   private static final Logger logger = Logger.getLogger(AbsolutaClient.class.getName());
   private final String hostname;
   private final int port;

   public AbsolutaClient(String text, int port) {
      this.hostname = (String)Preconditions.checkNotNull(text);
      this.port = port;
   }

   @SuppressWarnings("deprecation")
   public void connect(ITv2Client.Callback callback2) throws InterruptedException {
      Preconditions.checkNotNull(callback2);
      InetSocketAddress inetSocketAddress2 = new InetSocketAddress(this.hostname, this.port);
      logger.info("Connecting to " + inetSocketAddress2);
      NioEventLoopGroup nioEventLoopGroup2 = new NioEventLoopGroup();

      try {
         Bootstrap bootstrap2 = new Bootstrap();
         bootstrap2.group(nioEventLoopGroup2);
         bootstrap2.channel(NioSocketChannel.class);
         bootstrap2.option(ChannelOption.SO_KEEPALIVE, true);
         bootstrap2.handler(new AbsolutaClient.ChannelInitializer(callback2));
         ChannelFuture channelFuture2 = bootstrap2.connect(inetSocketAddress2).sync();
         Channel channel2 = channelFuture2.channel();
         channel2.closeFuture().sync();
      } finally {
         logger.info("Disconnected from " + inetSocketAddress2);
         nioEventLoopGroup2.shutdownGracefully();
      }

   }

   private class ChannelInitializer extends DscChannelInitializer {
      private final ITv2Client.Callback callback;

      ChannelInitializer(ITv2Client.Callback callback2) {
         this.callback = callback2;
      }

      protected SessionInfo buildOwnSessionInfo() {
         SessionInfo sessionInfo2 = new SessionInfo();
         sessionInfo2.setClient(true);
         sessionInfo2.setDeviceTypeOrVendorID(143);
         sessionInfo2.setDeviceId(0);
         sessionInfo2.setSoftwareVersion("0100");
         sessionInfo2.setProtocolVersion("0203");
         sessionInfo2.setTxSize(50);
         sessionInfo2.setRxSize(1024);
         sessionInfo2.setEncryptionType(0);
         sessionInfo2.setIdentifierOrInitKey("00000000");
         sessionInfo2.setSoftwareVersionFields("35 00 00 1E 02 03 00 00 01 03 01");
         return sessionInfo2;
      }

      protected Queue<HandshakeHandler<?>> buildHandshakeHandlers() {
         Queue<HandshakeHandler<?>> queue2 = new ArrayDeque<HandshakeHandler<?>>(3);
         queue2.add(new OpenSessionHandler());
         queue2.add(new RequestAccessHandler());
         queue2.add(new SoftwareVersionHandler());
         return queue2;
      }

      protected int getWriterIdleTimeSeconds() {
         return 5;
      }

      protected PollHandler.PollFactory pollFactory() {
         return new PollHandler.PollFactory() {
            public DscCommand createPoll() {
               UserActivity userActivity2 = new UserActivity();
               userActivity2.setPartitionNumber((Integer)null);
               userActivity2.setType(4);
               return userActivity2;
            }
         };
      }

      protected void onInitialized(DscEndpoint endpoint, SocketChannel socketChannel2) {
         this.callback.connected(endpoint);
      }
   }
}