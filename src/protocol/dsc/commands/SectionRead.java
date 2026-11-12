package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscArray;
import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscOptional;
import protocol.dsc.base.DscSerializable;

import java.util.ArrayList;
import java.util.List;

public class SectionRead extends DscCommandWithResponse {
   private static final int INDEX_BIT = 0;
   private static final int COUNT_BIT = 1;
   private static final int MODULE_NUMBER_BIT = 2;
   private static final int VIRTUAL_SECTION_NUMBER_BIT = 3;
   private final DscBitMask flags = new DscBitMask(COUNT_BIT, INDEX_BIT);
   private final DscOptional<DscNumber> moduleNumber = new DscOptional<DscNumber>(DscNumber.newUnsignedNum(COUNT_BIT), new SectionRead.FieldPresenceProvider(MODULE_NUMBER_BIT));
   private final DscNumber mainSectionNumber = DscNumber.newUnsignedNum(MODULE_NUMBER_BIT);
   private final DscArray<DscNumber> subSectionNumbers = new DscArray<DscNumber>(new SectionRead.SubSectionElementProvider());
   private final DscOptional<DscNumber> index = new DscOptional<DscNumber>(DscNumber.newUnsignedNum(COUNT_BIT), new SectionRead.FieldPresenceProvider(INDEX_BIT));
   private final DscOptional<DscNumber> count = new DscOptional<DscNumber>(DscNumber.newUnsignedNum(COUNT_BIT), new SectionRead.FieldPresenceProvider(COUNT_BIT));

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.flags, this.moduleNumber, this.mainSectionNumber, this.subSectionNumbers, this.index, this.count);
   }

   public int getCommandNumber() {
      return 1825;
   }

   public Integer getModuleNumber() {
      return this.moduleNumber.isPresent() ? ((DscNumber)this.moduleNumber.get()).toInt() : null;
   }

   public void setModuleNumber(Integer moduleNumber) {
      this.flags.set(MODULE_NUMBER_BIT, (Boolean)(moduleNumber != null));
      if (moduleNumber != null) {
         ((DscNumber)this.moduleNumber.get()).set((long)moduleNumber);
      }

   }

   public int getMainSectionNumber() {
      return this.mainSectionNumber.toInt();
   }

   public void setMainSectionNumber(int value) {
      this.mainSectionNumber.set((long)value);
   }

   public List<Integer> getSubSectionNumbers() {
      List<Integer> items = new ArrayList<>(this.subSectionNumbers.size());
      for (int i = 0; i < this.subSectionNumbers.size(); i++) {
         DscNumber value = (DscNumber) this.subSectionNumbers.get(i);
         items.add(value.toInt());
      }

      return items;
   }

   public void setSubSectionNumbers(List<Integer> items) {
      if (items.size() > 7) {
         throw new IllegalArgumentException("too many subsection numbers");
      } else {
         byte[] byteValue = this.flags.bytes();
         byteValue[0] = (byte)(byteValue[0] | items.size() << 4);
         this.subSectionNumbers.clear();
         for (Integer num : items) {
            ((DscNumber)this.subSectionNumbers.addNewElement()).set((long)num);
         }

         assert this.subSectionNumbers.getExpectedNumberOfElements() == this.subSectionNumbers.size();

      }
   }

   public Integer getIndex() {
      return this.index.isPresent() ? ((DscNumber)this.index.get()).toInt() : null;
   }

   public void setIndex(Integer index) {
      this.flags.set(INDEX_BIT, (Boolean)(index != null));
      if (index != null) {
         ((DscNumber)this.index.get()).set((long)index);
      }

   }

   public Integer getCount() {
      return this.count.isPresent() ? ((DscNumber)this.count.get()).toInt() : null;
   }

   public void setCount(Integer count) {
      this.flags.set(COUNT_BIT, (Boolean)(count != null));
      if (count != null) {
         ((DscNumber)this.count.get()).set((long)count);
      }

   }

   public boolean isVirtualSectionNumber() {
      return this.flags.get(VIRTUAL_SECTION_NUMBER_BIT);
   }

   public void setVirtualSectionNumber(boolean enabled) {
      this.flags.set(VIRTUAL_SECTION_NUMBER_BIT, (Boolean)enabled);
   }

   private class FieldPresenceProvider implements DscOptional.PresenceProvider {
      private final int flagsBit;

      FieldPresenceProvider(int flagsBit) {
         this.flagsBit = flagsBit;
      }

      public boolean isPresent() {
         return SectionRead.this.flags.get(this.flagsBit);
      }
   }

   private class SubSectionElementProvider implements DscArray.ElementProvider<DscNumber> {
      private SubSectionElementProvider() {
      }

      public int numberOfElements() {
         return (SectionRead.this.flags.bytes()[0] & 112) >> 4;
      }

      public DscNumber newElement() {
         return DscNumber.newUnsignedNum(MODULE_NUMBER_BIT);
      }

   }
}
