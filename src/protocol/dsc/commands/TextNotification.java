package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscCharsets;
import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscVariableBytes;

import java.util.List;

public class TextNotification extends DscAbstractCommand {
   private final DscNumber format = new DscNumber(1, false);
   private final DscVariableBytes message = new DscVariableBytes();

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.format, this.message);
   }

   public int getCommandNumber() {
      return 513;
   }

   public void setMessage(int var1, String var2) throws UnsupportedOperationException {
      this.format.set((long)var1);
      this.message.setString(DscCharsets.fromDataFormat(var1), var2);
   }

   public String getMessage() throws UnsupportedOperationException {
      return this.message.toString(DscCharsets.fromDataFormat(this.format.toInt()));
   }

   public int getFormat() {
      return this.format.toInt();
   }
}
