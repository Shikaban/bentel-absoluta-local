package protocol.dsc.base;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

public interface DscSerializable {
   void readFrom(ByteBuf var1) throws IndexOutOfBoundsException, DecoderException;

   void writeTo(ByteBuf var1);

   boolean isEquivalent(DscSerializable var1);
}
