package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.AttributeKey;

import protocol.dsc.commands.EncapsulatedCommandForMultiplePackets;
import protocol.dsc.errors.WrongCommandLengthException;

import java.util.List;

@Sharable
public class MultiplePacketsDecoder extends MessageToMessageDecoder<ByteBuf> {
   private static final AttributeKey<AssemblyState> ASSEMBLY_STATE =
         AttributeKey.valueOf("MultiplePacketsDecoder.assemblyState");

   protected void decode(ChannelHandlerContext context, ByteBuf inputBuffer, List<Object> output) throws Exception {
      if (inputBuffer.readableBytes() < 2) {
         output.add(inputBuffer.retain());
         return;
      }

      int commandNumber = inputBuffer.getUnsignedShort(inputBuffer.readerIndex());
      if (commandNumber != EncapsulatedCommandForMultiplePackets.COMMAND_NUMBER) {
         output.add(inputBuffer.retain());
         return;
      }

      if (inputBuffer.readableBytes() < 6) {
         throw new WrongCommandLengthException(
               EncapsulatedCommandForMultiplePackets.COMMAND_NUMBER,
               "unexpected encapsulated packet length");
      }

      int totalPackets = inputBuffer.getUnsignedByte(inputBuffer.readerIndex() + 2);
      int packetsToFollow = inputBuffer.getUnsignedByte(inputBuffer.readerIndex() + 3);

      AssemblyState state = context.channel().attr(ASSEMBLY_STATE).get();
      if (packetsToFollow == totalPackets - 1) {
         if (state != null) {
            state.buffer.release();
         }
         state = new AssemblyState(context.alloc().buffer(), packetsToFollow);
         context.channel().attr(ASSEMBLY_STATE).set(state);
      } else if (state == null || packetsToFollow != state.expectedPacketsToFollow - 1) {
         throw new WrongCommandLengthException(
               EncapsulatedCommandForMultiplePackets.COMMAND_NUMBER,
               "encapsulated packet received out of sequence");
      }

      inputBuffer.skipBytes(4);
      state.buffer.writeBytes(inputBuffer);
      state.expectedPacketsToFollow = packetsToFollow;

      if (packetsToFollow == 0) {
         context.channel().attr(ASSEMBLY_STATE).set(null);
         output.add(state.buffer);
      }
   }

   private static final class AssemblyState {
      private final ByteBuf buffer;
      private int expectedPacketsToFollow;

      private AssemblyState(ByteBuf buffer, int expectedPacketsToFollow) {
         this.buffer = buffer;
         this.expectedPacketsToFollow = expectedPacketsToFollow;
      }
   }
}
