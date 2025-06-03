package protocol.dsc.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscSerializable;

import java.util.List;

public abstract class DscCommandWithResponse extends DscCommandWithAppSeq {
   protected final boolean matchAsNotGeneralResponse(DscResponse var1) {
      if (!(var1 instanceof DscCommandWithResponse.Response)) {
         return false;
      } else {
         DscCommandWithResponse var2 = ((DscCommandWithResponse.Response<?>)var1).requestInstance;
         return this.getClass() == var2.getClass() && this.getFields().equals(var2.getFields());
      }
   }

   public abstract static class Response<C extends DscCommandWithResponse> extends DscAbstractCommand implements DscResponse {
      protected final C requestInstance;

      protected Response(C var1) {
         this.requestInstance = Preconditions.checkNotNull(var1);
      }

      protected abstract List<DscSerializable> getResponseFields();

      protected final List<DscSerializable> getFields() {
         return ImmutableList.<DscSerializable>builder()
            .addAll(this.requestInstance.getFields())
            .addAll(this.getResponseFields())
            .build();
      }
   }
}
