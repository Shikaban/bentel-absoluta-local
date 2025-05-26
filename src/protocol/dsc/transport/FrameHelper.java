package protocol.dsc.transport;

import com.google.common.collect.ImmutableBiMap;

import protocol.dsc.base.DscCharsets;

import java.nio.charset.Charset;
import java.util.Map;

final class FrameHelper {
   static final byte START_OF_FRAME = 126;
   static final byte END_OF_FRAME = 127;
   static final byte ESCAPE = 125;
   static final Map<Byte, Byte> ESCAPE_MAP = ImmutableBiMap.<Byte, Byte>builder()
      .put((byte)125, (byte)0)
      .put((byte)126, (byte)1)
      .put((byte)127, (byte)2)
      .build();
   static final Map<Byte, Byte> UNESCAPE_MAP;
   static final Charset MULTI_POINT_COMM_ID_CHARSET;

   private FrameHelper() {
   }

   static {
      UNESCAPE_MAP = ((ImmutableBiMap)ESCAPE_MAP).inverse();
      MULTI_POINT_COMM_ID_CHARSET = DscCharsets.ASCII;
   }
}
