package protocol.dsc.base;

import com.google.common.collect.ImmutableMap;

import protocol.dsc.util.BCDCharset;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Map;

public final class DscCharsets {
   public static final int DF_HEXADECIMAL = 0;
   public static final int DF_BCD = 1;
   public static final int DF_ASCII = 2;
   public static final int DF_UNICODE = 3;
   public static final int DF_WIN1252 = 4;
   public static final Charset BCD = new BCDCharset();
   public static final Charset ASCII;
   public static final Charset UNICODE;
   public static final Charset WIN1252;
   private static final Map<Integer, Charset> FORMATS;

   public static Charset fromDataFormat(int var0) throws UnsupportedOperationException {
      Charset var1 = (Charset)FORMATS.get(var0);
      if (var1 == null) {
         throw new UnsupportedOperationException(String.format("format %d not supported", var0));
      } else {
         return var1;
      }
   }

   private DscCharsets() {
   }

   static {
      ASCII = StandardCharsets.US_ASCII;
      UNICODE = StandardCharsets.UTF_16BE;

      Charset var0;
      try {
         var0 = Charset.forName("Windows-1252");
      } catch (UnsupportedCharsetException var2) {
         System.out.println("WARN: Unsupported charset Windows-1252, using ISO_8859_1");
         var0 = StandardCharsets.ISO_8859_1;
      }

      WIN1252 = var0;
      FORMATS = ImmutableMap.<Integer, Charset>builder()
         .put(1, BCD)
         .put(2, ASCII)
         .put(3, UNICODE)
         .put(4, WIN1252)
         .build();
   }
}
