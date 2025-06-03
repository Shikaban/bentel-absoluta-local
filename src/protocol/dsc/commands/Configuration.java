package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;
import protocol.dsc.base.DscArray;
import protocol.dsc.base.DscBinary;
import protocol.dsc.base.DscOptional;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscVariableBytes;

import java.nio.charset.Charset;
import java.util.ArrayList;
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

   private final Helper helper = new Helper();
   private final DscVariableBytes optionId = new DscVariableBytes();
   private final DscVariableBytes optionIdOffsetFrom = new DscVariableBytes();
   private final DscOptional<DscVariableBytes> optionIdOffsetTo;
   private final DscVariableBytes dataLength;
   private final DscArray<DscBinary> data;

   public Configuration() {
      this.optionIdOffsetTo = new DscOptional<>(new DscVariableBytes(), this.helper);
      this.dataLength = new DscVariableBytes();
      this.data = new DscArray<>(this.helper);
   }

   @Override
   protected List<DscSerializable> getRequestFields() {
      return ImmutableList.of(this.optionId, this.optionIdOffsetFrom, this.optionIdOffsetTo);
   }

   @Override
   protected List<DscSerializable> getOtherFields() {
      return ImmutableList.of(this.dataLength, this.data);
   }

   @Override
   public int getCommandNumber() {
      return 1905;
   }

   @Override
   public boolean match(DscRequestableCommand other) {
      if (other instanceof Configuration) {
         Configuration conf = (Configuration) other;
         if (this.getOptionId() != conf.getOptionId()) {
               return false;
         }
         Integer from = this.getOptionIdOffsetFrom();
         return from == null || from.equals(conf.getOptionIdOffsetFrom());
      }
      return false;
   }

   public void setOptionId(int optionId) {
      this.optionId.setPositiveInt(optionId);
   }

   public int getOptionId() {
      return this.optionId.toPositiveInt();
   }

   public void setOptionIdOffsetFrom(Integer offsetFrom) {
      this.optionIdOffsetFrom.setPositiveInteger(offsetFrom);
   }

   public Integer getOptionIdOffsetFrom() {
      return getInteger(this.optionIdOffsetFrom);
   }

   public void setOptionIdOffsetTo(Integer offsetTo) {
      ((DscVariableBytes) this.optionIdOffsetTo.getAnyway()).setPositiveInteger(offsetTo);
   }

   public Integer getOptionIdOffsetTo() {
      return getInteger((DscVariableBytes) this.optionIdOffsetTo.get());
   }

   public List<String> getStrings(Charset charset) {
      List<String> result = new ArrayList<>(this.data.size());
      for (DscBinary binary : this.data) {
         result.add(binary.toString(charset));
      }
      return result;
   }

   private Integer getInteger(DscVariableBytes bytes) {
      return bytes == null ? null : bytes.toPositiveInteger();
   }

   private class Helper implements DscOptional.PresenceProvider, DscArray.ElementProvider<DscBinary> {
      @Override
      public boolean isPresent() {
         return Configuration.this.optionIdOffsetFrom.length() > 0;
      }

      @Override
      public int numberOfElements() {
         DscVariableBytes to = (DscVariableBytes) Configuration.this.optionIdOffsetTo.get();
         if (to != null && Configuration.this.optionIdOffsetFrom.length() != 0) {
               return to.toPositiveInt() - Configuration.this.optionIdOffsetFrom.toPositiveInt() + 1;
         }
         return 1;
      }

      @Override
      public DscBinary newElement() {
         return new DscBinary(Configuration.this.dataLength.toPositiveInt());
      }
   }
}