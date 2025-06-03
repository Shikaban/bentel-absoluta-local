package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.AlarmMemoryInformation;

import java.util.List;
import org.javatuples.Triplet;

public class AlarmMemoryInformationReading extends RequestableCommandReading<Integer, Triplet<Boolean, Boolean, List<Integer>>, AlarmMemoryInformation> {
   public AlarmMemoryInformationReading() {
      super(AlarmMemoryInformation.class);
   }

   @Override
   protected AlarmMemoryInformation prepareRequest(ChannelHandlerContext ctx, Integer partitionNumber) throws Exception {
      AlarmMemoryInformation request = new AlarmMemoryInformation();
      request.setPartitionNumber(partitionNumber);
      return request;
   }

   @Override
   protected void parseResponse(ChannelHandlerContext ctx, AlarmMemoryInformation response, List<Message.Response> responses) {
      Triplet<Boolean, Boolean, List<Integer>> alarmInfo = Triplet.with(
               response.getFireAlarm(),
               response.getCOAlarm(),
               response.getZoneAlarms()
      );
      responses.add(new NewValue(this, response.getPartitionNumber(), alarmInfo));
   }
}
