package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.PartitionStatus;

import java.util.Collections;
import java.util.List;

public class PartitionStatusReading extends RequestableCommandReading<Integer, List<Boolean>, PartitionStatus> {
   public PartitionStatusReading() {
      super(PartitionStatus.class);
   }

   protected PartitionStatus prepareRequest(ChannelHandlerContext var1, Integer var2) {
      PartitionStatus var3 = new PartitionStatus();
      var3.setPartition(var2);
      return var3;
   }

   protected void parseResponse(ChannelHandlerContext var1, PartitionStatus var2, List<Message.Response> var3) {
      List<Integer> var4 = var2.getPartitions();
      List<? extends List<Boolean>> var5 = var2.getStatuses();
      if (var4.size() != var5.size()) {
         throw new IllegalArgumentException("invalid partition status");
      } else {
         for(int var6 = 0; var6 < var4.size(); ++var6) {
            var3.add(new NewValue(this, var4.get(var6), Collections.unmodifiableList((List)var5.get(var6))));
         }

      }
   }
}
