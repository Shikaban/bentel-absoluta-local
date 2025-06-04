package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscArray;
import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscStruct;
import protocol.dsc.base.DscVariableBytes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SystemTroubleStatus extends DscRequestableCommand implements DscArray.ElementProvider<SystemTroubleStatus.Trouble> {
   private static final int SYSTEM = 0;
   private static final int ALL_TROUBLES_NO_TROUBLES = 0;
   private final DscArray<SystemTroubleStatus.Trouble> troubles = new DscArray<SystemTroubleStatus.Trouble>(this);

   protected List<DscSerializable> getRequestFields() {
      return ImmutableList.of(this.troubles);
   }

   protected List<DscSerializable> getOtherFields() {
      return Collections.emptyList();
   }

   public int getCommandNumber() {
      return 2082;
   }

   public void setRequestAll() {
      SystemTroubleStatus.Trouble var1 = new SystemTroubleStatus.Trouble();
      var1.deviceModuleType.setPositiveInt(SYSTEM, 1);
      var1.numberOfTroubleTypes.set(1L);
      var1.troubleTypes.add((new DscVariableBytes()).setPositiveInt(ALL_TROUBLES_NO_TROUBLES, 1));
      this.troubles.clear();
      this.troubles.add(var1);
   }

   public List<SystemTroubleStatus.Trouble> getTroubles() {
      return Collections.unmodifiableList(this.troubles);
   }

   public boolean match(DscRequestableCommand var1) {
      return var1 instanceof SystemTroubleStatus;
   }

   public int numberOfElements() {
      return -1;
   }

   public SystemTroubleStatus.Trouble newElement() {
      return new SystemTroubleStatus.Trouble();
   }

   public static class Trouble extends DscStruct implements DscArray.ElementProvider<DscVariableBytes> {
      private final DscVariableBytes deviceModuleType = new DscVariableBytes();
      private final DscNumber numberOfTroubleTypes = DscNumber.newUnsignedNum(1);
      private final DscArray<DscVariableBytes> troubleTypes = new DscArray<DscVariableBytes>(this);

      protected List<DscSerializable> getFields() {
         return ImmutableList.of(this.deviceModuleType, this.numberOfTroubleTypes, this.troubleTypes);
      }

      public int getDeviceModuleType() {
         return this.deviceModuleType.toPositiveInt();
      }

      public List<Integer> getTroubleTypes() {
         List<Integer> var1 = new ArrayList<Integer>(this.troubleTypes.size());
         for (DscVariableBytes troubleType : this.troubleTypes) {
            int value = troubleType.toPositiveInt();
            if (value != ALL_TROUBLES_NO_TROUBLES) {
               var1.add(value);
            }
         }

         return var1;
      }

      public int numberOfElements() {
         return this.numberOfTroubleTypes.toInt();
      }

      public DscVariableBytes newElement() {
         return new DscVariableBytes();
      }
   }
}
