package protocol.dsc.util;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.concurrent.CancellationException;
import java.util.logging.Logger;

public class LogOnFailure implements ChannelFutureListener {
   private static final Logger logger = Logger.getLogger(LogOnFailure.class.getName());
   public static final LogOnFailure INSTANCE = new LogOnFailure();

   public void operationComplete(ChannelFuture channelFuture2) throws Exception {
      Throwable cause2 = channelFuture2.cause();
      if (cause2 != null) {
         if (cause2 instanceof CancellationException) {
            logger.finer("Operation cancelled");
         } else {
            logger.finer("Operation failed");
         }
      }
   }

   private LogOnFailure() {
   }
}