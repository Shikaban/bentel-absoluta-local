package protocol.dsc.commands;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscString;

import java.util.List;

public class CommandRequest extends DscCommandWithAppSeq {
   private DscRequestableCommand requestedCmd;
   private final DscString code = DscString.newBCDString();

   protected List<DscSerializable> getFields() {
      throw new UnsupportedOperationException("not implemented");
   }

   public int getCommandNumber() {
      return 2048;
   }

   public DscRequestableCommand getRequestedCmd() {
      return this.requestedCmd;
   }

   public void setRequestedCmd(DscRequestableCommand var1) {
      this.requestedCmd = var1;
   }

   public String getCode() {
      return this.code.toString();
   }

   public void setCode(String var1) {
      this.code.setString(var1);
   }

   protected boolean matchAsNotGeneralResponse(DscResponse var1) {
      if (this.requestedCmd != null && var1 instanceof DscRequestableCommand) {
         DscRequestableCommand var2 = (DscRequestableCommand)var1;
         return this.requestedCmd.match(var2);
      } else {
         return false;
      }
   }

   public void readCodeFrom(ByteBuf var1) {
      this.code.readFrom(var1);
   }

   public void readFrom(ByteBuf var1) throws DecoderException, IndexOutOfBoundsException {
      throw new UnsupportedOperationException("unable to read CommandRequest with this method");
   }

   public void writeTo(ByteBuf var1) {
      if (this.requestedCmd == null) {
         throw new IllegalStateException("requestedCmd not set");
      } else {
         var1.writeShort(this.requestedCmd.getCommandNumber());
         this.requestedCmd.writeRequestDataTo(var1);
         this.code.writeTo(var1);
      }
   }

   public String toString() {
      StringBuilder var1 = new StringBuilder(200);
      var1.append(super.toString()).append(" [req cmd");
      if (this.requestedCmd == null) {
         var1.append(" not set");
      } else {
         var1.append(": ").append(this.requestedCmd.getClass().getSimpleName()).append(String.format("/0x%04X", this.requestedCmd.getCommandNumber()));
      }

      var1.append(", code: '").append(this.code).append("']");
      return var1.toString();
   }
}
