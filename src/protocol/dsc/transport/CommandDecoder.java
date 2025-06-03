package protocol.dsc.transport;

import com.google.common.collect.ImmutableMap;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToMessageDecoder;
import protocol.dsc.commands.AccessCodeLengthNotification;
import protocol.dsc.commands.AccessCodes;
import protocol.dsc.commands.AccessCodesResponse;
import protocol.dsc.commands.AccessLevelLeadInOut;
import protocol.dsc.commands.AlarmMemoryInformation;
import protocol.dsc.commands.ArmingDisarmingNotification;
import protocol.dsc.commands.ArmingPreAlertNotification;
import protocol.dsc.commands.CommandError;
import protocol.dsc.commands.CommandOutput;
import protocol.dsc.commands.CommandOutputActivation;
import protocol.dsc.commands.CommandRequest;
import protocol.dsc.commands.CommandResponse;
import protocol.dsc.commands.Configuration;
import protocol.dsc.commands.DscCommand;
import protocol.dsc.commands.DscCommandWithAppSeq;
import protocol.dsc.commands.DscRequestableCommand;
import protocol.dsc.commands.EncapsulatedCommandForMultiplePackets;
import protocol.dsc.commands.EndSession;
import protocol.dsc.commands.EnterAccessLevel;
import protocol.dsc.commands.EnterConfigurationMode;
import protocol.dsc.commands.EntryDelayNotification;
import protocol.dsc.commands.EventBufferRead;
import protocol.dsc.commands.EventBufferReadResponse;
import protocol.dsc.commands.EventReportingConfigurationRead;
import protocol.dsc.commands.EventReportingConfigurationReadResponse;
import protocol.dsc.commands.EventReportingConfigurationWrite;
import protocol.dsc.commands.ExitConfigurationMode;
import protocol.dsc.commands.ExitDelayNotification;
import protocol.dsc.commands.LifeStyleZoneStatus;
import protocol.dsc.commands.LowACK;
import protocol.dsc.commands.MiscellaneousPreAlertNotification;
import protocol.dsc.commands.OpenSession;
import protocol.dsc.commands.PartitionAlarmMemoryNotification;
import protocol.dsc.commands.PartitionArmControl;
import protocol.dsc.commands.PartitionAssignmentConfiguration;
import protocol.dsc.commands.PartitionAssignments;
import protocol.dsc.commands.PartitionAssignmentsResponse;
import protocol.dsc.commands.PartitionDisarmControl;
import protocol.dsc.commands.PartitionQuickExitNotification;
import protocol.dsc.commands.PartitionReadyStatusNotification;
import protocol.dsc.commands.PartitionStatus;
import protocol.dsc.commands.Poll;
import protocol.dsc.commands.RequestAccess;
import protocol.dsc.commands.SectionRead;
import protocol.dsc.commands.SectionReadResponse;
import protocol.dsc.commands.SingleZoneBypassStatus;
import protocol.dsc.commands.SingleZoneBypassWrite;
import protocol.dsc.commands.SoftwareVersion;
import protocol.dsc.commands.SystemCapabilities;
import protocol.dsc.commands.SystemTroubleStatus;
import protocol.dsc.commands.TextNotification;
import protocol.dsc.commands.TimeDateBroadcastNotification;
import protocol.dsc.commands.TroubleDetail;
import protocol.dsc.commands.TroubleDetailNotification;
import protocol.dsc.commands.UnknownCommand;
import protocol.dsc.commands.UserActivity;
import protocol.dsc.commands.UserPartitionAssignmentConfiguration;
import protocol.dsc.commands.ZoneAlarmStatus;
import protocol.dsc.commands.ZoneAssignmentConfiguration;
import protocol.dsc.commands.ZoneBypassStatus;
import protocol.dsc.commands.ZoneStatus;
import protocol.dsc.errors.DscProtocolException;
import protocol.dsc.errors.WrongCommandLengthException;
import protocol.dsc.util.DscUtils;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Sharable
public class CommandDecoder extends MessageToMessageDecoder<ByteBuf> {
   private static final Map<Integer, Class<? extends DscCommand>> COMMANDS = initCommandsWith(
         AccessCodeLengthNotification.class, AccessCodes.class, AccessCodesResponse.class, AccessLevelLeadInOut.class,
         AlarmMemoryInformation.class, ArmingDisarmingNotification.class, ArmingPreAlertNotification.class,
         CommandError.class, CommandOutput.class, CommandOutputActivation.class, CommandRequest.class,
         CommandResponse.class, Configuration.class, EncapsulatedCommandForMultiplePackets.class, EndSession.class,
         EnterAccessLevel.class, EnterConfigurationMode.class, EntryDelayNotification.class, EventBufferRead.class,
         EventBufferReadResponse.class, EventReportingConfigurationRead.class, EventReportingConfigurationReadResponse.class,
         EventReportingConfigurationWrite.class, ExitConfigurationMode.class, ExitDelayNotification.class,
         LifeStyleZoneStatus.class, MiscellaneousPreAlertNotification.class, OpenSession.class,
         PartitionAlarmMemoryNotification.class, PartitionArmControl.class, PartitionAssignmentConfiguration.class,
         PartitionAssignments.class, PartitionAssignmentsResponse.class, PartitionDisarmControl.class,
         PartitionQuickExitNotification.class, PartitionReadyStatusNotification.class, PartitionStatus.class, Poll.class,
         RequestAccess.class, SectionRead.class, SectionReadResponse.class, SingleZoneBypassStatus.class,
         SingleZoneBypassWrite.class, SoftwareVersion.class, SystemCapabilities.class, SystemTroubleStatus.class,
         TextNotification.class, TimeDateBroadcastNotification.class, TroubleDetail.class, TroubleDetailNotification.class,
         UserActivity.class, UserPartitionAssignmentConfiguration.class, ZoneAlarmStatus.class,
         ZoneAssignmentConfiguration.class, ZoneBypassStatus.class, ZoneStatus.class);

