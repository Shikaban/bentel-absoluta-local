package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.EventReportingConfigurationWrite;

import java.util.List;
import org.javatuples.Pair;

public class EventReportingConfigurationWriting extends Writing<Pair<Integer, List<Boolean>>> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext var1, Pair<Integer, List<Boolean>> var2) throws Exception {
      Integer var3 = (Integer)var2.getValue0();
      List<Boolean> var4 = (List<Boolean>) var2.getValue1();
      EventReportingConfigurationWrite var5 = new EventReportingConfigurationWrite();
      var5.setReportingType(0, true);
      var5.setEventType(var3);
      var5.setSettings(var4);
      return var5;
   }
}
