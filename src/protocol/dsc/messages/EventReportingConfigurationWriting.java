package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.EventReportingConfigurationWrite;

import java.util.List;
import org.javatuples.Pair;

public class EventReportingConfigurationWriting extends Writing<Pair<Integer, List<Boolean>>> {
   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext context, Pair<Integer, List<Boolean>> pair2) throws Exception {
      Integer eventType = (Integer)pair2.getValue0();
      List<Boolean> items = (List<Boolean>) pair2.getValue1();
      EventReportingConfigurationWrite eventReportingConfigurationWrite2 = new EventReportingConfigurationWrite();
      eventReportingConfigurationWrite2.setReportingType(0, true);
      eventReportingConfigurationWrite2.setEventType(eventType);
      eventReportingConfigurationWrite2.setSettings(items);
      return eventReportingConfigurationWrite2;
   }
}
