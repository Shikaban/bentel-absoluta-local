package protocol.dsc.transport.command_handlers;

import io.netty.channel.Channel;

import protocol.dsc.commands.SoftwareVersion;
import protocol.dsc.session.SessionInfo;

import java.util.logging.Logger;

public class SoftwareVersionHandler extends HandshakeHandler<SoftwareVersion> {
   private static final Logger logger = Logger.getLogger(SoftwareVersionHandler.class.getName());

   public SoftwareVersionHandler() {
      super(SoftwareVersion.class);
   }

   public boolean validateOwnInfo(SessionInfo sessionInfo2) {
      return sessionInfo2.getSoftwareVersionFields() != null;
   }

   protected SoftwareVersion getCommand(Channel channel2) {
      String ownInfo = SessionInfo.getOwnInfo(channel2).getSoftwareVersionFields();
      SoftwareVersion softwareVersion2 = new SoftwareVersion();
      softwareVersion2.setVersionFields(ownInfo);
      return softwareVersion2;
   }

   protected int commandReceived(Channel channel2, SoftwareVersion softwareVersion2) {
      String versionFields = softwareVersion2.getVersionFields();
      SessionInfo.getPeerInfo(channel2).setIdentifierOrInitKey(versionFields);
      logger.fine("Peer software version fields:" + versionFields);
      return 0;
   }
}
