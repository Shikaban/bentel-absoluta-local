package protocol.dsc.messages;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import io.netty.channel.ChannelHandlerContext;

import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.base.DscCharsets;
import protocol.dsc.commands.Configuration;

import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Logger;

public class SingleLabelReading extends RequestableCommandReading<Void, String, Configuration> {
   private static final Logger logger = Logger.getLogger(SingleLabelReading.class.getName());

   private final int optionId;
   private final Integer number;
   private final Charset charset;

   public SingleLabelReading(int numericValue, Integer integerValue) {
      this(numericValue, integerValue, DscCharsets.UNICODE);
   }

   public SingleLabelReading(int optionId, Integer number, Charset charset2) {
      super(Configuration.class);
      this.optionId = optionId;
      this.number = number;
      this.charset = (Charset)Preconditions.checkNotNull(charset2);
   }

   protected Configuration prepareRequest(ChannelHandlerContext context, Void void2) {
      Configuration configuration2 = new Configuration();
      configuration2.setOptionId(this.optionId);
      if (this.number != null) {
         configuration2.setOptionIdOffsetFrom(this.number);
         configuration2.setOptionIdOffsetTo(this.number);
      } else {
         configuration2.setOptionIdOffsetFrom((Integer)null);
      }

      return configuration2;
   }

   protected void parseResponse(ChannelHandlerContext context, Configuration configuration2, List<Message.Response> responses) {
      if (configuration2.getOptionId() == this.optionId) {
         Integer optionIdOffsetFrom = configuration2.getOptionIdOffsetFrom();
         List<String> strings = configuration2.getStrings(this.charset);
         if (this.number != null && optionIdOffsetFrom == null) {
            logger.warning("Unexpected null from for option id: " + this.optionId);
            return;
         }

         if (Objects.equal(this.number, optionIdOffsetFrom) || this.number == null && optionIdOffsetFrom == 0) {
            responses.add(new NewValue(this, strings.get(0)));
         }
      }
   }
}
