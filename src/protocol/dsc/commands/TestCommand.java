package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscString;
import protocol.dsc.util.DscUtils;

import java.util.List;
import java.util.logging.Logger;

public class TestCommand extends DscCommandWithAppSeq {
   private static final Logger logger = Logger.getLogger(TestCommand.class.getName());
   private final int commandNumber;
   private final DscString payload = DscString.newBCDString();

   public TestCommand(int numericValue) {
      super(false);
      this.commandNumber = DscUtils.validateUShort(numericValue);
      logger.info("New test command 0x%04X; use it only for test " + numericValue);
   }

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.payload);
   }

   public int getCommandNumber() {
      return this.commandNumber;
   }

   public String getPayload() {
      return this.payload.toString();
   }

   public void setPayload(String text) {
      this.payload.setString(text);
   }
}
