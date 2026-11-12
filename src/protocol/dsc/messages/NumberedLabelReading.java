package protocol.dsc.messages;

import com.google.common.base.Preconditions;

import io.netty.channel.ChannelHandlerContext;

import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.base.DscCharsets;
import protocol.dsc.commands.Configuration;

import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Logger;

public class NumberedLabelReading extends RequestableCommandReading<Integer, String, Configuration> {
   private static final Logger logger = Logger.getLogger(EventBufferReading.class.getName());
   private final int optionId;
   private final int offset;
   private final int max;
   private final Charset charset;

   public NumberedLabelReading(int optionId) {
      this(optionId, 0, DscCharsets.UNICODE);
   }

   public NumberedLabelReading(int optionId, int offset, Charset charset) {
      this(optionId, offset, Integer.MAX_VALUE, charset);
   }

   public NumberedLabelReading(int optionId, int offset, int max, Charset charset) {
      super(Configuration.class);
      this.optionId = optionId;
      this.offset = offset;
      this.max = max;
      this.charset = (Charset)Preconditions.checkNotNull(charset);
   }

   protected Configuration prepareRequest(ChannelHandlerContext context, Integer labelNumber) {
      Preconditions.checkArgument(labelNumber >= 1, "label number must be >= 1");
      Preconditions.checkArgument(labelNumber <= this.max, "label number must be <= " + this.max);
      Configuration configuration2 = new Configuration();
      configuration2.setOptionId(this.optionId);
      configuration2.setOptionIdOffsetFrom(labelNumber + this.offset);
      configuration2.setOptionIdOffsetTo(labelNumber + this.offset);
      return configuration2;
   }

   protected void parseResponse(ChannelHandlerContext context, Configuration configuration2, List<Message.Response> responses) {
      if (configuration2.getOptionId() == this.optionId) {
         Integer optionIdOffsetFrom = configuration2.getOptionIdOffsetFrom();
         List<String> strings = configuration2.getStrings(this.charset);
         if (optionIdOffsetFrom == null) {
            logger.warning("Unexpected null from for option id: " + this.optionId);
            return;
         }

         int labelNumber = optionIdOffsetFrom - this.offset;

         for (String text : strings) {
         if (1 <= labelNumber && labelNumber <= this.max) {
            responses.add(new NewValue(this, labelNumber, text));
         }
         ++labelNumber;
         }
      }

   }
}
