package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscArray;
import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscVariableBytes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UserPartitionAssignmentConfiguration extends DscAbstractCommand implements DscArray.ElementProvider<DscBitMask> {
   private final DscVariableBytes userNumberStart = new DscVariableBytes();
   private final DscVariableBytes numberOfUsers = new DscVariableBytes();
   private final DscNumber numberOfBytes = DscNumber.newUnsignedNum(1);
   private final DscArray<DscBitMask> partitionAssignments = new DscArray(this);

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.userNumberStart, this.numberOfUsers, this.numberOfBytes, this.partitionAssignments);
   }

   public int getCommandNumber() {
      return 1909;
   }

   public Map<Integer, List<Integer>> getPartitionAssignments() {
      Map<Integer, List<Integer>> var1 = new HashMap();
      int var2 = this.userNumberStart.toPositiveInt();
      Iterator var3 = this.partitionAssignments.iterator();

      while(var3.hasNext()) {
         DscBitMask var4 = (DscBitMask)var3.next();
         var1.put(var2++, var4.getTrueIndexes());
      }

      return var1;
   }

   public int numberOfElements() {
      return this.numberOfUsers.toPositiveInt();
   }

   public DscBitMask newElement() {
      return new DscBitMask(this.numberOfBytes.toInt(), 1);
   }
}
