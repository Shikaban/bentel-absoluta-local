package plugin.absoluta.connection;

import cms.device.api.Panel.Arming;
import protocol.dsc.Message;

import java.util.Objects;
import org.javatuples.Pair;
import org.javatuples.Triplet;

public class Commander {
   private static final Integer SYSTEM = null;
   private static final int STAY_ARM = 1;
   private static final int AWAY_ARM = 2;
   private static final int INSTANT_STAY_ARM = 7;
   private static final int CLEAR_FAULTS = 10;
   static final int ACTIVATE_OUTPUT = 1;
   static final int DEACTIVATE_OUTPUT = 2;
   private final MessageHandler messageHandler;

   Commander(MessageHandler var1) {
      this.messageHandler = (MessageHandler)Objects.requireNonNull(var1);
   }

   public void arming(Arming newArmingStatus) {
      System.out.println("DEBUG: setting global arming to: " + newArmingStatus);
      switch(newArmingStatus) {
      case GLOBALLY_DISARMED:
         this.messageHandler.sendCommand(Message.DISARM, SYSTEM);
         break;
      case GLOBALLY_ARMED:
         this.messageHandler.sendCommand(Message.ARM, Pair.with(SYSTEM, AWAY_ARM));
         break;
      default:
         break;
      }

   }

   public void partitionArming(String partitionID, cms.device.api.Partition.Arming newArmingStatus) {
      System.out.println("DEBUG: setting partition " + partitionID + " arming to: " + newArmingStatus);
      Integer partitionIDInteger = Integer.valueOf(partitionID);
      switch(newArmingStatus) {
      case DISARMED:
         this.messageHandler.sendCommand(Message.DISARM, partitionIDInteger);
         break;
      case AWAY:
         this.messageHandler.sendCommand(Message.ARM, Pair.with(partitionIDInteger, AWAY_ARM));
         break;
      case STAY:
         this.messageHandler.sendCommand(Message.ARM, Pair.with(partitionIDInteger, STAY_ARM));
         break;
      case NODELAY:
         this.messageHandler.sendCommand(Message.ARM, Pair.with(partitionIDInteger, INSTANT_STAY_ARM));
         break;
      default:
         break;
      }

   }

   public boolean armingSupport(char presetMode) {
      return CustomizedArmingModes.CUSTOMIZED_ARMING_MODES.containsKey(presetMode);
   }

   public void armingSet(char presetMode) {
      System.out.println("DEBUG: setting global arming to preset " + presetMode);
      Integer presetModeInteger = (Integer)CustomizedArmingModes.CUSTOMIZED_ARMING_MODES.get(presetMode);
      if (presetModeInteger != null) {
         this.messageHandler.sendCommand(Message.ARM, Pair.with(SYSTEM, presetModeInteger));
      }
   }

   public void setBypassed(String zoneID, boolean setBypassed) {
      System.out.println("DEBUG: setting zone " + zoneID + " bypass to " + setBypassed);
      Integer zoneIDInteger = Integer.valueOf(zoneID);
      this.messageHandler.sendCommand(Message.SINGLE_ZONE_BYPASS_WRITE, Triplet.with((Integer)null, zoneIDInteger, setBypassed));
   }

   public void setOutput(String outputID, boolean setStatus) {
      System.out.println("DEBUG: " + (setStatus ? "closing" : "opening") + " output  " + outputID);
      Integer outputIDInteger = Integer.valueOf(outputID);
      Integer statusInteger = setStatus ? 1 : 2;
      this.messageHandler.sendCommand(Message.SET_OUTPUT, Triplet.with((Integer)null, outputIDInteger, statusInteger));
   }

   public void cleanTroubles() {
      System.out.println("DEBUG: cleaning troubles");
      this.messageHandler.sendCommand(Message.USER_ACTIVITY, CLEAR_FAULTS);
   }
}
