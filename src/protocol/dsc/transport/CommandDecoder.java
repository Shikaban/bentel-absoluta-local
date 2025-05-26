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
   private static final Map<Integer, Class<? extends DscCommand>> COMMANDS = initCommandsWith(AccessCodeLengthNotification.class, AccessCodes.class, AccessCodesResponse.class, AccessLevelLeadInOut.class, AlarmMemoryInformation.class, ArmingDisarmingNotification.class, ArmingPreAlertNotification.class, CommandError.class, CommandOutput.class, CommandOutputActivation.class, CommandRequest.class, CommandResponse.class, Configuration.class, EncapsulatedCommandForMultiplePackets.class, EndSession.class, EnterAccessLevel.class, EnterConfigurationMode.class, EntryDelayNotification.class, EventBufferRead.class, EventBufferReadResponse.class, EventReportingConfigurationRead.class, EventReportingConfigurationReadResponse.class, EventReportingConfigurationWrite.class, ExitConfigurationMode.class, ExitDelayNotification.class, LifeStyleZoneStatus.class, MiscellaneousPreAlertNotification.class, OpenSession.class, PartitionAlarmMemoryNotification.class, PartitionArmControl.class, PartitionAssignmentConfiguration.class, PartitionAssignments.class, PartitionAssignmentsResponse.class, PartitionDisarmControl.class, PartitionQuickExitNotification.class, PartitionReadyStatusNotification.class, PartitionStatus.class, Poll.class, RequestAccess.class, SectionRead.class, SectionReadResponse.class, SingleZoneBypassStatus.class, SingleZoneBypassWrite.class, SoftwareVersion.class, SystemCapabilities.class, SystemTroubleStatus.class, TextNotification.class, TimeDateBroadcastNotification.class, TroubleDetail.class, TroubleDetailNotification.class, UserActivity.class, UserPartitionAssignmentConfiguration.class, ZoneAlarmStatus.class, ZoneAssignmentConfiguration.class, ZoneBypassStatus.class, ZoneStatus.class);

   @SafeVarargs
   private static Map<Integer, Class<? extends DscCommand>> initCommandsWith(Class<? extends DscCommand>... var0) {
      try {
         Map<Integer, Class<? extends DscCommand>> var1 = new HashMap();
         Class[] var2 = var0;
         int var3 = var0.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Class<? extends DscCommand> var5 = var2[var4];
            DscCommand var6 = (DscCommand)var5.newInstance();
            Integer var7 = DscUtils.validateUShort(var6.getCommandNumber());
            if (var1.containsKey(var7)) {
               throw new IllegalStateException(String.format("duplicated command number 0x%04X", var7));
            }

            var1.put(var7, var5);
         }

         return ImmutableMap.copyOf(var1);
      } catch (IllegalAccessException | InstantiationException var8) {
         throw new AssertionError("init error", var8);
      }
   }

   public static boolean knows(Class<? extends DscCommand> var0) {
      return COMMANDS == null || COMMANDS.containsValue(var0);
   }

   protected void decode(ChannelHandlerContext var1, ByteBuf var2, List<Object> var3) throws DscProtocolException, CorruptedFrameException {
      assert var2.order() == ByteOrder.BIG_ENDIAN;

      if (SequenceHandlersHelper.isLowACK(var2)) {
         var3.add(LowACK.getInstance());
      } else {
         if (var2.readableBytes() < 2) {
            throw new CorruptedFrameException("invalid frame lenght");
         }

         int var4 = var2.readUnsignedShort();

         try {
            DscCommand var5 = this.newCommand(var4);
            if (var5 instanceof DscCommandWithAppSeq) {
               short var6 = var2.readUnsignedByte();
               ((DscCommandWithAppSeq)var5).setAppSeq(var6);
            }

            if (var5 instanceof CommandRequest) {
               this.parseCommandRequest((CommandRequest)var5, var2);
            } else {
               var5.readFrom(var2);
            }

            var3.add(var5);
         } catch (IndexOutOfBoundsException var7) {
            throw new WrongCommandLengthException(var4, "too short command data");
         }
      }

   }

   private DscCommand newCommand(int var1) {
      try {
         Class<? extends DscCommand> var2 = (Class)COMMANDS.get(var1);
         return (DscCommand)(var2 != null ? (DscCommand)var2.newInstance() : new UnknownCommand(var1));
      } catch (IllegalAccessException | InstantiationException var3) {
         throw new AssertionError("unexpected error", var3);
      }
   }

   private void parseCommandRequest(CommandRequest var1, ByteBuf var2) {
      int var3 = var2.readUnsignedShort();
      DscCommand var4 = this.newCommand(var3);
      if (var4 instanceof DscRequestableCommand) {
         DscRequestableCommand var5 = (DscRequestableCommand)var4;
         var5.readRequestDataFrom(var2);
         var1.setRequestedCmd(var5);
         var1.readCodeFrom(var2);
      } else {
         System.out.println("WARN: unknown or unexpected requested command: " + var4.getCommandNumber());
      }

   }
}
