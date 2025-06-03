package protocol.dsc.transport;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import protocol.dsc.DscError;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.DscGeneralResponse;
import protocol.dsc.commands.DscResponse;

import java.util.LinkedList;
import java.util.Queue;

public class ResponseHandler extends ChannelDuplexHandler {
   private static final int MAX_WAITING_CMDS = 32;
   private final Queue<DscCommandWithAppSeq> waitingCmds = new LinkedList<DscCommandWithAppSeq>();
   private static final boolean VERBOSE_DEBUG = false;

   public void channelInactive(ChannelHandlerContext var1) throws Exception {
      if (!this.waitingCmds.isEmpty()) {
         System.out.println("WARN: removing " + this.waitingCmds.size() + " waiting commands");
         this.waitingCmds.clear();
      }
      super.channelInactive(var1);
   }

   public void write(ChannelHandlerContext var1, Object var2, ChannelPromise var3) throws Exception {
      if (var2 instanceof DscCommandWithAppSeq) {
         DscCommandWithAppSeq var4 = (DscCommandWithAppSeq)var2;
         if (this.waitingCmds.size() == MAX_WAITING_CMDS) {
            DscCommandWithAppSeq var5 = (DscCommandWithAppSeq)this.waitingCmds.remove();
            System.out.println("WARN: waiting command removed to limit the queue size: " + var5);
         }
         this.waitingCmds.add(var4);
      }
      super.write(var1, var2, var3);
   }

   public void channelRead(ChannelHandlerContext var1, Object var2) throws Exception {
      boolean var3 = false;
      if (var2 instanceof DscResponse) {
         DscResponse var4 = (DscResponse)var2;
         for (DscCommandWithAppSeq var6 : waitingCmds) {
            if (var6.matchAsResponse(var4)) {
               if (VERBOSE_DEBUG) {
               System.out.println("DEBUG: response " + var4 + " received for " + var6);
               }
               waitingCmds.remove(var6);
               var3 = true;
               if (var4 instanceof DscGeneralResponse) {
               DscGeneralResponse var7 = (DscGeneralResponse) var4;
               var6.generalResponseReceived(var1.channel(), var7);
               }
               break;
            }
         }
      }

      if (var2 instanceof DscGeneralResponse) {
         if (!var3) {
            DscGeneralResponse var8 = (DscGeneralResponse)var2;
            if(VERBOSE_DEBUG) {
               System.out.println("DEBUG: unmatched general response " + var8);
            }
            if (!var8.isSuccess()) {
               var1.fireChannelRead(DscError.newGenericError(var8.getDescription()));
            }
         }
      } else {
         super.channelRead(var1, var2);
      }
   }
}
