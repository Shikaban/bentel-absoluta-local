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

   public UnknownCommand(int numericValue) {
      this.commandNumber = DscUtils.validateUShort(numericValue);
   }

   public int getCommandNumber() {
      return this.commandNumber;
   }

   public Priority getPriority() {
      throw new UnsupportedOperationException("not supported");
   }

   public void readFrom(ByteBuf inputBuffer) throws DscProtocolException, DecoderException, IndexOutOfBoundsException {
      this.payload = new byte[inputBuffer.readableBytes()];
      inputBuffer.readBytes(this.payload);
   }

   public void writeTo(ByteBuf buffer) {
      throw new UnsupportedOperationException("unknown command can not be written");
   }

   public boolean isEquivalent(DscSerializable serializable) {
      if (!(serializable instanceof UnknownCommand)) {
         return false;
      } else {
         UnknownCommand unknownCommand2 = (UnknownCommand)serializable;
         return this.commandNumber == unknownCommand2.commandNumber && Arrays.equals(this.payload, unknownCommand2.payload);
      }
   }

   public String toString() {
      return String.format("UnknownCommand [command number: 0x%04X, payload: %s]", this.commandNumber, DscUtils.hexDump(this.payload));
   }
}
