package protocol.dsc.messages;

import com.google.common.collect.ImmutableList;

import protocol.dsc.commands.SectionReadResponse;

public class MasterCodeReading extends SectionReading<String> {
   public MasterCodeReading() {
      super(6, ImmutableList.of(2));
   }

   protected String getResponseValue(SectionReadResponse var1) {
      return var1.getDataAsBCDString();
   }
}
