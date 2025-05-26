package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscBitMask;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscVariableBytes;

import java.util.List;

public class SingleZoneBypassStatus extends DscRequestableCommand {
   private final DscVariableBytes zoneNumber = new DscVariableBytes();
   private final DscBitMask bypassed = new DscBitMask(1, 0);

   protected List<DscSerializable> getRequestFields() {
      return ImmutableList.of(this.zoneNumber);
   }

   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of(this.bypassed);
   }

   public int getCommandNumber() {
      return 2080;
   }

   public int getZoneNumber() {
      return this.zoneNumber.toPositiveInt();
   }

   public void setZoneNumber(int var1) {
      this.zoneNumber.setPositiveInt(var1);
   }

   public boolean isBypassed() {
      return this.bypassed.get(0);
   }

   public void setBypassed(boolean var1) {
      this.bypassed.set(0, (Boolean)var1);
   }
}
