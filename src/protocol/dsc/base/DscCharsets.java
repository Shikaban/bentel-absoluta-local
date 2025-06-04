package protocol.dsc.base;

import com.google.common.collect.ImmutableMap;
import protocol.dsc.util.BCDCharset;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

public final class DscCharsets {
   public static final int FORMAT_HEXADECIMAL = 0;
   public static final int FORMAT_BCD = 1;
   public static final int FORMAT_ASCII = 2;
   public static final int FORMAT_UNICODE = 3;
   public static final int FORMAT_WIN1252 = 4;

   public static final Charset BCD = new BCDCharset();
   public static final Charset ASCII;
   public static final Charset UNICODE;
   public static final Charset WIN1252;

   private static final Map<Integer, Charset> FORMAT_TO_CHARSET_MAP;

   public static Charset fromDataFormat(int format) throws UnsupportedOperationException {
      Charset charset = FORMAT_TO_CHARSET_MAP.get(format);
      if (charset == null) {
         throw new UnsupportedOperationException(String.format("format %d not supported", format));
      } else {
         return charset;
      }
   }

   private DscCharsets() {
   }

   static {
      ASCII = StandardCharsets.US_ASCII;
      UNICODE = StandardCharsets.UTF_16BE;

      Charset win1252Charset;
      try {
         win1252Charset = Charset.forName("Windows-1252");
      } catch (UnsupportedCharsetException ex) {
         System.out.println("WARN: Unsupported charset Windows-1252, using ISO_8859_1");
         win1252Charset = StandardCharsets.ISO_8859_1;
      }

      WIN1252 = win1252Charset;
      FORMAT_TO_CHARSET_MAP = ImmutableMap.<Integer, Charset>builder()
         .put(FORMAT_BCD, BCD)
         .put(FORMAT_ASCII, ASCII)
         .put(FORMAT_UNICODE, UNICODE)
         .put(FORMAT_WIN1252, WIN1252)
         .build();
   }
}
