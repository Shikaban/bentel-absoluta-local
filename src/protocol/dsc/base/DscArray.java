package protocol.dsc.base;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.Iterator;

public final class DscArray<T extends DscSerializable> extends ArrayList<T> implements DscSerializable {
   private final DscArray.ElementProvider<T> elementProvider;

   public DscArray(DscArray.ElementProvider<T> var1) {
      this.elementProvider = (DscArray.ElementProvider)Preconditions.checkNotNull(var1);
   }

   public void readFrom(ByteBuf var1) throws IndexOutOfBoundsException, DecoderException {
      int var2 = this.elementProvider.numberOfElements();
      this.clear();
      if (var2 >= 0) {
         this.ensureCapacity(var2);

         for(int var5 = 0; var5 < var2; ++var5) {
            T var4 = this.elementProvider.newElement();
            ((DscSerializable) var4).readFrom(var1);
            this.add(var4);
         }
      } else {
         while(var1.isReadable()) {
            T var3 = this.elementProvider.newElement();
            ((DscSerializable) var3).readFrom(var1);
            this.add(var3);
         }
      }

   }

   public void writeTo(ByteBuf var1) {
      Iterator var2 = this.iterator();

      while(var2.hasNext()) {
         T var3 = (T) var2.next();
         var3.writeTo(var1);
      }

   }

   public boolean isEquivalent(DscSerializable var1) {
      if (var1 instanceof DscArray) {
         int var2 = this.size();
         DscArray<?> var3 = (DscArray)var1;
         if (var2 != var3.size()) {
            return false;
         } else {
            for(int var4 = 0; var4 < var2; ++var4) {
               if (!((DscSerializable)this.get(var4)).isEquivalent((DscSerializable)var3.get(var4))) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public int getExpectedNumberOfElements() {
      return this.elementProvider.numberOfElements();
   }

   public T addNewElement() {
      T var1 = this.elementProvider.newElement();
      this.add(var1);
      return var1;
   }

   public interface ElementProvider<T> {
      int numberOfElements();

      T newElement();
   }
}
