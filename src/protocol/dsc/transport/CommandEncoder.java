package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToByteEncoder;

import protocol.dsc.commands.DscCommand;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.LowACK;

import java.util.logging.Logger;

@Sharable
public class CommandEncoder extends MessageToByteEncoder<DscCommand> {
   private static final Logger logger = Logger.getLogger(CommandEncoder.class.getName());

   protected void encode(ChannelHandlerContext ctx, DscCommand cmd, ByteBuf buffer) throws Exception {

      if (cmd instanceof LowACK) {
         logger.finest("Low ACK encoded");
      } else {
         buffer.writeShort(cmd.getCommandNumber());
         if (cmd instanceof DscCommandWithAppSeq) {
            int counters = SequenceHandlersHelper.getCounters(ctx).nextAppSeq();
            ((DscCommandWithAppSeq)cmd).setAppSeq(counters);
            buffer.writeByte(counters);
         }

         cmd.writeTo(buffer);
         logger.finest("Command encoded: " + cmd);
      }
   }
}
