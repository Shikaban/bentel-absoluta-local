package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.PartitionStatus;

import java.util.Collections;
import java.util.List;

public class PartitionStatusesReading extends RequestableCommandReading<List<Integer>, List<List<Boolean>>, PartitionStatus> {
   public PartitionStatusesReading() {
      super(PartitionStatus.class);
   }

   protected PartitionStatus prepareRequest(ChannelHandlerContext var1, List<Integer> var2) {
      PartitionStatus var3 = new PartitionStatus();
      var3.setPartitions(var2);
      return var3;
   }

   protected void parseResponse(ChannelHandlerContext var1, PartitionStatus var2, List<Message.Response> var3) {
      List<Integer> var4 = var2.getPartitions();
      List<? extends List<Boolean>> var5 = var2.getStatuses();
      if (var4.size() != var5.size()) {
         throw new IllegalArgumentException("invalid partition status");
      } else {
         var3.add(new NewValue(this, var4, Collections.unmodifiableList(var5)));
      }
   }
}
