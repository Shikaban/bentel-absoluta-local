package protocol.dsc.errors;

import io.netty.handler.codec.DecoderException;
import protocol.dsc.util.DscUtils;

public abstract class DscProtocolException extends DecoderException {
   private final int receivedCommand;

   public DscProtocolException(int numericValue, String text) {
      super(text);
      this.receivedCommand = DscUtils.validateUShort(numericValue);
   }

   public abstract int getErrorCode();

   public int getReceivedCommand() {
      return this.receivedCommand;
   }

   public String toString() {
      return String.format("%s [received command: 0x%04X, error code: 0x%02X, message: %s]", this.getClass().getSimpleName(), this.getReceivedCommand(), this.getErrorCode(), this.getMessage());
   }
}
