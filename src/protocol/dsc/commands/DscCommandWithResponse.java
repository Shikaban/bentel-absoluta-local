package protocol.dsc.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import protocol.dsc.base.DscSerializable;

import java.util.List;

public abstract class DscCommandWithResponse extends DscCommandWithAppSeq {
   protected final boolean matchAsNotGeneralResponse(DscResponse response2) {
      if (!(response2 instanceof DscCommandWithResponse.Response)) {
         return false;
      } else {
         DscCommandWithResponse commandWithResponse = ((DscCommandWithResponse.Response<?>)response2).requestInstance;
         return this.getClass() == commandWithResponse.getClass() && this.getFields().equals(commandWithResponse.getFields());
      }
   }

   public abstract static class Response<C extends DscCommandWithResponse> extends DscAbstractCommand implements DscResponse {
      protected final C requestInstance;

      protected Response(C c2) {
         this.requestInstance = Preconditions.checkNotNull(c2);
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
