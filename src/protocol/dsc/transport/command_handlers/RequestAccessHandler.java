package protocol.dsc.transport.command_handlers;

import io.netty.channel.Channel;

import protocol.dsc.commands.RequestAccess;
import protocol.dsc.session.SessionInfo;
import protocol.dsc.transport.AESDecoder;
import protocol.dsc.transport.AESEncoder;
import protocol.dsc.util.DscUtils;

import java.util.logging.Logger;

public class RequestAccessHandler extends HandshakeHandler<RequestAccess> {
   private static final Logger logger = Logger.getLogger(RequestAccessHandler.class.getName());
   public static final String DEFAULT_ACCESS_CODE = "12345678";
   private byte[] ownSessionKey;

   public RequestAccessHandler() {
      super(RequestAccess.class);
   }

   public boolean validateOwnInfo(SessionInfo sessionInfo2) {
      String identifierOrInitKey = sessionInfo2.getIdentifierOrInitKey();
      switch(sessionInfo2.getEncryptionType()) {
      case 0:
         return identifierOrInitKey != null;
      case 1:
         return identifierOrInitKey != null && identifierOrInitKey.length() >= 8;
      default:
         return false;
      }
   }

   protected RequestAccess getCommand(Channel channel2) {
      SessionInfo ownInfo = SessionInfo.getOwnInfo(channel2);
      String identifierOrInitKey = ownInfo.getIdentifierOrInitKey();
      RequestAccess requestAccess2 = new RequestAccess();
      switch(ownInfo.getEncryptionType()) {
      case 0:
         this.ownSessionKey = null;
         requestAccess2.setIdentifier(identifierOrInitKey);
         break;
      case 1:
         this.ownSessionKey = RequestAccessAESHelper.getRandomBytes();
         (new RequestAccessAESHelper(requestAccess2)).encryptKey(identifierOrInitKey, this.ownSessionKey);
         break;
      default:
         throw new IllegalStateException("unexpected encryption type");
      }

      return requestAccess2;
   }

   protected void commandSent(Channel channel2) {
      if (this.ownSessionKey != null) {
         logger.fine("Decoding key: " + DscUtils.hexDump(this.ownSessionKey));
         AESDecoder.setKey(channel2, this.ownSessionKey);
      }
   }

   protected int commandReceived(Channel channel2, RequestAccess requestAccess2) {
      SessionInfo peerInfo = SessionInfo.getPeerInfo(channel2);
      switch(peerInfo.getEncryptionType()) {
      case 0:
         String identifier2 = requestAccess2.getIdentifier();
         peerInfo.setIdentifierOrInitKey(identifier2);
         logger.fine("Peer identifier: " + identifier2);
         return 0;
      case 1:
         String identifierOrInitKey = peerInfo.getIdentifierOrInitKey();
         if (identifierOrInitKey == null) {
            identifierOrInitKey = peerInfo.getMultiPointCommId();
            peerInfo.setIdentifierOrInitKey(identifierOrInitKey);
         }

         byte[] byteValue = (new RequestAccessAESHelper(requestAccess2)).decryptKey(identifierOrInitKey);
         if (byteValue != null) {
            logger.fine("Encoding key: " + DscUtils.hexDump(byteValue));
            AESEncoder.setKey(channel2, byteValue);
            return 0;
         }

         logger.warning("Invalid access request received");
         return 1;
      default:
         logger.warning("Unexpected encryption type");
         return 1;
      }
   }
}
