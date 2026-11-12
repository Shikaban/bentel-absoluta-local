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

   protected void parseResponse(ChannelHandlerContext context, AccessCodesResponse accessCodesResponse2, List<Message.Response> responses) {
      responses.add(new NewValue(this, Pair.with(accessCodesResponse2.getUserNumberStart(), accessCodesResponse2.getNumberOfUsers()), accessCodesResponse2.getCodes()));
   }

   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext context, Pair<Integer, Integer> pair2) throws Exception {
      AccessCodes accessCodes2 = new AccessCodes();
      accessCodes2.setUserNumberStart((Integer)pair2.getValue0());
      accessCodes2.setNumberOfUsers((Integer)pair2.getValue1());
      return accessCodes2;
   }
}
