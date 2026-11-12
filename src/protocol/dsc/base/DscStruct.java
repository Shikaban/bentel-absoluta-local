package protocol.dsc.base;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.List;

public abstract class DscStruct implements DscSerializable {
   protected abstract List<DscSerializable> getFields();

   public void readFrom(ByteBuf inputBuffer) throws DecoderException, IndexOutOfBoundsException {
      for (DscSerializable field : this.getFields()) {
         field.readFrom(inputBuffer);
      }
   }

   public void writeTo(ByteBuf buffer) {
      for (DscSerializable field : this.getFields()) {
         field.writeTo(buffer);
      }
   }

   public boolean isEquivalent(DscSerializable serializable) {
      if (this.getClass() == serializable.getClass()) {
         List<DscSerializable> fields = this.getFields();
         List<DscSerializable> items = ((DscStruct)serializable).getFields();
         int fieldCount = fields.size();
         if (fieldCount != items.size()) {
            return false;
         } else {
            for(int index = 0; index < fieldCount; ++index) {
               if (!((DscSerializable)fields.get(index)).isEquivalent((DscSerializable)items.get(index))) {
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
