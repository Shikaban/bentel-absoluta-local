package protocol.dsc.messages;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.AccessCodes;
import protocol.dsc.commands.AccessCodesResponse;
import protocol.dsc.commands.DscCommandWithAppSeq;

import java.util.List;
import org.javatuples.Pair;

public class AccessCodesReading extends Reading<Pair<Integer, Integer>, List<String>, AccessCodesResponse> {
   public AccessCodesReading() {
      super(AccessCodesResponse.class);
   }

   protected void parseResponse(ChannelHandlerContext var1, AccessCodesResponse var2, List<Message.Response> var3) {
      var3.add(new NewValue(this, Pair.with(var2.getUserNumberStart(), var2.getNumberOfUsers()), var2.getCodes()));
   }

   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext var1, Pair<Integer, Integer> var2) throws Exception {
      AccessCodes var3 = new AccessCodes();
      var3.setUserNumberStart((Integer)var2.getValue0());
      var3.setNumberOfUsers((Integer)var2.getValue1());
      return var3;
   }
}
