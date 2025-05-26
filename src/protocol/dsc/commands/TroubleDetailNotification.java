package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscArray;
import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscStruct;
import protocol.dsc.base.DscVariableBytes;

import java.util.Collections;
import java.util.List;

public class TroubleDetailNotification extends DscAbstractCommand implements DscArray.ElementProvider<TroubleDetailNotification.Trouble> {
   private final DscArray<TroubleDetailNotification.Trouble> troubles = new DscArray(this);

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.troubles);
   }

   public int getCommandNumber() {
      return 2083;
   }

   public List<TroubleDetailNotification.Trouble> getTroubles() {
      return Collections.unmodifiableList(this.troubles);
   }

   public int numberOfElements() {
      return -1;
   }

   public TroubleDetailNotification.Trouble newElement() {
      return new TroubleDetailNotification.Trouble();
   }

   public static class Trouble extends DscStruct {
      private final DscVariableBytes deviceModuleType = new DscVariableBytes();
      private final DscVariableBytes troubleType = new DscVariableBytes();
      private final DscVariableBytes deviceModuleNumber = new DscVariableBytes();
      private final DscNumber status = DscNumber.newUnsignedNum(1);

      protected List<DscSerializable> getFields() {
         return ImmutableList.of(this.deviceModuleType, this.troubleType, this.deviceModuleNumber, this.status);
      }

      public int getDeviceModuleType() {
         return this.deviceModuleType.toPositiveInt();
      }

      public int getTroubleType() {
         return this.troubleType.toPositiveInt();
      }

      public int getDeviceModuleNumber() {
         return this.deviceModuleNumber.toPositiveInt();
      }

      public int getStatus() {
         return this.status.toInt();
      }
   }
}
