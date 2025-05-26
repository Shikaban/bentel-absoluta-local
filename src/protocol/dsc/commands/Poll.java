package protocol.dsc.commands;

import java.util.Collections;
import java.util.List;

import protocol.dsc.base.DscSerializable;

public class Poll extends DscAbstractCommand {
   protected List<DscSerializable> getFields() {
      return Collections.emptyList();
   }

   public int getCommandNumber() {
      return 1536;
   }
}
