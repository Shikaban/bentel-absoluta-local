package protocol.dsc.session;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import protocol.dsc.DscEndpointState;
import protocol.dsc.commands.DscCommand;
import protocol.dsc.commands.Poll;
import protocol.dsc.transport.ACKHandler;
import protocol.dsc.transport.AESDecoder;
import protocol.dsc.transport.AESEncoder;
import protocol.dsc.transport.CommandDecoder;
import protocol.dsc.transport.CommandEncoder;
import protocol.dsc.transport.CommandQueueHandler;
import protocol.dsc.transport.DataDecoder;
import protocol.dsc.transport.DataEncoder;
import protocol.dsc.transport.EndpointHandler;
import protocol.dsc.transport.FallbackHandler;
import protocol.dsc.transport.FlushCommandHandler;
import protocol.dsc.transport.FrameDecoder;
import protocol.dsc.transport.FrameEncoder;
import protocol.dsc.transport.MultiplePacketsDecoder;
import protocol.dsc.transport.PipelineHandler;
import protocol.dsc.transport.ResponseHandler;
import protocol.dsc.transport.TransportLayerDecoder;
import protocol.dsc.transport.TransportLayerEncoder;
import protocol.dsc.transport.command_handlers.EndSessionHandler;
import protocol.dsc.transport.command_handlers.HandshakeHandler;
import protocol.dsc.transport.command_handlers.MiscNotificationHandler;
import protocol.dsc.transport.command_handlers.OpenSessionHandler;
import protocol.dsc.transport.command_handlers.PollHandler;
import protocol.dsc.transport.command_handlers.RequestAccessHandler;
import protocol.dsc.transport.command_handlers.RequestedReadingHandler;
import protocol.dsc.transport.command_handlers.TextNotificationHandler;
import protocol.dsc.transport.command_handlers.TroubleDetailNotificationHandler;
import protocol.dsc.transport.command_handlers.UserPartitionAssignmentConfigurationHandler;
import protocol.dsc.transport.command_handlers.WritingHandler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DscChannelInitializer extends ChannelInitializer<SocketChannel> {
   private static final FrameEncoder FRAME_ENCODER = new FrameEncoder();
   private static final AESDecoder AES_DECODER = new AESDecoder();
   private static final AESEncoder AES_ENCODER = new AESEncoder();
   private static final DataDecoder DATA_DECODER = new DataDecoder();
   private static final DataEncoder DATA_ENCODER = new DataEncoder();
   private static final TransportLayerDecoder TRANSPORT_LAYER_DECODER = new TransportLayerDecoder();
   private static final TransportLayerEncoder TRANSPORT_LAYER_ENCODER = new TransportLayerEncoder();
   private static final MultiplePacketsDecoder MULTIPLE_PACKETS_DECODER = new MultiplePacketsDecoder();
   private static final CommandDecoder COMMAND_DECODER = new CommandDecoder();
   private static final CommandEncoder COMMAND_ENCODER = new CommandEncoder();
   private static final FlushCommandHandler FLUSH_COMMAND_HANDLER = new FlushCommandHandler();
   private static final ACKHandler ACK_HANDLER = new ACKHandler();
   private static final EndSessionHandler END_SESSION_HANDLER = new EndSessionHandler();
   private static final FallbackHandler FALLBACK_HANDLER = new FallbackHandler();
   private static final TextNotificationHandler TEXT_NOTIFICATION_HANDLER = new TextNotificationHandler();
   private static final UserPartitionAssignmentConfigurationHandler USER_PARTITION_ASSIGNMENT_CONFIGURATION_HANDLER = new UserPartitionAssignmentConfigurationHandler();
   private static final TroubleDetailNotificationHandler TROUBLE_DETAIL_NOTIFICATION_HANDLER = new TroubleDetailNotificationHandler();
   private static final MiscNotificationHandler MISC_NOTIFICATION_HANDLER = new MiscNotificationHandler();
   private static final WritingHandler WRITING_HANDLER = new WritingHandler();
   private static final AtomicInteger counter = new AtomicInteger();
   private final int num;
   private static final boolean VERBOSE_DEBUG = false;

   public DscChannelInitializer() {
      this.num = counter.getAndIncrement();
   }

   protected SessionInfo getOwnInfo() {
      SessionInfo var1 = new SessionInfo();
      var1.setClient(false);
      var1.setDeviceTypeOrVendorID(143);
      var1.setDeviceId(0);
      var1.setSoftwareVersion("0100");
      var1.setProtocolVersion("0211");
      var1.setTxSize(65535);
      var1.setRxSize(65535);
      var1.setEncryptionType(1);
      var1.setIdentifierOrInitKey("12345678");
      return var1;
   }

   protected Queue<HandshakeHandler<?>> buildHandshakeHandlers() {
      Queue<HandshakeHandler<?>> var1 = new ArrayDeque<HandshakeHandler<?>>(2);
      var1.add(new OpenSessionHandler());
      var1.add(new RequestAccessHandler());
      return var1;
   }

   protected List<ChannelHandler> buildNormalModeHandlers() {
      List<ChannelHandler> var1 = new ArrayList<ChannelHandler>(6);
      var1.add(new RequestedReadingHandler());
      var1.add(WRITING_HANDLER);
      var1.add(TEXT_NOTIFICATION_HANDLER);
      var1.add(USER_PARTITION_ASSIGNMENT_CONFIGURATION_HANDLER);
      var1.add(TROUBLE_DETAIL_NOTIFICATION_HANDLER);
      var1.add(MISC_NOTIFICATION_HANDLER);
      return var1;
   }

   protected int writerIdleTimeSeconds() {
      return 30;
   }

   protected PollHandler.PollFactory pollFactory() {
      return new PollHandler.PollFactory() {
         public DscCommand createPoll() {
            return new Poll();
         }
      };
   }

   protected abstract void inizialized(DscEndpoint var1, SocketChannel var2);

   protected final void initChannel(SocketChannel var1) throws Exception {
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: initializing channel for connection number " + this.num + " ...");
      }
      final DscEndpoint var2 = new DscEndpoint(var1);
      var1.closeFuture().addListener(new ChannelFutureListener() {
         public void operationComplete(ChannelFuture var1) throws Exception {
            var2.setState(DscEndpointState.CLOSED);
         }
      });
      SessionInfo.initInfo(var1, this.getOwnInfo());
      byte var3 = 0;
      ChannelPipeline var10000 = var1.pipeline();
      ChannelHandler[] var10001 = new ChannelHandler[1];
      int var4 = var3 + 1;
      var10001[0] = this.newLoggingHandler(var3);
      var10000.addLast(var10001).addLast(new ChannelHandler[]{new ReadTimeoutHandler(45)}).addLast(new ChannelHandler[]{new WriteTimeoutHandler(45)}).addLast(new ChannelHandler[]{new IdleStateHandler(0, this.writerIdleTimeSeconds(), 0)}).addLast(new ChannelHandler[]{new FrameDecoder()}).addLast(new ChannelHandler[]{FRAME_ENCODER}).addLast(new ChannelHandler[]{AES_DECODER}).addLast(new ChannelHandler[]{AES_ENCODER}).addLast(new ChannelHandler[]{this.newLoggingHandler(var4++)}).addLast(new ChannelHandler[]{DATA_DECODER}).addLast(new ChannelHandler[]{DATA_ENCODER}).addLast(new ChannelHandler[]{this.newLoggingHandler(var4++)}).addLast(new ChannelHandler[]{TRANSPORT_LAYER_DECODER}).addLast(new ChannelHandler[]{TRANSPORT_LAYER_ENCODER}).addLast(new ChannelHandler[]{MULTIPLE_PACKETS_DECODER}).addLast(new ChannelHandler[]{this.newLoggingHandler(var4++)}).addLast(new ChannelHandler[]{COMMAND_DECODER}).addLast(new ChannelHandler[]{COMMAND_ENCODER}).addLast(new ChannelHandler[]{this.newLoggingHandler(var4++)}).addLast(new ChannelHandler[]{FLUSH_COMMAND_HANDLER}).addLast(new ChannelHandler[]{ACK_HANDLER}).addLast(new ChannelHandler[]{new ResponseHandler()}).addLast(new ChannelHandler[]{new CommandQueueHandler()}).addLast(new ChannelHandler[]{this.newLoggingHandler(var4++)}).addLast(new ChannelHandler[]{END_SESSION_HANDLER}).addLast(new ChannelHandler[]{new PollHandler(this.pollFactory())}).addLast(new ChannelHandler[]{new PipelineHandler(this.buildHandshakeHandlers(), this.buildNormalModeHandlers())}).addLast(new ChannelHandler[]{this.newLoggingHandler(var4++)}).addLast(new ChannelHandler[]{FALLBACK_HANDLER}).addLast(new ChannelHandler[]{new EndpointHandler(var2)}).addLast(new ChannelHandler[]{this.newLoggingHandler(var4++)});
      this.inizialized(var2, var1);
   }

   private LoggingHandler newLoggingHandler(int var1) {
      return new LoggingHandler("DEBUG: .LoggingHandler_" + var1 + ".connection_" + this.num, LogLevel.TRACE);
   }
}
