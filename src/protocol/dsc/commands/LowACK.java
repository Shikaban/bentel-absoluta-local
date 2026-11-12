package protocol.dsc.commands;

import io.netty.buffer.ByteBuf;
import protocol.dsc.Priority;
import protocol.dsc.base.DscSerializable;

public final class LowACK implements DscCommand {
   private LowACK() {
   }

   public int getCommandNumber() {
      throw new UnsupportedOperationException("ACK do not have a command number");
   }

   public Priority getPriority() {
      return null;
   }

   public String toString() {
      return "low ACK";
   }

   public static LowACK getInstance() {
      return LowACK.LowACKHolder.INSTANCE;
   }

   public void readFrom(ByteBuf inputBuffer) {
   }

   public void writeTo(ByteBuf buffer) {
   }

   public boolean isEquivalent(DscSerializable serializable) {
      return serializable instanceof LowACK;
   }

   // $FF: synthetic method
   LowACK(Object object2) {
      this();
   }

   private static class LowACKHolder {
      private static final LowACK INSTANCE = new LowACK();
   }
}
