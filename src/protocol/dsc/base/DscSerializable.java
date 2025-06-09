package protocol.dsc.base;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

public interface DscSerializable {
   void readFrom(ByteBuf buf) throws IndexOutOfBoundsException, DecoderException;

   void writeTo(ByteBuf buf);

   boolean isEquivalent(DscSerializable other);
}