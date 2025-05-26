package protocol.dsc.session;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class SessionInfo {
   public static final int INTERFACE_COMMUNICATOR_MODULE = 2;
   public static final int I_CONTROL = 128;
   public static final int KEYPAD = 129;
   public static final int GENERIC_3RD_PARTY_DEVICE = 143;
   public static final int NO_ENCRYPTION = 0;
   public static final int AES_128_ENCRYPTION = 1;
   private static final AttributeKey<SessionInfo> OWN_INFO = AttributeKey.valueOf("SessionInfo.ownInfo");
   private static final AttributeKey<SessionInfo> PEER_INFO = AttributeKey.valueOf("SessionInfo.peerInfo");
   private Boolean client;
   private Integer deviceTypeOrVendorID;
   private Integer deviceId;
   private String softwareVersion;
   private String protocolVersion;
   private Integer txSize;
   private Integer rxSize;
   private Integer encryptionType;
   private String identifierOrInitKey;
   private String softwareVersionFields;
   private String multiPointCommId;

   public Boolean isClient() {
      return this.client;
   }

   public Boolean getClient() {
      return this.client;
   }

   public void setClient(Boolean var1) {
      this.client = var1;
   }

   public Integer getDeviceTypeOrVendorID() {
      return this.deviceTypeOrVendorID;
   }

   public void setDeviceTypeOrVendorID(Integer var1) {
      this.deviceTypeOrVendorID = var1;
   }

   public Integer getDeviceId() {
      return this.deviceId;
   }

   public void setDeviceId(Integer var1) {
      this.deviceId = var1;
   }

   public String getSoftwareVersion() {
      return this.softwareVersion;
   }

   public void setSoftwareVersion(String var1) {
      this.softwareVersion = var1;
   }

   public String getProtocolVersion() {
      return this.protocolVersion;
   }

   public void setProtocolVersion(String var1) {
      this.protocolVersion = var1;
   }

   public Integer getTxSize() {
      return this.txSize;
   }

   public void setTxSize(Integer var1) {
      this.txSize = var1;
   }

   public Integer getRxSize() {
      return this.rxSize;
   }

   public void setRxSize(Integer var1) {
      this.rxSize = var1;
   }

   public Integer getEncryptionType() {
      return this.encryptionType;
   }

   public void setEncryptionType(Integer var1) {
      this.encryptionType = var1;
   }

   public String getIdentifierOrInitKey() {
      return this.identifierOrInitKey;
   }

   public void setIdentifierOrInitKey(String var1) {
      this.identifierOrInitKey = var1;
   }

   public String getSoftwareVersionFields() {
      return this.softwareVersionFields;
   }

   public void setSoftwareVersionFields(String var1) {
      this.softwareVersionFields = var1;
   }

   public String getMultiPointCommId() {
      return this.multiPointCommId;
   }

   public void setMultiPointCommId(String var1) {
      this.multiPointCommId = var1;
   }

   public static void initInfo(Channel var0, SessionInfo var1) {
      if (var1.getClient() == null) {
         throw new IllegalArgumentException("invalid own info (null client)");
      } else if (var0.attr(OWN_INFO).setIfAbsent(var1) != null) {
         throw new IllegalStateException("info already initialized");
      } else {
         SessionInfo var2 = new SessionInfo();
         var2.setClient(!var1.getClient());
         var0.attr(PEER_INFO).set(var2);
      }
   }

   public static SessionInfo getOwnInfo(Channel var0) {
      return getInfo(OWN_INFO, var0);
   }

   public static SessionInfo getPeerInfo(Channel var0) {
      return getInfo(PEER_INFO, var0);
   }

   private static SessionInfo getInfo(AttributeKey<SessionInfo> var0, Channel var1) {
      SessionInfo var2 = (SessionInfo)var1.attr(var0).get();
      if (var2 != null) {
         return var2;
      } else {
         throw new IllegalStateException("info not initialized");
      }
   }
}
