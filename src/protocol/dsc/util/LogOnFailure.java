package protocol.dsc.util;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import java.util.concurrent.CancellationException;

public class LogOnFailure implements ChannelFutureListener {
   public static final LogOnFailure INSTANCE = new LogOnFailure();

   public void operationComplete(ChannelFuture var1) throws Exception {
      Throwable var2 = var1.cause();
      if (var2 != null) {
         if (var2 instanceof CancellationException) {
            System.out.println("TRACE: operation cancelled");
         } else {
            System.out.println("TRACE: operation failed");
         }
      }
   }

   private LogOnFailure() {
   }
}
