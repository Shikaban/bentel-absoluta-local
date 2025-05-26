package protocol.dsc.transport;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.TooLongFrameException;
import protocol.dsc.session.SessionInfo;

import java.nio.ByteOrder;
import java.util.List;

public class FrameDecoder extends ByteToMessageDecoder {
   private static final int MAX_FRAME_LENGTH = 65535;
   private ByteBuf outBuf = null;
   private boolean inFrame = false;
   private boolean discarding = false;

   protected void decode(ChannelHandlerContext var1, ByteBuf var2, List<Object> var3) throws DecoderException {
      try {
         byte var4;
         while(this.discarding && var2.isReadable()) {
            var4 = var2.readByte();
            switch(var4) {
            case 126:
               this.inFrame = true;
               this.discarding = false;
               break;
            case 127:
               this.inFrame = false;
               this.discarding = false;
            }
         }

         if (var2.isReadable()) {
            assert !this.discarding;

            if (this.outBuf == null) {
               this.outBuf = var1.alloc().buffer().order(ByteOrder.BIG_ENDIAN);

               assert this.outBuf.maxCapacity() > MAX_FRAME_LENGTH;
            }

            while(var2.isReadable()) {
               var4 = var2.readByte();
               switch(var4) {
               case 125:
                  if (this.inFrame) {
                     if (!var2.isReadable()) {
                        var2.readerIndex(var2.readerIndex() - 1);
                        return;
                     }

                     var4 = var2.readByte();
                     Byte var5 = (Byte)FrameHelper.UNESCAPE_MAP.get(var4);
                     if (var5 == null) {
                        var2.readerIndex(var2.readerIndex() - 1);
                        this.discarding = true;
                        throw new CorruptedFrameException(String.format("unexpected escape sequence: %02X %02X", 125, var4));
                     }

                     var4 = var5;
                  }
               default:
                  if (this.outBuf.readableBytes() >= MAX_FRAME_LENGTH) {
                     this.discarding = true;
                     throw new TooLongFrameException(String.format("frame length exceeds %d", MAX_FRAME_LENGTH));
                  }

                  this.outBuf.writeByte(var4);
                  break;
               case 126:
                  try {
                     if (this.inFrame) {
                        throw new CorruptedFrameException("unexpected start of frame");
                     }

                     this.setID(var1);

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

                     var3.add(this.outBuf);
                     this.outBuf = null;
                  } finally {
                     this.inFrame = false;
                  }

                  return;
               }
            }

         }
      } catch (DecoderException var15) {
         this.releaseOutBuf();
         throw var15;
      }
   }

   private void setID(ChannelHandlerContext var1) throws CorruptedFrameException {
      assert this.outBuf != null;

      try {
         String var2 = this.outBuf.toString(FrameHelper.MULTI_POINT_COMM_ID_CHARSET);
         SessionInfo var3 = SessionInfo.getPeerInfo(var1.channel());
         String var4 = var3.getMultiPointCommId();
         if (var4 == null) {
            var3.setMultiPointCommId(var2);
         } else if (!var4.equals(var2)) {
            throw new CorruptedFrameException(String.format("unexpected multi-point communication id '%s' (it was '%s')", var2, var4));
         }
      } finally {
         this.releaseOutBuf();
      }

   }

   protected void decodeLast(ChannelHandlerContext var1, ByteBuf var2, List<Object> var3) throws DecoderException {
      try {
         this.decode(var1, var2, var3);
         if (this.inFrame) {
            throw new CorruptedFrameException("no end of frame");
         }
      } finally {
         this.reset();
      }

   }

   protected void handlerRemoved0(ChannelHandlerContext var1) {
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
