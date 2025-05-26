package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import protocol.dsc.base.DscSerializable;

import java.util.Iterator;
import java.util.List;

public abstract class DscRequestableCommand extends DscAbstractCommand implements DscResponse {
   protected abstract List<DscSerializable> getRequestFields();

   protected abstract List<DscSerializable> getOtherFields();

   protected final List<DscSerializable> getFields() {
      return ImmutableList.<DscSerializable>builder()
         .addAll(this.getRequestFields())
         .addAll(this.getOtherFields())
         .build();
   }

   public final void readFrom(ByteBuf var1) throws DecoderException, IndexOutOfBoundsException {
      super.readFrom(var1);
   }

   public final void readRequestDataFrom(ByteBuf var1) throws DecoderException, IndexOutOfBoundsException {
      Iterator var2 = this.getRequestFields().iterator();

      while(var2.hasNext()) {
         DscSerializable var3 = (DscSerializable)var2.next();
         var3.readFrom(var1);
      }

   }

   public final void writeTo(ByteBuf var1) {
      super.writeTo(var1);
   }

   public final void writeRequestDataTo(ByteBuf var1) {
      Iterator var2 = this.getRequestFields().iterator();

      while(var2.hasNext()) {
         DscSerializable var3 = (DscSerializable)var2.next();
         var3.writeTo(var1);
      }

   }

   public boolean match(DscRequestableCommand var1) {
      if (this.getClass() != var1.getClass()) {
         return false;
      } else {
         List<DscSerializable> var2 = this.getRequestFields();
         List<DscSerializable> var3 = var1.getRequestFields();
         int var4 = var2.size();
         if (var4 != var3.size()) {
            return false;
         } else {
            for(int var5 = 0; var5 < var4; ++var5) {
               if (!((DscSerializable)var2.get(var5)).isEquivalent((DscSerializable)var3.get(var5))) {
                  return false;
               }
            }

            return true;
         }
      }
   }
}
