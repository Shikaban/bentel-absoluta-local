package protocol.dsc.transport.command_handlers;

import io.netty.channel.Channel;
import protocol.dsc.commands.OpenSession;
import protocol.dsc.session.SessionInfo;

public class OpenSessionHandler extends HandshakeHandler<OpenSession> {
   public OpenSessionHandler() {
      super(OpenSession.class);
   }

   public boolean validateOwnInfo(SessionInfo var1) {
      return var1.getDeviceTypeOrVendorID() != null && var1.getDeviceId() != null && var1.getSoftwareVersion() != null && var1.getProtocolVersion() != null && var1.getTxSize() != null && var1.getRxSize() != null && (var1.getEncryptionType() != null || !Boolean.TRUE.equals(var1.isClient()));
   }

   protected OpenSession getCommand(Channel var1) {
      SessionInfo var2 = SessionInfo.getOwnInfo(var1);
      OpenSession var3 = new OpenSession();
      var3.setDeviceTypeOrVendorID(var2.getDeviceTypeOrVendorID());
      var3.setDeviceId(var2.getDeviceId());
      var3.setSoftwareVersion(var2.getSoftwareVersion());
      var3.setProtocolVersion(var2.getProtocolVersion());
      var3.setTxSize(var2.getTxSize());
      var3.setRxSize(var2.getRxSize());
      var3.setEncriptionType(var2.getEncryptionType());
      return var3;
   }

   protected int commandReceived(Channel var1, OpenSession var2) {
      SessionInfo var3 = SessionInfo.getPeerInfo(var1);
      var3.setDeviceTypeOrVendorID(var2.getDeviceTypeOrVendorID());
      var3.setDeviceId(var2.getDeviceId());
      var3.setSoftwareVersion(var2.getSoftwareVersion());
      var3.setProtocolVersion(var2.getProtocolVersion());
      var3.setTxSize(var2.getTxSize());
      var3.setRxSize(var2.getRxSize());
      var3.setEncryptionType(var2.getEncryptionType());
      if (var3.getEncryptionType() != 0 && var3.getEncryptionType() != 1) {
         return 3;
      } else {
         SessionInfo var4 = SessionInfo.getOwnInfo(var1);
         if (var4.getEncryptionType() == null) {
            var4.setEncryptionType(var3.getEncryptionType());
         } else if (!var4.getEncryptionType().equals(var3.getEncryptionType())) {
            return 3;
         }

         return 0;
      }
   }
}
