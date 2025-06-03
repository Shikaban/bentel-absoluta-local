package protocol.dsc;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Septet;
import org.javatuples.Triplet;

import protocol.dsc.base.DscCharsets;
import protocol.dsc.messages.*;

public class Message<P, V> {
   // Definizioni statiche di messaggi comuni
   public static final Message<Void, String> TEXT_NOTIFICATION = new Message<>();
   public static final Message<Void, List<Quartet<Integer, Integer, Integer, Integer>>> TROUBLE_DETAIL_NOTIFICATION = new Message<>();
   public static final Message<Void, Integer> PARTITION_STATUS_CHANGED = new Message<>();
   public static final Message<Void, Integer> PARTITION_ALARM_MEMORY_CHANGED = new Message<>();
   public static final Message<Void, Integer> ZONE_STATUS_CHANGED = new Message<>();
   public static final Message<Void, Quartet<Integer, Integer, Integer, Integer>> ACCESS_LEVEL_LEAD_IN_OUT = new Message<>();
   public static final Message<Void, Pair<Integer, List<Integer>>> USER_PARTITION_ASSIGNMENT_CONFIGURATION = new Message<>();
   public static final Message<Void, Integer> MAX_USERS = new MaxUsersReading();
   public static final Message<Void, Integer> CODE_LENGTH = new CodeLengthReading();
   public static final Message<Void, String> MASTER_CODE = new MasterCodeReading();
   public static final Message<Pair<Integer, Integer>, List<String>> ACCESS_CODES = new AccessCodesReading();
   public static final Message<Integer, List<Integer>> PARTITION_ASSIGNMENT = new PartitionAssignmentReading();
   public static final Message<Void, List<Integer>> PARTITION_ASSIGNMENT_CONFIGURATION = new PartitionAssignmentConfigurationReading();
   public static final Message<Integer, List<Integer>> PARTITION_ZONES = new ZoneAssignmentReading();
   public static final Message<Integer, List<Boolean>> PARTITION_STATUS = new PartitionStatusReading();
   public static final Message<List<Integer>, List<List<Boolean>>> PARTITION_STATUSES = new PartitionStatusesReading();
   public static final Message<Integer, Triplet<Boolean, Boolean, List<Integer>>> PARTITION_ALARM_MEMORY = new AlarmMemoryInformationReading();
   public static final Message<Integer, List<Triplet<Integer, Integer, Boolean>>> PARTITION_ZONE_ALARM_STATUS = new PartitionZoneAlarmStatusReading();
   public static final Message<Integer, List<Boolean>> ZONE_STATUS = new ZoneStatusReading();
   public static final Message<Pair<Integer, Integer>, List<List<Boolean>>> ZONE_STATUSES = new ZoneStatusesReading();
   public static final Message<Integer, Boolean> SINGLE_ZONE_BYPASS_STATUS = new SingleZoneBypassStatusReading();
   public static final Message<Void, List<Integer>> BYPASSED_ZONES = new ZoneBypassStatusReading();
   public static final Message<Void, List<Pair<Integer, Integer>>> SYSTEM_TROUBLE_STATUS = new SystemTroubleStatusReading();
   public static final Message<Pair<Integer, Integer>, List<Integer>> TROUBLE_DETAIL = new TroubleDetailReading();
   public static final Message<Integer, String> PARTITION_LABEL = new NumberedLabelReading(211);
   public static final Message<Integer, String> ZONE_LABEL = new NumberedLabelReading(209);
   public static final Message<Integer, String> KEYPAD_LABEL = new NumberedLabelReading(226);
   public static final Message<Integer, String> WIRELESS_SIREN_LABEL = new NumberedLabelReading(233);
   public static final Message<Integer, String> USER_LABEL = new NumberedLabelReading(217);
   public static final Message<Integer, String> WIRELESS_REPEATER_LABEL = new NumberedLabelReading(234);
   public static final Message<Integer, String> ZONE_EXPANDER_MODULE_LABEL = new NumberedLabelReading(227);
   public static final Message<Integer, String> POWER_SUPPLY_MODULE_LABEL = new NumberedLabelReading(228);
   public static final Message<Integer, String> HIGH_CURRENT_OUTPUT_MODULE_LABEL = new NumberedLabelReading(229);
   public static final Message<Integer, String> OUTPUT_EXPANDER_MODULE_LABEL = new NumberedLabelReading(230);
   public static final Message<Void, String> SYSTEM_LABEL = new SingleLabelReading(211, 0);

