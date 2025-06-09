package protocol.dsc.base;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.Objects;

public final class DscOptional<T extends DscSerializable> implements DscSerializable {
   private final T object;
   private final DscOptional.PresenceProvider presenceProvider;

   public DscOptional(T object, DscOptional.PresenceProvider presenceProvider) {
      this.object = Preconditions.checkNotNull(object);
      this.presenceProvider = Preconditions.checkNotNull(presenceProvider);
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

   @Override
   public int hashCode() {
      return Objects.hash(this.get());
   }

   @Override
   public boolean equals(Object other) {
      if (other != null && this.getClass() == other.getClass()) {
         DscOptional<?> otherOpt = (DscOptional<?>)other;
         return Objects.equals(this.get(), otherOpt.get());
      } else {
         return false;
      }
   }

   @Override
   public String toString() {
      return Objects.toString(this.get());
   }

   public void readFrom(ByteBuf buf) throws IndexOutOfBoundsException, DecoderException {
      if (this.presenceProvider.isPresent()) {
         this.object.readFrom(buf);
      }
   }

   public void writeTo(ByteBuf buf) {
      if (this.presenceProvider.isPresent()) {
         this.object.writeTo(buf);
      }
   }

   public boolean isEquivalent(DscSerializable other) {
      if (other instanceof DscOptional) {
         DscOptional<?> otherOpt = (DscOptional<?>)other;
         if (this.isPresent() != otherOpt.isPresent()) {
               return false;
         } else {
               return this.isPresent() ? this.get().isEquivalent(otherOpt.get()) : true;
         }
      } else {
         return false;
      }
   }

   public interface PresenceProvider {
      boolean isPresent();
   }
}
