package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscString;

import java.util.List;

public class SoftwareVersion extends DscAbstractCommand {
   private final DscString versionFields = DscString.newBCDString(11);

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.versionFields);
   }

   public int getCommandNumber() {
      return 1549;
   }

   public String getVersionFields() {
      return this.versionFields.toString();
   }

   public void setVersionFields(String var1) {
      this.versionFields.setString(var1);
   }
}
