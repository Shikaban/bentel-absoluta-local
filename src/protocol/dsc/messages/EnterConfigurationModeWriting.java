package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.EnterConfigurationMode;
import org.javatuples.Pair;

public class EnterConfigurationModeWriting<P> extends Writing<P> {
   private final int type;
   private final boolean readWrite;

   public static EnterConfigurationModeWriting<Pair<Integer, String>> withPartitionAndCodeParam(int type, boolean readWrite) {
      return new EnterConfigurationModeWriting<>(type, readWrite);
   }

   public static EnterConfigurationModeWriting<Integer> withPartitionParam(int type, boolean readWrite) {
      return new EnterConfigurationModeWriting<>(type, readWrite);
   }

   private EnterConfigurationModeWriting(int type, boolean readWrite) {
      this.type = type;
      this.readWrite = readWrite;
   }

   @Override
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext ctx, P param) throws Exception {
      int partitionNumber;
      String programmingAccessCode;

      if (param instanceof Pair<?, ?> pair) {
         partitionNumber = (Integer) pair.getValue0();
         programmingAccessCode = (String) pair.getValue1();
      } else if (param instanceof Integer) {
         partitionNumber = (Integer) param;
         programmingAccessCode = Messages.getPin(ctx);
      } else {
         throw new IllegalArgumentException("Unexpected parameter class: " + param.getClass());
      }

      EnterConfigurationMode command = new EnterConfigurationMode();
      command.setPartitionNumber(partitionNumber);
      command.setType(this.type);
      command.setProgrammingAccessCode(programmingAccessCode);
      command.setReadWrite(this.readWrite);
      return command;
   }
}
