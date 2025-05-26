package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscArray;
import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PartitionStatus extends DscRequestableCommand implements DscArray.ElementProvider<DscBitMask> {
   private final DscBitMask partitions = new DscBitMask(true, 1);
   private final DscNumber bytesOfStatus = DscNumber.newUnsignedNum(1);
   private final DscArray<DscBitMask> statuses = new DscArray(this);

   protected List<DscSerializable> getRequestFields() {
      return ImmutableList.of(this.partitions);
   }

   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of(this.bytesOfStatus, this.statuses);
   }

   public int getCommandNumber() {
      return 2066;
   }

   public List<Integer> getPartitions() {
      return this.partitions.getTrueIndexes();
   }

   public void setPartition(int var1) {
      this.partitions.reset();
      this.partitions.setMinNumberOfBits(var1);
      this.partitions.set(var1, true);
   }

   public void setPartitions(List<Integer> var1) {
      this.partitions.reset();
      this.partitions.setMinNumberOfBits(var1.isEmpty() ? 0 : (Integer)Collections.max(var1));
      Iterator var2 = var1.iterator();

      while(var2.hasNext()) {
         Integer var3 = (Integer)var2.next();
         this.partitions.set(var3, true);
      }

   }

   public List<? extends List<Boolean>> getStatuses() {
      return Collections.unmodifiableList(this.statuses);
   }

   public int numberOfElements() {
      return this.getPartitions().size();
   }

   public DscBitMask newElement() {
      return new DscBitMask(this.bytesOfStatus.toInt(), 0);
   }
}
