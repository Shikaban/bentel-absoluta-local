package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscVariableBytes;

import java.util.List;

public class PartitionAssignments extends DscCommandWithResponse {
   private final DscVariableBytes userNumberStart = new DscVariableBytes();
   private final DscVariableBytes numberOfUsers = new DscVariableBytes();

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.userNumberStart, this.numberOfUsers);
   }

   public int getCommandNumber() {
      return 1848;
   }

   public int getUserNumberStart() {
      return this.userNumberStart.toPositiveInt();
   }

   public void setUserNumberStart(int value) {
      this.userNumberStart.setPositiveInt(value);
   }

   public int getNumberOfUsers() {
      return this.numberOfUsers.toPositiveInt();
   }

   public void setNumberOfUsers(int value) {
      this.numberOfUsers.setPositiveInt(value);
   }
}
