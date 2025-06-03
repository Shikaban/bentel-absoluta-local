
package plugin.absoluta;

import com.google.common.collect.ImmutableMap;

import cms.device.api.Panel;
import cms.device.api.Output.Action;
import cms.device.api.Panel.Arming;
import cms.device.spi.PanelProvider;
import plugin.absoluta.connection.ConnectionHandler;
import plugin.absoluta.connection.ConnectionThread;
import plugin.absoluta.connection.PanelStatus;

import java.util.Map;
import org.openide.util.Exceptions;

public class AbsolutaPanelProvider implements PanelProvider {
   private final String address;
   private final int port;
   private final String pin;
   private final PanelStatus panelStatus;
   private PanelCallback callback;
   private ConnectionHandler connectionHandler;

   public AbsolutaPanelProvider(Map<String, String> var1) {
      this.address = (String)var1.get("address");
      this.port = Integer.parseInt(var1.getOrDefault("port", "3064"));
      this.pin = (String)var1.get("pin");
      this.panelStatus = new PanelStatus();
   }

   public Map<String, String> getSettings() {
      return ImmutableMap.<String, String>builder()
         .put("address", this.address)
         .put("port", Integer.toString(this.port))
         .put("pin", this.pin)
         .build();
   }

   public void initialize(PanelCallback var1) {
      this.callback = var1;
      this.panelStatus.addPropertyChangeListener(new CallbackListener(var1, this.panelStatus));
   }

   public Panel.connStatus connect() {
      this.connectionHandler = new ConnectionHandler(this.panelStatus, this.callback);
      if (!this.connectionHandler.setPin(this.pin)) {
         return Panel.connStatus.UNAUTHORIZED;
      } else {
         (new ConnectionThread(this.address, this.port, this.connectionHandler)).start();

         try {
            return this.connectionHandler.waitConnection();
         } catch (InterruptedException var2) {
            Exceptions.printStackTrace(var2);
            return Panel.connStatus.UNREACHABLE;
         }
      }
   }

   public void disconnect() {
      this.connectionHandler.disconnect();
   }

   public void arming(Arming var1) {
      this.connectionHandler.getCommander().arming(var1);
   }

   public void partitionArming(String var1, cms.device.api.Partition.Arming var2) {
      this.connectionHandler.getCommander().partitionArming(var1, var2);
   }

   public boolean armingSupport(char presetMode) {
      return this.connectionHandler.getCommander().armingSupport(presetMode);
   }

   public void armingSet(char presetMode) {
      this.connectionHandler.getCommander().armingSet(presetMode);
   }

   public void setBypassed(String zoneID, boolean setBypassed) {
      this.connectionHandler.getCommander().setBypassed(zoneID, setBypassed);
   }

   public boolean getBypassed(String zoneID) {
      int zoneInt = Integer.parseInt(zoneID);
      return this.panelStatus.getZoneBypass(zoneInt);
   }

   public void doOutputAction(String var1, Action var2) {
      switch(var2) {
      case DO_CLOSE:
         this.connectionHandler.getCommander().setOutput(var1, true);
         break;
      case DO_OPEN:
         this.connectionHandler.getCommander().setOutput(var1, false);
         break;
      default:
         break;
      }
   }

   void cleanTroubles() {
      this.connectionHandler.getCommander().cleanTroubles();
   }

}
