package protocol.dsc.commands;

import java.util.Collections;
import java.util.List;

import protocol.dsc.base.DscSerializable;

public class EncapsulatedCommandForMultiplePackets extends DscAbstractCommand {
   public static final int COMMAND_NUMBER = 1571;

   protected List<DscSerializable> getFields() {
      return Collections.emptyList();
   }

   public int getCommandNumber() {
      return 1571;
   }
}
