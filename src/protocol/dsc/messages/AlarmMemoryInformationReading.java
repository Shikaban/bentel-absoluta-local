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

   protected AlarmMemoryInformation prepareRequest(ChannelHandlerContext var1, Integer var2) throws Exception {
      AlarmMemoryInformation var3 = new AlarmMemoryInformation();
      var3.setPartitionNumber(var2);
      return var3;
   }

   protected void parseResponse(ChannelHandlerContext var1, AlarmMemoryInformation var2, List<Message.Response> var3) {
      Triplet<Boolean, Boolean, List<Integer>> var4 = new Triplet(var2.getFireAlarm(), var2.getCOAlarm(), var2.getZoneAlarms());
      var3.add(new NewValue(this, var2.getPartitionNumber(), var4));
   }
}
