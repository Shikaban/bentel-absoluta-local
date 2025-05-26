package protocol.dsc.transport.command_handlers;

import io.netty.channel.Channel;
import protocol.dsc.commands.RequestAccess;
import protocol.dsc.session.SessionInfo;
import protocol.dsc.transport.AESDecoder;
import protocol.dsc.transport.AESEncoder;
import protocol.dsc.util.DscUtils;

public class RequestAccessHandler extends HandshakeHandler<RequestAccess> {
   public static final String DEFAULT_ACCESS_CODE = "12345678";
   private byte[] ownSessionKey;
   private static final boolean VERBOSE_DEBUG = false;

   public RequestAccessHandler() {
      super(RequestAccess.class);
   }

   public boolean validateOwnInfo(SessionInfo var1) {
      String var2 = var1.getIdentifierOrInitKey();
      switch(var1.getEncryptionType()) {
      case 0:
         return var2 != null;
      case 1:
         return var2 != null && var2.length() >= 8;
      default:
         return false;
      }
   }

   protected RequestAccess getCommand(Channel var1) {
      SessionInfo var2 = SessionInfo.getOwnInfo(var1);
      String var3 = var2.getIdentifierOrInitKey();
      RequestAccess var4 = new RequestAccess();
      switch(var2.getEncryptionType()) {
      case 0:
         this.ownSessionKey = null;
         var4.setIdentifier(var3);
         break;
      case 1:
         this.ownSessionKey = RequestAccessAESHelper.getRandomBytes();
         (new RequestAccessAESHelper(var4)).encryptKey(var3, this.ownSessionKey);
         break;
      default:
         throw new IllegalStateException("unexpected encryption type");
      }

      return var4;
   }

   protected void commandSent(Channel var1) {
      if (this.ownSessionKey != null) {
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: decoding key: " + DscUtils.hexDump(this.ownSessionKey));
         }
         AESDecoder.setKey(var1, this.ownSessionKey);
      }
   }

   protected int commandReceived(Channel var1, RequestAccess var2) {
      SessionInfo var3 = SessionInfo.getPeerInfo(var1);
      switch(var3.getEncryptionType()) {
      case 0:
         String var4 = var2.getIdentifier();
         var3.setIdentifierOrInitKey(var4);
         if(VERBOSE_DEBUG) {
            System.out.println("DEBUG: peer identifier: " + var4);
         }
         return 0;
      case 1:
         String var5 = var3.getIdentifierOrInitKey();
         if (var5 == null) {
            var5 = var3.getMultiPointCommId();
            var3.setIdentifierOrInitKey(var5);
         }

         byte[] var6 = (new RequestAccessAESHelper(var2)).decryptKey(var5);
         if (var6 != null) {
            if(VERBOSE_DEBUG) {
               System.out.println("DEBUG: encoding key: " + DscUtils.hexDump(var6));
            }
            AESEncoder.setKey(var1, var6);
            return 0;
         }

         System.out.println("WARN: invalid access request received");
         return 1;
      default:
         System.out.println("WARN: unexpected encryption type");
         return 1;
      }
   }
}
