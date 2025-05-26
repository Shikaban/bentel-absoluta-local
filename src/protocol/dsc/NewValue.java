package protocol.dsc;

import com.google.common.base.Preconditions;

public class NewValue extends Message.Response {
   private final Object value;

   public <V> NewValue(Message<Void, V> var1, V var2) {
      this(var1, null, var2);
   }

   public <P, V> NewValue(Message<P, V> var1, P var2, V var3) {
      super((Message)Preconditions.checkNotNull(var1), var2);
      this.value = var3;
   }

   public <V> V getValue(Message<?, V> var1) {
      if (this.message != var1) {
         throw new IllegalArgumentException(String.format("unexpected message: %s instead of %s", var1, this.message));
      } else {
         return (V) this.value;
      }
   }

   public String toString() {
      return this.message + "(" + this.param + "): " + this.value;
   }
}
