package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscCharsets;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscVariableBytes;

import java.nio.charset.Charset;
import java.util.List;

public class RequestAccess extends DscCommandWithAppSeq {
   private static final Charset IDENTIFIER_CHARSET;
   private final DscVariableBytes identifier = new DscVariableBytes();

   protected List<DscSerializable> getFields() {
      return ImmutableList.of(this.identifier);
   }

   public int getCommandNumber() {
      return 1550;
   }

   public DscVariableBytes identifier() {
      return this.identifier;
   }

   public String getIdentifier() {
      return this.identifier.toString(IDENTIFIER_CHARSET);
   }

   public void setIdentifier(String var1) {
      this.identifier.setString(IDENTIFIER_CHARSET, var1);
   }

   static {
      IDENTIFIER_CHARSET = DscCharsets.BCD;
   }
}
