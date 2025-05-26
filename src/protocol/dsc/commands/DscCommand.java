package protocol.dsc.commands;

import protocol.dsc.Priority;
import protocol.dsc.base.DscSerializable;

public interface DscCommand extends DscSerializable {
   int getCommandNumber();

   Priority getPriority();
}
