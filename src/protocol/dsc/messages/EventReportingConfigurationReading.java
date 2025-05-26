package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.EventReportingConfigurationRead;
import protocol.dsc.commands.EventReportingConfigurationReadResponse;

import java.util.List;

public class EventReportingConfigurationReading extends Reading<Integer, List<Boolean>, EventReportingConfigurationReadResponse> {
   public EventReportingConfigurationReading() {
      super(EventReportingConfigurationReadResponse.class);
   }

   protected void parseResponse(ChannelHandlerContext var1, EventReportingConfigurationReadResponse var2, List<Message.Response> var3) {
      if (var2.getReportingType(0)) {
         var3.add(new NewValue(this, var2.getEventType(), var2.getSettings()));
      }

   }

   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext var1, Integer var2) throws Exception {
      EventReportingConfigurationRead var3 = new EventReportingConfigurationRead();
      var3.setReportingType(0, true);
      var3.setEventType(var2);
      return var3;
   }
}
