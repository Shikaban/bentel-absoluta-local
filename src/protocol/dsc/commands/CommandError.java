package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.errors.DscProtocolException;

import java.util.List;

public class CommandError extends DscAbstractCommand implements DscGeneralResponse {
   private final DscNumber receivedCommand = DscNumber.newUnsignedNum(2);
   private final DscNumber errorCode = DscNumber.newUnsignedNum(1);

   public CommandError() {
   }

   public CommandError(DscProtocolException var1) {
      this.receivedCommand.set((long)var1.getReceivedCommand());
      this.errorCode.set((long)var1.getErrorCode());
   }

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.receivedCommand, this.errorCode);
   }

   public int getCommandNumber() {
      return 1281;
   }

   public boolean isSuccess() {
      return false;
   }

   public String getDescription() {
      return String.format("error code: 0x%02X", this.getErrorCode());
   }

   public int getReceivedCommand() {
      return this.receivedCommand.toInt();
   }

   public void setReceivedCommand(int var1) {
      this.receivedCommand.set((long)var1);
   }

   public int getErrorCode() {
      return this.errorCode.toInt();
   }

   public void setErrorCode(int var1) {
      this.errorCode.set((long)var1);
   }

   public String toString() {
      return String.format("%s [received command: 0x%04X, error code: 0x%02X]", super.toString(), this.getReceivedCommand(), this.getErrorCode());
   }
}
