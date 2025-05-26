package protocol.dsc.commands;

public class PartitionReadyStatusNotification extends AbstractPartitionReqCommand {
   public int getCommandNumber() {
      return 569;
   }
}
