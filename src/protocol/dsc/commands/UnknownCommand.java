package protocol.dsc.commands;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import protocol.dsc.Priority;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.errors.DscProtocolException;
import protocol.dsc.util.DscUtils;

import java.util.Arrays;

public class UnknownCommand implements DscCommand {
   private final int commandNumber;
   private byte[] payload;

   public UnknownCommand(int var1) {
      this.commandNumber = DscUtils.validateUShort(var1);
   }

   public int getCommandNumber() {
      return this.commandNumber;
   }

   public Priority getPriority() {
      throw new UnsupportedOperationException("not supported");
   }

   public void readFrom(ByteBuf var1) throws DscProtocolException, DecoderException, IndexOutOfBoundsException {
      this.payload = new byte[var1.readableBytes()];
      var1.readBytes(this.payload);
   }

   public void writeTo(ByteBuf var1) {
      throw new UnsupportedOperationException("unknown command can not be written");
   }

   public boolean isEquivalent(DscSerializable var1) {
      if (!(var1 instanceof UnknownCommand)) {
         return false;
      } else {
         UnknownCommand var2 = (UnknownCommand)var1;
         return this.commandNumber == var2.commandNumber && Arrays.equals(this.payload, var2.payload);
      }
   }

   public String toString() {
      return String.format("UnknownCommand [command number: 0x%04X, payload: %s]", this.commandNumber, DscUtils.hexDump(this.payload));
   }
}
