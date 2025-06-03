package protocol.dsc;

import com.google.common.base.Preconditions;

public class NewValue extends Message.Response {
   private final Object value;

   public <V> NewValue(Message<Void, V> message, V value) {
      this(message, null, value);
   }

   public <P, V> NewValue(Message<P, V> message, P param, V value) {
      super(Preconditions.checkNotNull(message), param);
      this.value = value;
   }

   @SuppressWarnings("unchecked")
   public <V> V getValue(Message<?, V> message) {
      if (this.message != message) {
         throw new IllegalArgumentException(
               String.format("unexpected message: %s instead of %s", message, this.message)
         );
      }
      return (V) this.value;
   }

   public String toString() {
      return this.message + "(" + this.param + "): " + this.value;
   }
}
