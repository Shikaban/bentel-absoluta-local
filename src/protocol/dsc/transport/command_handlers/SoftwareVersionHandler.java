package protocol.dsc.transport.command_handlers;

import io.netty.channel.Channel;
import protocol.dsc.commands.SoftwareVersion;
import protocol.dsc.session.SessionInfo;

public class SoftwareVersionHandler extends HandshakeHandler<SoftwareVersion> {
   private static final boolean VERBOSE_DEBUG = false;

   public SoftwareVersionHandler() {
      super(SoftwareVersion.class);
   }

   public boolean validateOwnInfo(SessionInfo var1) {
      return var1.getSoftwareVersionFields() != null;
   }

   protected SoftwareVersion getCommand(Channel var1) {
      String var2 = SessionInfo.getOwnInfo(var1).getSoftwareVersionFields();
      SoftwareVersion var3 = new SoftwareVersion();
      var3.setVersionFields(var2);
      return var3;
   }

   protected int commandReceived(Channel var1, SoftwareVersion var2) {
      String var3 = var2.getVersionFields();
      SessionInfo.getPeerInfo(var1).setIdentifierOrInitKey(var3);
      if(VERBOSE_DEBUG) {
         System.out.println("DEBUG: peer software version fields:" + var3);
      }
      return 0;
   }
}
