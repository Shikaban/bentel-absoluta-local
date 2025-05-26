package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscString;

import java.util.List;

public class SectionReadResponse extends DscCommandWithResponse.Response<SectionRead> {
   private final DscString data = DscString.newBCDString();

   public SectionReadResponse() {
      super(new SectionRead());
   }

   protected List<DscSerializable> getResponseFields() {
      return ImmutableList.of(this.data);
   }

   public int getCommandNumber() {
      return 18209;
   }

   public Integer getModuleNumber() {
      return ((SectionRead)this.requestInstance).getModuleNumber();
   }

   public int getMainSectionNumber() {
      return ((SectionRead)this.requestInstance).getMainSectionNumber();
   }

   public List<Integer> getSubSectionNumbers() {
      return ((SectionRead)this.requestInstance).getSubSectionNumbers();
   }

   public Integer getIndex() {
      return ((SectionRead)this.requestInstance).getIndex();
   }

   public Integer getCount() {
      return ((SectionRead)this.requestInstance).getCount();
   }

   public boolean isVirtualSectionNumber() {
      return ((SectionRead)this.requestInstance).isVirtualSectionNumber();
   }

   public String getDataAsBCDString() {
      return this.data.toString();
   }

   public DscBitMask getDataAsBitMask(int var1) {
      return new DscBitMask(this.data.bytes(), var1, true);
   }
}
