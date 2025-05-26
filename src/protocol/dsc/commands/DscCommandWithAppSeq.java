package protocol.dsc.commands;

import io.netty.channel.Channel;
import protocol.dsc.util.DscUtils;

public abstract class DscCommandWithAppSeq extends DscAbstractCommand {
   private int appSeq = -1;
   private DscCommandWithAppSeq.ResponseCallback responseCallback;

   protected DscCommandWithAppSeq() {
   }

   protected DscCommandWithAppSeq(boolean var1) {
      super(var1);
   }

   public final int getAppSeq() {
      return this.appSeq;
   }

   public final void setAppSeq(int var1) {
      this.appSeq = DscUtils.validateUByte(var1);
   }

   public final void setResponseCallback(DscCommandWithAppSeq.ResponseCallback var1) {
      this.responseCallback = var1;
   }

   public final boolean hasResponseCallback() {
      return this.responseCallback != null;
   }

   public final boolean matchAsResponse(DscResponse var1) {
      if (var1 instanceof CommandResponse) {
         return this.getAppSeq() == ((CommandResponse)var1).getCommandSeq();
      } else if (var1 instanceof CommandError) {
         return this.getCommandNumber() == ((CommandError)var1).getReceivedCommand();
      } else {
         return this.matchAsNotGeneralResponse(var1);
      }
   }

   protected boolean matchAsNotGeneralResponse(DscResponse var1) {
      return false;
   }

   public final void generalResponseReceived(Channel var1, DscGeneralResponse var2) {
      if (this.responseCallback != null) {
         this.responseCallback.generalResponseReceived(var1, var2);
      }

   }

   public String toString() {
      return this.appSeq == -1 ? String.format("%s [app seq not set]", super.toString()) : String.format("%s [app seq: 0x%02X]", super.toString(), this.appSeq);
   }

   public interface ResponseCallback {
      void generalResponseReceived(Channel var1, DscGeneralResponse var2);
   }
}
