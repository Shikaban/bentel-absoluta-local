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

   protected SectionReading(int mainSection, ImmutableList<Integer> immutableList2) {
      super(SectionReadResponse.class);
      this.mainSection = mainSection;
      this.subSections = Preconditions.checkNotNull(immutableList2);
   }

   protected void parseResponse(ChannelHandlerContext context, SectionReadResponse sectionReadResponse2, List<Message.Response> responses) {
      if (null == sectionReadResponse2.getModuleNumber() && this.mainSection == sectionReadResponse2.getMainSectionNumber() && this.subSections.equals(sectionReadResponse2.getSubSectionNumbers()) && null == sectionReadResponse2.getIndex() && null == sectionReadResponse2.getCount() && !sectionReadResponse2.isVirtualSectionNumber()) {
         V responseValue = this.getResponseValue(sectionReadResponse2);
         responses.add(new NewValue(this, responseValue));
      }

   }

   protected abstract V getResponseValue(SectionReadResponse sectionReadResponse2);

   protected DscCommandWithAppSeq prepareCommand(ChannelHandlerContext context, Void void2) throws Exception {
      SectionRead sectionRead2 = new SectionRead();
      sectionRead2.setMainSectionNumber(this.mainSection);
      sectionRead2.setSubSectionNumbers(this.subSections);
      return sectionRead2;
   }
}
