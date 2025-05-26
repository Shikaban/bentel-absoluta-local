package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;

import java.util.List;

public class CommandResponse extends DscAbstractCommand implements DscGeneralResponse {
   public static final int SUCCESS = 0;
   public static final int INVALID_IDENTIFIER = 1;
   public static final int UNSUPPORTED_ENCRYPTION_TYPE = 3;
   public static final int INVALID_PARTITION = 20;
   public static final int NO_TROUBLES_PRESENT_FOR_REQUESTED_TYPE = 26;
   public static final int NO_REQUESTED_ALARMS_FOUND = 27;
   private final DscNumber commandSeq = DscNumber.newUnsignedNum(1);
   private final DscNumber responseCode = DscNumber.newUnsignedNum(1);

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.commandSeq, this.responseCode);
   }

   public int getCommandNumber() {
      return 1282;
   }

   public boolean isSuccess() {
      return this.getResponseCode() == 0;
   }

   public String getDescription() {
      return this.isSuccess() ? "successful response" : String.format("unsuccessful response with error code: 0x%02X", this.getResponseCode());
   }

   public int getCommandSeq() {
      return this.commandSeq.toInt();
   }

   public void setCommandSeq(int var1) {
      this.commandSeq.set((long)var1);
   }

   public int getResponseCode() {
      return this.responseCode.toInt();
   }

   public void setResponseCode(int var1) {
      this.responseCode.set((long)var1);
   }

   public String toString() {
      return String.format("%s [command seq: 0x%02X, response code: 0x%02X]", super.toString(), this.getCommandSeq(), this.getResponseCode());
   }
}
