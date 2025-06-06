package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscArray;
import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;

import java.util.ArrayList;
import java.util.List;

public class PartitionAssignmentsResponse extends DscCommandWithResponse.Response<PartitionAssignments> implements DscArray.ElementProvider<DscBitMask> {
   private final DscNumber bitMaskLength = DscNumber.newUnsignedNum(1);
   private final DscArray<DscBitMask> partitionAssignments = new DscArray<DscBitMask>(this);

   public PartitionAssignmentsResponse() {
      super(new PartitionAssignments());
   }

   protected List<DscSerializable> getResponseFields() {
      return ImmutableList.of(this.bitMaskLength, this.partitionAssignments);
   }

   public int getCommandNumber() {
      return 18232;
   }

   public int getUserNumberStart() {
      return ((PartitionAssignments)this.requestInstance).getUserNumberStart();
   }

   public int getNumberOfUsers() {
      return ((PartitionAssignments)this.requestInstance).getNumberOfUsers();
   }

   public List<List<Integer>> getPartitionAssignments() {
      List<List<Integer>> var1 = new ArrayList<List<Integer>>(this.partitionAssignments.size());
      for (DscBitMask bitMask : this.partitionAssignments) {
         var1.add(bitMask.getTrueIndexes());
      }

      return var1;
   }

   public int numberOfElements() {
      return this.getNumberOfUsers();
   }

   public DscBitMask newElement() {
      return new DscBitMask(this.bitMaskLength.toInt(), 1);
   }
}
