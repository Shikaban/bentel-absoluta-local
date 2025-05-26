package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscVariableBytes;

import java.util.List;

public class SystemCapabilities extends DscRequestableCommand {
   private final DscVariableBytes maxZones = new DscVariableBytes();
   private final DscVariableBytes maxUsers = new DscVariableBytes();
   private final DscVariableBytes maxPartitions = new DscVariableBytes();
   private final DscVariableBytes maxFOBs = new DscVariableBytes();
   private final DscVariableBytes maxProxTags = new DscVariableBytes();
   private final DscVariableBytes maxOutputs = new DscVariableBytes();

   protected List<DscSerializable> getRequestFields() {
      return ImmutableList.of();
   }

   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of(this.maxZones, this.maxUsers, this.maxPartitions, this.maxFOBs, this.maxProxTags, this.maxOutputs);
   }

   public int getCommandNumber() {
      return 1555;
   }

   public int getMaxZones() {
      return this.maxZones.toPositiveInt();
   }

   public int getMaxUsers() {
      return this.maxUsers.toPositiveInt();
   }

   public int getMaxPartitions() {
      return this.maxPartitions.toPositiveInt();
   }

   public int getMaxFOBs() {
      return this.maxFOBs.toPositiveInt();
   }

   public int getMaxProxTags() {
      return this.maxProxTags.toPositiveInt();
   }

   public int getMaxOutputs() {
      return this.maxOutputs.toPositiveInt();
   }
}
