package protocol.dsc.session;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import protocol.dsc.DscEndpointState;
import protocol.dsc.commands.DscCommand;
import protocol.dsc.commands.Poll;
import protocol.dsc.transport.*;
import protocol.dsc.transport.command_handlers.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public abstract class DscChannelInitializer extends ChannelInitializer<SocketChannel> {
   private static final Logger LOGGER = Logger.getLogger(DscChannelInitializer.class.getName());

   // Handler statici riutilizzabili
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
   private static final UserPartitionAssignmentConfigurationHandler USER_PARTITION_ASSIGNMENT_HANDLER = new UserPartitionAssignmentConfigurationHandler();
   private static final TroubleDetailNotificationHandler TROUBLE_DETAIL_HANDLER = new TroubleDetailNotificationHandler();
   private static final MiscNotificationHandler MISC_NOTIFICATION_HANDLER = new MiscNotificationHandler();
   private static final WritingHandler WRITING_HANDLER = new WritingHandler();

   private static final AtomicInteger CONNECTION_COUNTER = new AtomicInteger();
   private final int connectionId;

   public DscChannelInitializer() {
      this.connectionId = CONNECTION_COUNTER.getAndIncrement();
   }

   // Default session information for this endpoint
   protected SessionInfo buildOwnSessionInfo() {
      SessionInfo info = new SessionInfo();
      info.setClient(false);
      info.setDeviceTypeOrVendorID(143);
      info.setDeviceId(0);
      info.setSoftwareVersion("0100");
      info.setProtocolVersion("0211");
      info.setTxSize(65535);
      info.setRxSize(65535);
      info.setEncryptionType(1);
      info.setIdentifierOrInitKey("12345678");
      return info;
   }

   // Build the handler sequence for the initial handshake
   protected Queue<HandshakeHandler<?>> buildHandshakeHandlers() {
      Queue<HandshakeHandler<?>> handshakeHandlers = new ArrayDeque<>(2);
      handshakeHandlers.add(new OpenSessionHandler());
      handshakeHandlers.add(new RequestAccessHandler());
      return handshakeHandlers;
   }

   // Build the handler list for normal operation mode
   protected List<ChannelHandler> buildNormalModeHandlers() {
      List<ChannelHandler> handlers = new ArrayList<>(6);
      handlers.add(new RequestedReadingHandler());
      handlers.add(WRITING_HANDLER);
      handlers.add(TEXT_NOTIFICATION_HANDLER);
      handlers.add(USER_PARTITION_ASSIGNMENT_HANDLER);
      handlers.add(TROUBLE_DETAIL_HANDLER);
      handlers.add(MISC_NOTIFICATION_HANDLER);
      return handlers;
   }

   // Writer idle timeout (poll)
   protected int getWriterIdleTimeSeconds() {
      return Consts.DEFAULT_POLL_TIMEOUT;
   }

   // Factory for creating poll commands
   protected PollHandler.PollFactory pollFactory() {
      return new PollHandler.PollFactory() {
         public DscCommand createPoll() {
               return new Poll();
         }
      };
   }

   // To be implemented: called when the pipeline is ready
   protected abstract void onInitialized(DscEndpoint endpoint, SocketChannel channel);

   @Override
   protected final void initChannel(SocketChannel channel) throws Exception {
      LOGGER.fine("Initializing channel for connection number " + this.connectionId + " ...");

      final DscEndpoint endpoint = new DscEndpoint(channel);

      // Listener for channel closure: update endpoint state
      channel.closeFuture().addListener(new ChannelFutureListener() {
         public void operationComplete(ChannelFuture future) {
               endpoint.setState(DscEndpointState.CLOSED);
         }
      });

      // Initialize the session information associated with the channel
      SessionInfo.initInfo(channel, this.buildOwnSessionInfo());

      int logHandlerIndex = 0;
      ChannelPipeline pipeline = channel.pipeline();

      // Costruzione pipeline Netty
      pipeline
         .addLast(newLoggingHandler(logHandlerIndex++))
         .addLast(new ReadTimeoutHandler(Consts.IDLE_TIMEOUT))
         .addLast(new WriteTimeoutHandler(Consts.IDLE_TIMEOUT))
         .addLast(new IdleStateHandler(0, getWriterIdleTimeSeconds(), 0))
         .addLast(new FrameDecoder())
         .addLast(FRAME_ENCODER)
         .addLast(AES_DECODER)
         .addLast(AES_ENCODER)
         .addLast(newLoggingHandler(logHandlerIndex++))
         .addLast(DATA_DECODER)
         .addLast(DATA_ENCODER)
         .addLast(newLoggingHandler(logHandlerIndex++))
         .addLast(TRANSPORT_LAYER_DECODER)
         .addLast(TRANSPORT_LAYER_ENCODER)
         .addLast(MULTIPLE_PACKETS_DECODER)
         .addLast(newLoggingHandler(logHandlerIndex++))
         .addLast(COMMAND_DECODER)
         .addLast(COMMAND_ENCODER)
         .addLast(newLoggingHandler(logHandlerIndex++))
         .addLast(FLUSH_COMMAND_HANDLER)
         .addLast(ACK_HANDLER)
         .addLast(new ResponseHandler())
         .addLast(new CommandQueueHandler())
         .addLast(newLoggingHandler(logHandlerIndex++))
         .addLast(END_SESSION_HANDLER)
         .addLast(new PollHandler(this.pollFactory()))
         .addLast(new PipelineHandler(this.buildHandshakeHandlers(), this.buildNormalModeHandlers()))
         .addLast(newLoggingHandler(logHandlerIndex++))
         .addLast(FALLBACK_HANDLER)
         .addLast(new EndpointHandler(endpoint))
         .addLast(newLoggingHandler(logHandlerIndex++));

      // Hook for post-initialization customization
      this.onInitialized(endpoint, channel);
   }

   // Logging handler with an identifying name for debugging
   private LoggingHandler newLoggingHandler(int index) {
      return new LoggingHandler("DEBUG: .LoggingHandler_" + index + ".connection_" + this.connectionId, LogLevel.TRACE);
   }
}