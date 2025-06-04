package protocol.dsc.base;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;

public final class DscArray<T extends DscSerializable> extends ArrayList<T> implements DscSerializable {
   private final ElementProvider<T> elementProvider;

   public DscArray(ElementProvider<T> elementProvider) {
      this.elementProvider = Preconditions.checkNotNull(elementProvider);
   }

   @Override
   public void readFrom(ByteBuf buffer) throws IndexOutOfBoundsException, DecoderException {
      int expectedElements = this.elementProvider.numberOfElements();
      this.clear();
      if (expectedElements >= 0) {
         this.ensureCapacity(expectedElements);

         for (int i = 0; i < expectedElements; ++i) {
               T element = this.elementProvider.newElement();
               element.readFrom(buffer);
               this.add(element);
         }
      } else {
         while (buffer.isReadable()) {
               T element = this.elementProvider.newElement();
               element.readFrom(buffer);
               this.add(element);
         }
      }
   }

   @Override
   public void writeTo(ByteBuf buffer) {
      for (T element : this) {
         element.writeTo(buffer);
      }
   }

   @Override
   public boolean isEquivalent(DscSerializable other) {
      if (other instanceof DscArray) {
         DscArray<?> otherArray = (DscArray<?>) other;
         if (this.size() != otherArray.size()) {
               return false;
         }
         for (int i = 0; i < this.size(); ++i) {
               if (!this.get(i).isEquivalent((DscSerializable) otherArray.get(i))) {
                  return false;
               }
         }
         return true;
      }
      return false;
   }

   public int getExpectedNumberOfElements() {
      return this.elementProvider.numberOfElements();
   }

   public T addNewElement() {
      T element = this.elementProvider.newElement();
      this.add(element);
      return element;
   }

   public interface ElementProvider<T> {
      int numberOfElements();
      T newElement();
   }
}
