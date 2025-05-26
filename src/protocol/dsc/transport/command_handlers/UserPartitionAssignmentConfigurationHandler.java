package protocol.dsc.transport.command_handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.UserPartitionAssignmentConfiguration;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.javatuples.Pair;

@Sharable
public class UserPartitionAssignmentConfigurationHandler extends ChannelInboundHandlerAdapter {
   public void channelRead(ChannelHandlerContext var1, Object var2) throws Exception {
      if (var2 instanceof UserPartitionAssignmentConfiguration) {
         UserPartitionAssignmentConfiguration var3 = (UserPartitionAssignmentConfiguration)var2;
         Iterator var4 = var3.getPartitionAssignments().entrySet().iterator();

         while(var4.hasNext()) {
            Entry<Integer, List<Integer>> var5 = (Entry)var4.next();
            Integer var6 = (Integer)var5.getKey();
            List<Integer> var7 = (List)var5.getValue();
            var1.fireChannelRead(new NewValue(Message.USER_PARTITION_ASSIGNMENT_CONFIGURATION, Pair.with(var6, var7)));
         }
      } else {
         super.channelRead(var1, var2);
      }

   }
}
