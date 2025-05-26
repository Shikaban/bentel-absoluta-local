package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscString;

import java.util.List;

public class OpenSession extends DscCommandWithAppSeq {
   private final DscNumber deviceTypeOrVendorID = DscNumber.newUnsignedNum(1);
   private final DscNumber deviceId = DscNumber.newUnsignedNum(2);
   private final DscString softwareVersion = DscString.newBCDString(2);
   private final DscString protocolVersion = DscString.newBCDString(2);
   private final DscNumber txSize = DscNumber.newUnsignedNum(2);
   private final DscNumber rxSize = DscNumber.newUnsignedNum(2);
   private final DscNumber unused = DscNumber.newUnsignedNum(2).set(1L);
   private final DscNumber encryptionType = DscNumber.newUnsignedNum(1);

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.deviceTypeOrVendorID, this.deviceId, this.softwareVersion, this.protocolVersion, this.txSize, this.rxSize, this.unused, this.encryptionType);
   }

   public int getCommandNumber() {
      return 1546;
   }

   public int getDeviceTypeOrVendorID() {
      return this.deviceTypeOrVendorID.toInt();
   }

   public void setDeviceTypeOrVendorID(int var1) {
      this.deviceTypeOrVendorID.set((long)var1);
   }

   public int getDeviceId() {
      return this.deviceId.toInt();
   }

   public void setDeviceId(int var1) {
      this.deviceId.set((long)var1);
   }

   public String getSoftwareVersion() {
      return this.softwareVersion.toString();
   }

   public void setSoftwareVersion(String var1) {
      this.softwareVersion.setString(var1);
   }

   public String getProtocolVersion() {
      return this.protocolVersion.toString();
   }

   public void setProtocolVersion(String var1) {
      this.protocolVersion.setString(var1);
   }

   public int getTxSize() {
      return this.txSize.toInt();
   }

   public void setTxSize(int var1) {
      this.txSize.set((long)var1);
   }

   public int getRxSize() {
      return this.rxSize.toInt();
   }

   public void setRxSize(int var1) {
      this.rxSize.set((long)var1);
   }

   public int getEncryptionType() {
      return this.encryptionType.toInt();
   }

   public void setEncriptionType(int var1) {
      this.encryptionType.set((long)var1);
   }

   public String toString() {
      return String.format("%s [dev type/vend id: %02X, dev id: %04X, sw ver: %s, prot ver: %s, tx size: %04X, rx size: %04X, enc type: %02X]", super.toString(), this.getDeviceTypeOrVendorID(), this.getDeviceId(), this.getSoftwareVersion(), this.getProtocolVersion(), this.getTxSize(), this.getRxSize(), this.getEncryptionType());
   }
}
