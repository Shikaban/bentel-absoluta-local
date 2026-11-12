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

   public void setRequestedCmd(DscRequestableCommand requestableCommand) {
      this.requestedCmd = requestableCommand;
   }

   public String getCode() {
      return this.code.toString();
   }

   public void setCode(String text) {
      this.code.setString(text);
   }

   protected boolean matchAsNotGeneralResponse(DscResponse response) {
      if (this.requestedCmd != null && response instanceof DscRequestableCommand) {
         DscRequestableCommand requestableCommand = (DscRequestableCommand)response;
         return this.requestedCmd.match(requestableCommand);
      } else {
         return false;
      }
   }

   public void readCodeFrom(ByteBuf inputBuffer) {
      this.code.readFrom(inputBuffer);
   }

   public void readFrom(ByteBuf inputBuffer) throws DecoderException, IndexOutOfBoundsException {
      throw new UnsupportedOperationException("unable to read CommandRequest with this method");
   }

   public void writeTo(ByteBuf buffer) {
      if (this.requestedCmd == null) {
         throw new IllegalStateException("requestedCmd not set");
      } else {
         buffer.writeShort(this.requestedCmd.getCommandNumber());
         this.requestedCmd.writeRequestDataTo(buffer);
         this.code.writeTo(buffer);
      }
   }

   public String toString() {
      StringBuilder textBuilder = new StringBuilder(200);
      textBuilder.append(super.toString()).append(" [req cmd");
      if (this.requestedCmd == null) {
         textBuilder.append(" not set");
      } else {
         textBuilder.append(": ").append(this.requestedCmd.getClass().getSimpleName()).append(String.format("/0x%04X", this.requestedCmd.getCommandNumber()));
      }

      textBuilder.append(", code: '").append(this.code).append("']");
      return textBuilder.toString();
   }
}
