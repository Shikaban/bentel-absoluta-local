package protocol.dsc.messages;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import io.netty.channel.ChannelHandlerContext;
import protocol.dsc.Message;
import protocol.dsc.NewValue;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.SectionRead;
import protocol.dsc.commands.SectionReadResponse;

import java.util.List;

abstract class SectionReading<V> extends Reading<Void, V, SectionReadResponse> {
   protected final int mainSection;
   protected final ImmutableList<Integer> subSections;

   protected SectionReading(int var1, ImmutableList<Integer> var2) {
      super(SectionReadResponse.class);
      this.mainSection = var1;
      this.subSections = (ImmutableList)Preconditions.checkNotNull(var2);
   }

   protected void parseResponse(ChannelHandlerContext var1, SectionReadResponse var2, List<Message.Response> var3) {
      if (null == var2.getModuleNumber() && this.mainSection == var2.getMainSectionNumber() && this.subSections.equals(var2.getSubSectionNumbers()) && null == var2.getIndex() && null == var2.getCount() && !var2.isVirtualSectionNumber()) {
         V var4 = this.getResponseValue(var2);
         var3.add(new NewValue(this, var4));
      }

   }

   protected abstract V getResponseValue(SectionReadResponse var1);

   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext var1, Void var2) throws Exception {
      SectionRead var3 = new SectionRead();
      var3.setMainSectionNumber(this.mainSection);
      var3.setSubSectionNumbers(this.subSections);
      return var3;
   }
}
