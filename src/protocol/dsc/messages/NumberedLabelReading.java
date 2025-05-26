package protocol.dsc.messages;

import com.google.common.base.Preconditions;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.base.DscCharsets;
import protocol.dsc.commands.Configuration;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

public class NumberedLabelReading extends RequestableCommandReading<Integer, String, Configuration> {
   private final int optionId;
   private final int offset;
   private final int max;
   private final Charset charset;

   public NumberedLabelReading(int var1) {
      this(var1, 0, DscCharsets.UNICODE);
   }

   public NumberedLabelReading(int var1, int var2, Charset var3) {
      this(var1, var2, Integer.MAX_VALUE, var3);
   }

   public NumberedLabelReading(int var1, int var2, int var3, Charset var4) {
      super(Configuration.class);
      this.optionId = var1;
      this.offset = var2;
      this.max = var3;
      this.charset = (Charset)Preconditions.checkNotNull(var4);
   }

   protected Configuration prepareRequest(ChannelHandlerContext var1, Integer var2) {
      Preconditions.checkArgument(var2 >= 1, "label number must be >= 1");
      Preconditions.checkArgument(var2 <= this.max, "label number must be <= " + this.max);
      Configuration var3 = new Configuration();
      var3.setOptionId(this.optionId);
      var3.setOptionIdOffsetFrom(var2 + this.offset);
      var3.setOptionIdOffsetTo(var2 + this.offset);
      return var3;
   }

   protected void parseResponse(ChannelHandlerContext var1, Configuration var2, List<Message.Response> var3) {
      if (var2.getOptionId() == this.optionId) {
         Integer var4 = var2.getOptionIdOffsetFrom();
         List<String> var5 = var2.getStrings(this.charset);
         if (var4 == null) {
            System.out.println("WARN: unexpected null from for option id: " + this.optionId);
            return;
         }

         int var6 = var4 - this.offset;

         for(Iterator var7 = var5.iterator(); var7.hasNext(); ++var6) {
            String var8 = (String)var7.next();
            if (1 <= var6 && var6 <= this.max) {
               var3.add(new NewValue(this, var6, var8));
            }
         }
      }

   }
}
