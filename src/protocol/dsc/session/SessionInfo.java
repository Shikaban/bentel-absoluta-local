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

   public void setClient(Boolean enabled) {
      this.client = enabled;
   }

   public Integer getDeviceTypeOrVendorID() {
      return this.deviceTypeOrVendorID;
   }

   public void setDeviceTypeOrVendorID(Integer deviceTypeOrVendorID) {
      this.deviceTypeOrVendorID = deviceTypeOrVendorID;
   }

   public Integer getDeviceId() {
      return this.deviceId;
   }

   public void setDeviceId(Integer deviceId) {
      this.deviceId = deviceId;
   }

   public String getSoftwareVersion() {
      return this.softwareVersion;
   }

   public void setSoftwareVersion(String text) {
      this.softwareVersion = text;
   }

   public String getProtocolVersion() {
      return this.protocolVersion;
   }

   public void setProtocolVersion(String text) {
      this.protocolVersion = text;
   }

   public Integer getTxSize() {
      return this.txSize;
   }

   public void setTxSize(Integer txSize) {
      this.txSize = txSize;
   }

   public Integer getRxSize() {
      return this.rxSize;
   }

   public void setRxSize(Integer rxSize) {
      this.rxSize = rxSize;
   }

   public Integer getEncryptionType() {
      return this.encryptionType;
   }

   public void setEncryptionType(Integer encryptionType) {
      this.encryptionType = encryptionType;
   }

   public String getIdentifierOrInitKey() {
      return this.identifierOrInitKey;
   }

   public void setIdentifierOrInitKey(String id) {
      this.identifierOrInitKey = id;
   }

   public String getSoftwareVersionFields() {
      return this.softwareVersionFields;
   }

   public void setSoftwareVersionFields(String text) {
      this.softwareVersionFields = text;
   }

   public String getMultiPointCommId() {
      return this.multiPointCommId;
   }

   public void setMultiPointCommId(String id) {
      this.multiPointCommId = id;
   }

   public static void initInfo(Channel channel2, SessionInfo sessionInfo2) {
      if (sessionInfo2.getClient() == null) {
         throw new IllegalArgumentException("invalid own info (null client)");
      } else if (channel2.attr(OWN_INFO).setIfAbsent(sessionInfo2) != null) {
         throw new IllegalStateException("info already initialized");
      } else {
         SessionInfo sessionInfo3 = new SessionInfo();
         sessionInfo3.setClient(!sessionInfo2.getClient());
         channel2.attr(PEER_INFO).set(sessionInfo3);
      }
   }

   public static SessionInfo getOwnInfo(Channel channel2) {
      return getInfo(OWN_INFO, channel2);
   }

   public static SessionInfo getPeerInfo(Channel channel2) {
      return getInfo(PEER_INFO, channel2);
   }

   private static SessionInfo getInfo(AttributeKey<SessionInfo> attributeKey2, Channel channel2) {
      SessionInfo sessionInfo2 = (SessionInfo)channel2.attr(attributeKey2).get();
      if (sessionInfo2 != null) {
         return sessionInfo2;
      } else {
         throw new IllegalStateException("info not initialized");
      }
   }
}
