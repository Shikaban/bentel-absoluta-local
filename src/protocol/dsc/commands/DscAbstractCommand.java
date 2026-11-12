package protocol.dsc.commands;

import protocol.dsc.Priority;
import protocol.dsc.base.DscStruct;
import protocol.dsc.transport.CommandDecoder;

public abstract class DscAbstractCommand extends DscStruct implements DscCommand {
   private Priority priority;

   protected DscAbstractCommand() {
      this(true);
   }

   protected DscAbstractCommand(boolean enabled) {
      this.priority = Priority.NORMAL;
      if (enabled) {
         assert CommandDecoder.knows(this.getClass()) : "command can not be decoded because it is unknown to CommandDecoder";
      } else {
         assert !CommandDecoder.knows(this.getClass()) : "command can be decoded, but this is unexpected";
      }

   }

   public final Priority getPriority() {
      return this.priority;
   }

   public final void setPriority(Priority priority2) {
      this.priority = priority2;
   }

   public String toString() {
      return String.format("%s [command number: 0x%04X]", this.getClass().getSimpleName(), this.getCommandNumber());
   }
}
