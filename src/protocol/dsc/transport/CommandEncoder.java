package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToByteEncoder;
import protocol.dsc.commands.DscCommand;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.LowACK;

import java.nio.ByteOrder;

@Sharable
public class CommandEncoder extends MessageToByteEncoder<DscCommand> {
   private static final boolean VERBOSE_DEBUG = false;

   protected void encode(ChannelHandlerContext ctx, DscCommand cmd, ByteBuf buffer) throws Exception {
      assert buffer.order() == ByteOrder.BIG_ENDIAN;

      if (cmd instanceof LowACK) {
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: low ACK encoded");
         }
      } else {
         buffer.writeShort(cmd.getCommandNumber());
         if (cmd instanceof DscCommandWithAppSeq) {
            int var4 = SequenceHandlersHelper.getCounters(ctx).nextAppSeq();
            ((DscCommandWithAppSeq)cmd).setAppSeq(var4);
            buffer.writeByte(var4);
         }

         cmd.writeTo(buffer);
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: command encoded: " + cmd);
         }
      }
   }
}
