package protocol.dsc.transport.command_handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandler.Sharable;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.UserPartitionAssignmentConfiguration;

import java.util.List;
import java.util.Map.Entry;
import org.javatuples.Pair;

@Sharable
public class UserPartitionAssignmentConfigurationHandler extends ChannelInboundHandlerAdapter {
   public void channelRead(ChannelHandlerContext context, Object object2) throws Exception {
      if (object2 instanceof UserPartitionAssignmentConfiguration) {
         UserPartitionAssignmentConfiguration userPartitionAssignmentConfiguration2 = (UserPartitionAssignmentConfiguration)object2;
         for (Entry<Integer, List<Integer>> entry : userPartitionAssignmentConfiguration2.getPartitionAssignments().entrySet()) {
            Integer key = entry.getKey();
            List<Integer> value = entry.getValue();
            context.fireChannelRead(new NewValue(Message.USER_PARTITION_ASSIGNMENT_CONFIGURATION, Pair.with(key, value)));
         }
      } else {
         super.channelRead(context, object2);
      }

   }
}
