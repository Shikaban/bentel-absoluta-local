package protocol.dsc.base;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.Objects;

public final class DscOptional<T extends DscSerializable> implements DscSerializable {
   private final T object;
   private final DscOptional.PresenceProvider presenceProvider;

   public DscOptional(T var1, DscOptional.PresenceProvider var2) {
      this.object = Preconditions.checkNotNull(var1);
      this.presenceProvider = (DscOptional.PresenceProvider)Preconditions.checkNotNull(var2);
   }

   public T get() {
      return this.isPresent() ? this.object : null;
   }

   public T getAnyway() {
      return this.object;
   }

   public boolean isPresent() {
      return this.presenceProvider.isPresent();
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.get()});
   }

   public boolean equals(Object var1) {
      if (var1 != null && this.getClass() == var1.getClass()) {
         DscOptional<?> var2 = (DscOptional)var1;
         return Objects.equals(this.get(), var2.get());
      } else {
         return false;
      }
   }

   public String toString() {
      return Objects.toString(this.get());
   }

   public void readFrom(ByteBuf var1) throws IndexOutOfBoundsException, DecoderException {
      if (this.presenceProvider.isPresent()) {
         this.object.readFrom(var1);
      }

   }

   public void writeTo(ByteBuf var1) {
      if (this.presenceProvider.isPresent()) {
         this.object.writeTo(var1);
      }

   }

   public boolean isEquivalent(DscSerializable var1) {
      if (var1 instanceof DscOptional) {
         DscOptional<?> var2 = (DscOptional)var1;
         if (this.isPresent() != var2.isPresent()) {
            return false;
         } else {
            return this.isPresent() ? this.get().isEquivalent(var2.get()) : true;
         }
      } else {
         return false;
      }
   }

   public interface PresenceProvider {
      boolean isPresent();
   }
}
