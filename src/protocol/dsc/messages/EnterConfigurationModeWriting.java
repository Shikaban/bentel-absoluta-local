package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.EnterConfigurationMode;

import org.javatuples.Pair;

public class EnterConfigurationModeWriting<P> extends Writing<P> {
   private final int type;
   private final boolean readWrite;

   public static EnterConfigurationModeWriting<Pair<Integer, String>> withPartitionAndCodeParam(int var0, boolean var1) {
      return new EnterConfigurationModeWriting(var0, var1);
   }

   public static EnterConfigurationModeWriting<Integer> withPartitionParam(int var0, boolean var1) {
      return new EnterConfigurationModeWriting(var0, var1);
   }

   private EnterConfigurationModeWriting(int var1, boolean var2) {
      this.type = var1;
      this.readWrite = var2;
   }

   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext var1, P var2) throws Exception {
      int var3;
      String var4;
      if (var2 instanceof Pair) {
         Pair<?, ?> var5 = (Pair)var2;
         var3 = (Integer)var5.getValue0();
         var4 = (String)var5.getValue1();
      } else {
         if (!(var2 instanceof Integer)) {
            throw new IllegalArgumentException("unexpected param class");
         }

         var3 = (Integer)var2;
         var4 = Messages.getPin(var1);
      }

      EnterConfigurationMode var6 = new EnterConfigurationMode();
      var6.setPartitionNumber(var3);
      var6.setType(this.type);
      var6.setProgrammingAccessCode(var4);
      var6.setReadWrite(this.readWrite);
      return var6;
   }
}
