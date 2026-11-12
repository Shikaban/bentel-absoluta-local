package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscVariableBytes;

import java.util.List;

public class LifeStyleZoneStatus extends DscAbstractCommand {
   private final DscVariableBytes zoneNumber = new DscVariableBytes();

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.zoneNumber);
   }

   public int getCommandNumber() {
      return 528;
   }

   public final int getZoneNumber() {
      return this.zoneNumber.toPositiveInt();
   }

   public final void setZoneNumber(int value) {
      this.zoneNumber.setPositiveInt(value);
   }
}
