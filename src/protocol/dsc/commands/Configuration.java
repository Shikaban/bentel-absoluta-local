package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscArray;
import protocol.dsc.base.DscBinary;
import protocol.dsc.base.DscOptional;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscVariableBytes;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Configuration extends DscRequestableCommand {
   public static final int ZONE_LABEL = 209;
   public static final int PARTITION_LABEL = 211;
   public static final int USER_LABEL = 217;
   public static final int KEYPAD_LABEL = 226;
   public static final int ZONE_EXPANDER_MODULE_LABEL = 227;
   public static final int POWER_SUPPLY_MODULE_LABEL = 228;
   public static final int HIGH_CURRENT_OUTPUT_MODULE_LABEL = 229;
   public static final int OUTPUT_EXPANDER_MODULE_LABEL = 230;
   public static final int WIRELESS_TRANSCEIVER_LABEL = 231;
   public static final int ALTERNATE_COMMUNICATOR_MODULE_LABEL = 232;
   public static final int WIRELESS_SIREN_LABEL = 233;
   public static final int WIRELESS_REPEATER_LABEL = 234;
   public static final int AUDIO_VERIFICATION_MODULE_LABEL = 235;
   public static final int ABSOLUTA_ZONE_LABEL = 1;
   public static final int ABSOLUTA_PARTITION_LABEL = 3;
   public static final int ABSOLUTA_COMMAND_LABELS = 4;
   public static final int ABSOLUTA_ARMING_MODE_LABEL = 13;
   private final Configuration.Helper helper = new Configuration.Helper();
   private final DscVariableBytes optionId = new DscVariableBytes();
   private final DscVariableBytes optionIdOffsetFrom = new DscVariableBytes();
   private final DscOptional<DscVariableBytes> optionIdOffsetTo;
   private final DscVariableBytes dataLength;
   private final DscArray<DscBinary> data;

   public Configuration() {
      this.optionIdOffsetTo = new DscOptional(new DscVariableBytes(), this.helper);
      this.dataLength = new DscVariableBytes();
      this.data = new DscArray(this.helper);
   }

   protected List<DscSerializable> getRequestFields() {
      return ImmutableList.of(this.optionId, this.optionIdOffsetFrom, this.optionIdOffsetTo);
   }

   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of(this.dataLength, this.data);
   }

   public int getCommandNumber() {
      return 1905;
   }

   public boolean match(DscRequestableCommand var1) {
      if (var1 instanceof Configuration) {
         Configuration var2 = (Configuration)var1;
         if (this.getOptionId() != var2.getOptionId()) {
            return false;
         } else {
            Integer var3 = this.getOptionIdOffsetFrom();
            return var3 == null ? true : var3.equals(var2.getOptionIdOffsetFrom());
         }
      } else {
         return false;
      }
   }

   public void setOptionId(int var1) {
      this.optionId.setPositiveInt(var1);
   }

   public int getOptionId() {
      return this.optionId.toPositiveInt();
   }

   public void setOptionIdOffsetFrom(Integer var1) {
      this.optionIdOffsetFrom.setPositiveInteger(var1);
   }

   public Integer getOptionIdOffsetFrom() {
      return this.get(this.optionIdOffsetFrom);
   }

   public void setOptionIdOffsetTo(Integer var1) {
      ((DscVariableBytes)this.optionIdOffsetTo.getAnyway()).setPositiveInteger(var1);
   }

   public Integer getOptionIdOffsetTo() {
      return this.get((DscVariableBytes)this.optionIdOffsetTo.get());
   }

   public List<String> getStrings(Charset var1) {
      List<String> var2 = new ArrayList(this.data.size());
      for (DscBinary binary : this.data) {
         var2.add(binary.toString(var1));
      }

      return var2;
   }

   private Integer get(DscVariableBytes var1) {
      return var1 == null ? null : var1.toPositiveInteger();
   }

   private class Helper implements DscOptional.PresenceProvider, DscArray.ElementProvider<DscBinary> {
      private Helper() {
      }

      public boolean isPresent() {
         return Configuration.this.optionIdOffsetFrom.length() > 0;
      }

      public int numberOfElements() {
         DscVariableBytes var1 = (DscVariableBytes)Configuration.this.optionIdOffsetTo.get();
         return var1 != null && Configuration.this.optionIdOffsetFrom.length() != 0 ? var1.toPositiveInt() - Configuration.this.optionIdOffsetFrom.toPositiveInt() + 1 : 1;
      }

      public DscBinary newElement() {
         return new DscBinary(Configuration.this.dataLength.toPositiveInt());
      }

   }
}
