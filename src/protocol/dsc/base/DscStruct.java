package protocol.dsc.base;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.List;

public abstract class DscStruct implements DscSerializable {
   protected abstract List<DscSerializable> getFields();

   public void readFrom(ByteBuf var1) throws DecoderException, IndexOutOfBoundsException {
      for (DscSerializable field : this.getFields()) {
         field.readFrom(var1);
      }
   }

   public void writeTo(ByteBuf var1) {
      for (DscSerializable field : this.getFields()) {
         field.writeTo(var1);
      }
   }

   public boolean isEquivalent(DscSerializable var1) {
      if (this.getClass() == var1.getClass()) {
         List<DscSerializable> var2 = this.getFields();
         List<DscSerializable> var3 = ((DscStruct)var1).getFields();
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
      } else {
         return false;
      }
   }
}
