package absoluta.connection;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Trouble {
   private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("absoluta.connection.Bundle");

   static final int ZONE_DEVICE = 1;
   static final int TROUBLE_RESTORE = 0;
   static final int TROUBLE = 1;
   static final int TROUBLE_IN_MEMORY = 2;

   private final int deviceType;      // Device type (zone, sensor, etc.)
   private final int troubleType;     // Trouble type (restore, trouble, in memory)
   private final int deviceNumber;    // Device identifier
   private final boolean inMemory;    // True if the trouble exists only in memory

   Trouble(int deviceType, int troubleType, int deviceNumber, boolean inMemory) {
      this.deviceType = deviceType;
      this.troubleType = troubleType;
      this.deviceNumber = deviceNumber;
      this.inMemory = inMemory;
   }

   int getDeviceType() {
      return this.deviceType;
   }

   int getTroubleType() {
      return this.troubleType;
   }

   int getDeviceNumber() {
      return this.deviceNumber;
   }

   public boolean isInMemory() {
      return this.inMemory;
   }

   public String toLocalizedString(PanelStatus panelStatus) {
      StringBuilder sb = new StringBuilder();

      try {
         sb.append(BUNDLE.getString(String.format("Trouble.deviceType.0x%02X", this.deviceType)));
      } catch (MissingResourceException ex) {
         sb.append(String.format("%s 0x%02X", BUNDLE.getString("Trouble.deviceType"), this.deviceType));
      }

      // If it is a zone with a label, include the label; otherwise show only the number
      if (this.deviceType == ZONE_DEVICE && panelStatus.getZoneLabel(this.deviceNumber) != null) {
         sb.append(" ").append(panelStatus.getZoneLabel(this.deviceNumber));
      } else if (this.deviceNumber != 0) {
         sb.append(" #").append(this.deviceNumber);
      }

      sb.append(": ");

      try {
         sb.append(BUNDLE.getString(String.format("Trouble.troubleType.0x%02X", this.troubleType)));
      } catch (MissingResourceException ex) {
         sb.append(String.format("%s 0x%02X", BUNDLE.getString("Trouble.troubleType"), this.troubleType));
      }

      if (this.inMemory) {
         sb.append(" (").append(BUNDLE.getString("Trouble.inMemory")).append(")");
      }

      return sb.toString();
   }

   @Override
   public String toString() {
      return "Trouble{deviceType=" + this.deviceType + ", troubleType=" + this.troubleType + ", deviceNumber=" + this.deviceNumber + ", inMemory=" + this.inMemory + '}';
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      Trouble other = (Trouble) obj;
      return deviceType == other.deviceType &&
            troubleType == other.troubleType &&
            deviceNumber == other.deviceNumber &&
            inMemory == other.inMemory;
   }

   @Override
   public int hashCode() {
      int hash = 5;
      hash = 89 * hash + deviceType;
      hash = 89 * hash + troubleType;
      hash = 89 * hash + deviceNumber;
      hash = 89 * hash + (inMemory ? 1 : 0);
      return hash;
   }
}
