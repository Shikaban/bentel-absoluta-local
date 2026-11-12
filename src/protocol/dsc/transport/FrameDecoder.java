package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.TooLongFrameException;

import protocol.dsc.session.SessionInfo;

import java.util.List;

public class FrameDecoder extends ByteToMessageDecoder {
   private static final int MAX_FRAME_LENGTH = 65535;
   private ByteBuf outBuf = null;
   private boolean inFrame = false;
   private boolean discarding = false;

   protected void decode(ChannelHandlerContext context, ByteBuf inputBuffer, List<Object> output) throws DecoderException {
      try {
         byte byteValue;
         while(this.discarding && inputBuffer.isReadable()) {
            byteValue = inputBuffer.readByte();
            switch(byteValue) {
            case 126:
               this.inFrame = true;
               this.discarding = false;
               break;
            case 127:
               this.inFrame = false;
               this.discarding = false;
            }
         }

         if (inputBuffer.isReadable()) {
            assert !this.discarding;

            if (this.outBuf == null) {
               this.outBuf = context.alloc().buffer();

               assert this.outBuf.maxCapacity() > MAX_FRAME_LENGTH;
            }

            while(inputBuffer.isReadable()) {
               byteValue = inputBuffer.readByte();
               switch(byteValue) {
               case 125:
                  if (this.inFrame) {
                     if (!inputBuffer.isReadable()) {
                        inputBuffer.readerIndex(inputBuffer.readerIndex() - 1);
                        return;
                     }

                     byteValue = inputBuffer.readByte();
                     Byte unescapedByte = (Byte)FrameHelper.UNESCAPE_MAP.get(byteValue);
                     if (unescapedByte == null) {
                        inputBuffer.readerIndex(inputBuffer.readerIndex() - 1);
                        this.discarding = true;
                        throw new CorruptedFrameException(String.format("unexpected escape sequence: %02X %02X", 125, byteValue));
                     }

                     byteValue = unescapedByte;
                  }
               default:
                  if (this.outBuf.readableBytes() >= MAX_FRAME_LENGTH) {
                     this.discarding = true;
                     throw new TooLongFrameException(String.format("frame length exceeds %d", MAX_FRAME_LENGTH));
                  }

                  this.outBuf.writeByte(byteValue);
                  break;
               case 126:
                  try {
                     if (this.inFrame) {
                        throw new CorruptedFrameException("unexpected start of frame");
                     }

                     this.setID(context);

                     assert this.outBuf == null;
                  } finally {
                     this.inFrame = true;
                  }

                  return;
               case 127:
                  try {
                     if (!this.inFrame) {
                        throw new CorruptedFrameException("unexpected end of frame");
                     }

                     output.add(this.outBuf);
                     this.outBuf = null;
                  } finally {
                     this.inFrame = false;
                  }

                  return;
               }
            }

         }
      } catch (DecoderException exception) {
         this.releaseOutBuf();
         throw exception;
      }
   }

   private void setID(ChannelHandlerContext context) throws CorruptedFrameException {
      assert this.outBuf != null;

      try {
         String communicationId = this.outBuf.toString(FrameHelper.MULTI_POINT_COMM_ID_CHARSET);
         SessionInfo peerInfo = SessionInfo.getPeerInfo(context.channel());
         String multiPointCommId = peerInfo.getMultiPointCommId();
         if (multiPointCommId == null) {
            peerInfo.setMultiPointCommId(communicationId);
         } else if (!multiPointCommId.equals(communicationId)) {
            throw new CorruptedFrameException(String.format("unexpected multi-point communication id '%s' (it was '%s')", communicationId, multiPointCommId));
         }
      } finally {
         this.releaseOutBuf();
      }

   }

   protected void decodeLast(ChannelHandlerContext context, ByteBuf inputBuffer, List<Object> output) throws DecoderException {
      try {
         this.decode(context, inputBuffer, output);
         if (this.inFrame) {
            throw new CorruptedFrameException("no end of frame");
         }
      } finally {
         this.reset();
      }

   }

   protected void handlerRemoved0(ChannelHandlerContext context) {
      this.reset();
   }

   private void releaseOutBuf() {
      if (this.outBuf != null) {
         this.outBuf.release();
         this.outBuf = null;
      }

   }

   private void reset() {
      this.inFrame = false;
      this.discarding = false;
      this.releaseOutBuf();
   }
}
