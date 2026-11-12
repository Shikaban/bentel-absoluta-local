package protocol.dsc.commands;

import io.netty.channel.Channel;
import protocol.dsc.util.DscUtils;

public abstract class DscCommandWithAppSeq extends DscAbstractCommand {
   private int appSeq = -1;
   private DscCommandWithAppSeq.ResponseCallback responseCallback;

   protected DscCommandWithAppSeq() {
   }

   protected DscCommandWithAppSeq(boolean enabled) {
      super(enabled);
   }

   public final int getAppSeq() {
      return this.appSeq;
   }

   public final void setAppSeq(int value) {
      this.appSeq = DscUtils.validateUByte(value);
   }

   public final void setResponseCallback(DscCommandWithAppSeq.ResponseCallback responseCallback2) {
      this.responseCallback = responseCallback2;
   }

   public final boolean hasResponseCallback() {
      return this.responseCallback != null;
   }

   public final boolean matchAsResponse(DscResponse response) {
      if (response instanceof CommandResponse) {
         return this.getAppSeq() == ((CommandResponse)response).getCommandSeq();
      } else if (response instanceof CommandError) {
         return this.getCommandNumber() == ((CommandError)response).getReceivedCommand();
      } else {
         return this.matchAsNotGeneralResponse(response);
      }
   }

   protected boolean matchAsNotGeneralResponse(DscResponse response) {
      return false;
   }

   public final void generalResponseReceived(Channel channel2, DscGeneralResponse generalResponse) {
      if (this.responseCallback != null) {
         this.responseCallback.generalResponseReceived(channel2, generalResponse);
      }

   }

   public String toString() {
      return this.appSeq == -1 ? String.format("%s [app seq not set]", super.toString()) : String.format("%s [app seq: 0x%02X]", super.toString(), this.appSeq);
   }

   public interface ResponseCallback {
      void generalResponseReceived(Channel channel2, DscGeneralResponse generalResponse);
   }
}
