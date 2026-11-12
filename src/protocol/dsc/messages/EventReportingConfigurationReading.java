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

   protected void parseResponse(ChannelHandlerContext context, EventReportingConfigurationReadResponse eventReportingConfigurationReadResponse2, List<Message.Response> responses) {
      if (eventReportingConfigurationReadResponse2.getReportingType(0)) {
         responses.add(new NewValue(this, eventReportingConfigurationReadResponse2.getEventType(), eventReportingConfigurationReadResponse2.getSettings()));
      }

   }

   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext context, Integer eventType) throws Exception {
      EventReportingConfigurationRead eventReportingConfigurationRead2 = new EventReportingConfigurationRead();
      eventReportingConfigurationRead2.setReportingType(0, true);
      eventReportingConfigurationRead2.setEventType(eventType);
      return eventReportingConfigurationRead2;
   }
}
