package protocol.dsc.transport.command_handlers;

import io.netty.channel.Channel;
import protocol.dsc.commands.OpenSession;
import protocol.dsc.session.SessionInfo;

public class OpenSessionHandler extends HandshakeHandler<OpenSession> {
   public OpenSessionHandler() {
      super(OpenSession.class);
   }

   public boolean validateOwnInfo(SessionInfo sessionInfo2) {
      return sessionInfo2.getDeviceTypeOrVendorID() != null && sessionInfo2.getDeviceId() != null && sessionInfo2.getSoftwareVersion() != null && sessionInfo2.getProtocolVersion() != null && sessionInfo2.getTxSize() != null && sessionInfo2.getRxSize() != null && (sessionInfo2.getEncryptionType() != null || !Boolean.TRUE.equals(sessionInfo2.isClient()));
   }

   protected OpenSession getCommand(Channel channel2) {
      SessionInfo ownInfo = SessionInfo.getOwnInfo(channel2);
      OpenSession openSession2 = new OpenSession();
      openSession2.setDeviceTypeOrVendorID(ownInfo.getDeviceTypeOrVendorID());
      openSession2.setDeviceId(ownInfo.getDeviceId());
      openSession2.setSoftwareVersion(ownInfo.getSoftwareVersion());
      openSession2.setProtocolVersion(ownInfo.getProtocolVersion());
      openSession2.setTxSize(ownInfo.getTxSize());
      openSession2.setRxSize(ownInfo.getRxSize());
      openSession2.setEncriptionType(ownInfo.getEncryptionType());
      return openSession2;
   }

   protected int commandReceived(Channel channel2, OpenSession openSession2) {
      SessionInfo peerInfo = SessionInfo.getPeerInfo(channel2);
      peerInfo.setDeviceTypeOrVendorID(openSession2.getDeviceTypeOrVendorID());
      peerInfo.setDeviceId(openSession2.getDeviceId());
      peerInfo.setSoftwareVersion(openSession2.getSoftwareVersion());
      peerInfo.setProtocolVersion(openSession2.getProtocolVersion());
      peerInfo.setTxSize(openSession2.getTxSize());
      peerInfo.setRxSize(openSession2.getRxSize());
      peerInfo.setEncryptionType(openSession2.getEncryptionType());
      if (peerInfo.getEncryptionType() != 0 && peerInfo.getEncryptionType() != 1) {
         return 3;
      } else {
         SessionInfo ownInfo = SessionInfo.getOwnInfo(channel2);
         if (ownInfo.getEncryptionType() == null) {
            ownInfo.setEncryptionType(peerInfo.getEncryptionType());
         } else if (!ownInfo.getEncryptionType().equals(peerInfo.getEncryptionType())) {
            return 3;
         }

         return 0;
      }
   }
}