   public static final Message<Integer, String> ABSOLUTA_PARTITION_LABEL;
   public static final Message<Integer, String> ABSOLUTA_OUTPUT_LABEL;
   public static final Message<Integer, String> ABSOLUTA_REMOTE_COMMAND_LABEL;
   public static final Message<Integer, String> ABSOLUTA_ZONE_LABEL;
   public static final Message<Integer, String> ABSOLUTA_ARMING_MODE_LABEL;
   public static final Message<Void, String> ABSOLUTA_SYSTEM_LABEL;
   public static final Message<Void, String> WIRELESS_TRANSCEIVER_LABEL;
   public static final Message<Void, String> ALTERNATE_COMMUNICATOR_MODULE_LABEL;
   public static final Message<Void, String> AUDIO_VERIFICATION_MODULE_LABEL;
   public static final Message<Integer, List<Boolean>> EVENT_REPORTING_CONFIGURATION_READ;
   public static final Message<Pair<Integer, Integer>, List<Septet<Calendar, Integer, Boolean, Integer, Integer, Integer, List<Integer>>>> EVENT_BUFFER_READ;
   public static final Message<Void, Pair<List<Integer>, List<Integer>>> ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS;
   public static final Message<Void, List<Integer>> ABSOLUTA_COMMAND_OUTPUT_ACTIVATION;
   public static final Message<Pair<Integer, String>, Void> ENTER_INSTALLERS_RO_PROGRAMMING_MODE;
   public static final Message<Pair<Integer, String>, Void> ENTER_ACCESS_CODE_RO_PROGRAMMING_MODE;
   public static final Message<Integer, Void> ENTER_BYPASS_PROGRAMMING_MODE;
   public static final Message<Integer, Void> EXIT_CONFIGURATION_MODE;
   public static final Message<Pair<Integer, String>, Void> ENTER_ACCESS_LEVEL;
   public static final Message<Triplet<Integer, Integer, Boolean>, Void> SINGLE_ZONE_BYPASS_WRITE;
   public static final Message<Pair<Integer, Integer>, Void> ARM;
   public static final Message<Integer, Void> DISARM;
   public static final Message<Triplet<Integer, Integer, Integer>, Void> SET_OUTPUT;
   public static final Message<Pair<Integer, List<Boolean>>, Void> EVENT_REPORTING_CONFIGURATION_WRITE;
   public static final Message<Pair<Integer, String>, Void> VALIDATE_USER_CODE;
   public static final Message<Integer, Void> USER_ACTIVITY;
   public static final Message<Pair<Integer, String>, Void> TEST_COMMAND;

   private String name;

   protected Message() {
   }

   @Override
   public final String toString() {
      if (this.name == null) {
         Field[] fields = Message.class.getFields();
         for (Field field : fields) {
               try {
                  if (this == field.get(this)) {
                     this.name = field.getName();
                     break;
                  }
               } catch (IllegalAccessException | IllegalArgumentException ignored) {
               }
         }
      }
      return this.name;
   }

   static {
      ABSOLUTA_PARTITION_LABEL = new NumberedLabelReading(3, 1, DscCharsets.WIN1252);
      ABSOLUTA_OUTPUT_LABEL = new NumberedLabelReading(4, 0, 50, DscCharsets.WIN1252);
      ABSOLUTA_REMOTE_COMMAND_LABEL = new NumberedLabelReading(4, 50, 32, DscCharsets.WIN1252);
      ABSOLUTA_ZONE_LABEL = new NumberedLabelReading(1, 0, DscCharsets.WIN1252);
      ABSOLUTA_ARMING_MODE_LABEL = new NumberedLabelReading(13, 0, DscCharsets.WIN1252);
      ABSOLUTA_SYSTEM_LABEL = new SingleLabelReading(3, 1, DscCharsets.WIN1252);
      WIRELESS_TRANSCEIVER_LABEL = new SingleLabelReading(231, null);
      ALTERNATE_COMMUNICATOR_MODULE_LABEL = new SingleLabelReading(232, null);
      AUDIO_VERIFICATION_MODULE_LABEL = new SingleLabelReading(235, null);
      EVENT_REPORTING_CONFIGURATION_READ = new EventReportingConfigurationReading();
      EVENT_BUFFER_READ = new EventBufferReading();
      ABSOLUTA_ENABLED_OUTPUTS_AND_REMOTE_COMMANDS = new AbsolutaEnabledOutputsAndRemoteCommandsReading();
      ABSOLUTA_COMMAND_OUTPUT_ACTIVATION = new CommandOutputActivationReading();
      ENTER_INSTALLERS_RO_PROGRAMMING_MODE = EnterConfigurationModeWriting.withPartitionAndCodeParam(0, false);
      ENTER_ACCESS_CODE_RO_PROGRAMMING_MODE = EnterConfigurationModeWriting.withPartitionAndCodeParam(1, false);
      ENTER_BYPASS_PROGRAMMING_MODE = EnterConfigurationModeWriting.withPartitionParam(3, true);
      EXIT_CONFIGURATION_MODE = new ExitConfigurationModeWriting();
      ENTER_ACCESS_LEVEL = new EnterAccessLevelWriting();
      SINGLE_ZONE_BYPASS_WRITE = new SingleZoneBypassWriting();
      ARM = new ArmWriting();
      DISARM = new DisarmWriting();
      SET_OUTPUT = new OutputWriting();
      EVENT_REPORTING_CONFIGURATION_WRITE = new EventReportingConfigurationWriting();
      VALIDATE_USER_CODE = new ValidateUserCodeWriting();
      USER_ACTIVITY = new UserActivityWriting();
      TEST_COMMAND = new TestCommandWriting();
   }

   public abstract static class Response {
      protected final Message<?, ?> message;
      protected final Object param;

      protected <P, V> Response(Message<P, V> message, P param) {
         this.message = message;
         this.param = param;
      }

      public boolean isFor(Message<?, ?> message) {
         return this.message == message;
      }

      public Message<?, ?> getMessage() {
         return this.message;
      }

      @SuppressWarnings("unchecked")
      public <P> P getParam(Message<P, ?> message) {
         if (this.message != message) {
               throw new IllegalArgumentException(
                  String.format("unexpected message: %s instead of %s", message, this.message)
               );
         }
         return (P) this.param;
      }
   }
}