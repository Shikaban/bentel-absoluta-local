package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscArray;
import protocol.dsc.base.DscNumber;
import protocol.dsc.base.DscSerializable;
import protocol.dsc.base.DscString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AccessCodesResponse extends DscCommandWithResponse.Response<AccessCodes> implements DscArray.ElementProvider<DscString> {
   private final DscNumber accessCodeLength = DscNumber.newUnsignedNum(1);
   private final DscArray<DscString> codes = new DscArray(this);

   public AccessCodesResponse() {
      super(new AccessCodes());
   }

   protected List<DscSerializable> getResponseFields() {
      return ImmutableList.of(this.accessCodeLength, this.codes);
   }

   public int getCommandNumber() {
      return 18230;
   }

   public int getUserNumberStart() {
      return ((AccessCodes)this.requestInstance).getUserNumberStart();
   }

   public int getNumberOfUsers() {
      return ((AccessCodes)this.requestInstance).getNumberOfUsers();
   }

   public List<String> getCodes() {
      List<String> var1 = new ArrayList(this.codes.size());
      Iterator var2 = this.codes.iterator();

      while(var2.hasNext()) {
         DscString var3 = (DscString)var2.next();
         var1.add(var3.toString());
      }

      return var1;
   }

   public int numberOfElements() {
      return this.getNumberOfUsers();
   }

   public DscString newElement() {
      return DscString.newBCDString(this.accessCodeLength.toInt());
   }
}