   @SafeVarargs
   private static Map<Integer, Class<? extends DscCommand>> initCommandsWith(Class<? extends DscCommand>... commandClasses) {
      try {
         Map<Integer, Class<? extends DscCommand>> commandMap = new HashMap<>();
         for (Class<? extends DscCommand> clazz : commandClasses) {
            DscCommand instance = clazz.getDeclaredConstructor().newInstance();
            Integer commandNumber = DscUtils.validateUShort(instance.getCommandNumber());
            if (commandMap.containsKey(commandNumber)) {
               throw new IllegalStateException(String.format("Duplicated command number 0x%04X", commandNumber));
            }
            commandMap.put(commandNumber, clazz);
         }
         return ImmutableMap.copyOf(commandMap);
      } catch (ReflectiveOperationException e) {
         throw new AssertionError("Error initializing command map", e);
      }
   }

   public static boolean knows(Class<? extends DscCommand> clazz) {
      return COMMANDS.containsValue(clazz);
   }

   @Override
   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
         throws DscProtocolException, CorruptedFrameException {
      assert in.order() == ByteOrder.BIG_ENDIAN;

      if (SequenceHandlersHelper.isLowACK(in)) {
         out.add(LowACK.getInstance());
         return;
      }

      if (in.readableBytes() < 2) {
         throw new CorruptedFrameException("Invalid frame length");
      }

      int commandNumber = in.readUnsignedShort();

      try {
         DscCommand command = this.newCommand(commandNumber);
         if (command instanceof DscCommandWithAppSeq) {
            short appSeq = in.readUnsignedByte();
            ((DscCommandWithAppSeq) command).setAppSeq(appSeq);
         }

         if (command instanceof CommandRequest) {
            this.parseCommandRequest((CommandRequest) command, in);
         } else {
            command.readFrom(in);
         }

         out.add(command);
      } catch (IndexOutOfBoundsException ex) {
         throw new WrongCommandLengthException(commandNumber, "Too short command data");
      }
   }

   private DscCommand newCommand(int commandNumber) {
      try {
         Class<? extends DscCommand> clazz = COMMANDS.get(commandNumber);
         if (clazz != null) {
            return clazz.getDeclaredConstructor().newInstance();
         } else {
            return new UnknownCommand(commandNumber);
         }
      } catch (ReflectiveOperationException e) {
         throw new AssertionError("Unexpected error creating command instance", e);
      }
   }

   private void parseCommandRequest(CommandRequest request, ByteBuf in) {
      int requestedCmdNumber = in.readUnsignedShort();
      DscCommand requestedCmd = this.newCommand(requestedCmdNumber);
      if (requestedCmd instanceof DscRequestableCommand) {
         DscRequestableCommand reqCmd = (DscRequestableCommand) requestedCmd;
         reqCmd.readRequestDataFrom(in);
         request.setRequestedCmd(reqCmd);
         request.readCodeFrom(in);
      } else {
         System.out.println("WARN: Unknown or unexpected requested command: " + requestedCmd.getCommandNumber());
      }
   }
}