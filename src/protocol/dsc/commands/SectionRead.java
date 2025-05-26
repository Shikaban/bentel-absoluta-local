package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscArray;
import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscOptional;
import protocol.dsc.base.DscSerializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SectionRead extends DscCommandWithResponse {
   private static final int INDEX_BIT = 0;
   private static final int COUNT_BIT = 1;
   private static final int MODULE_NUMBER_BIT = 2;
   private static final int VIRTUAL_SECTION_NUMBER_BIT = 3;
   private final DscBitMask flags = new DscBitMask(COUNT_BIT, INDEX_BIT);
   private final DscOptional<DscNumber> moduleNumber = new DscOptional(DscNumber.newUnsignedNum(COUNT_BIT), new SectionRead.FieldPresenceProvider(MODULE_NUMBER_BIT));
   private final DscNumber mainSectionNumber = DscNumber.newUnsignedNum(MODULE_NUMBER_BIT);
   private final DscArray<DscNumber> subSectionNumbers = new DscArray(new SectionRead.SubSectionElementProvider());
   private final DscOptional<DscNumber> index = new DscOptional(DscNumber.newUnsignedNum(COUNT_BIT), new SectionRead.FieldPresenceProvider(INDEX_BIT));
   private final DscOptional<DscNumber> count = new DscOptional(DscNumber.newUnsignedNum(COUNT_BIT), new SectionRead.FieldPresenceProvider(COUNT_BIT));

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.flags, this.moduleNumber, this.mainSectionNumber, this.subSectionNumbers, this.index, this.count);
   }

   public int getCommandNumber() {
      return 1825;
   }

   public Integer getModuleNumber() {
      return this.moduleNumber.isPresent() ? ((DscNumber)this.moduleNumber.get()).toInt() : null;
   }

   public void setModuleNumber(Integer var1) {
      this.flags.set(MODULE_NUMBER_BIT, (Boolean)(var1 != null));
      if (var1 != null) {
         ((DscNumber)this.moduleNumber.get()).set((long)var1);
      }

   }

   public int getMainSectionNumber() {
      return this.mainSectionNumber.toInt();
   }

   public void setMainSectionNumber(int var1) {
      this.mainSectionNumber.set((long)var1);
   }

   public List<Integer> getSubSectionNumbers() {
      List<Integer> var1 = new ArrayList(this.subSectionNumbers.size());
      Iterator var2 = this.subSectionNumbers.iterator();

      while(var2.hasNext()) {
         DscNumber var3 = (DscNumber)var2.next();
         var1.add(var3.toInt());
      }

      return var1;
   }

   public void setSubSectionNumbers(List<Integer> var1) {
      if (var1.size() > 7) {
         throw new IllegalArgumentException("too many subsection numbers");
      } else {
         byte[] var10000 = this.flags.bytes();
         var10000[0] = (byte)(var10000[0] | var1.size() << 4);
         this.subSectionNumbers.clear();
         Iterator var2 = var1.iterator();

         while(var2.hasNext()) {
            Integer var3 = (Integer)var2.next();
            ((DscNumber)this.subSectionNumbers.addNewElement()).set((long)var3);
         }

         assert this.subSectionNumbers.getExpectedNumberOfElements() == this.subSectionNumbers.size();

      }
   }

   public Integer getIndex() {
      return this.index.isPresent() ? ((DscNumber)this.index.get()).toInt() : null;
   }

   public void setIndex(Integer var1) {
      this.flags.set(INDEX_BIT, (Boolean)(var1 != null));
      if (var1 != null) {
         ((DscNumber)this.index.get()).set((long)var1);
      }

   }

   public Integer getCount() {
      return this.count.isPresent() ? ((DscNumber)this.count.get()).toInt() : null;
   }

   public void setCount(Integer var1) {
      this.flags.set(COUNT_BIT, (Boolean)(var1 != null));
      if (var1 != null) {
         ((DscNumber)this.count.get()).set((long)var1);
      }

   }

   public boolean isVirtualSectionNumber() {
      return this.flags.get(VIRTUAL_SECTION_NUMBER_BIT);
   }

   public void setVirtualSectionNumber(boolean var1) {
      this.flags.set(VIRTUAL_SECTION_NUMBER_BIT, (Boolean)var1);
   }

   private class FieldPresenceProvider implements DscOptional.PresenceProvider {
      private final int flagsBit;

      FieldPresenceProvider(int var2) {
         this.flagsBit = var2;
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
