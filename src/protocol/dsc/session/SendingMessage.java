package protocol.dsc.session;

import com.google.common.base.Preconditions;

import protocol.dsc.Message;
import protocol.dsc.Priority;

public class SendingMessage<P, V> {
   private final Message<P, V> message;
   private final P param;
   private final Priority priority;

   public SendingMessage(Message<P, V> message2, P p2, Priority priority2) {
      this.message = Preconditions.checkNotNull(message2);
      this.param = p2;
      this.priority = (Priority)Preconditions.checkNotNull(priority2);
   }

   public Message<P, V> getMessage() {
      return this.message;
   }

   public P getParam() {
      return this.param;
   }

   public Priority getPriority() {
      return this.priority;
   }

   public String toString() {
      return this.message + "(" + this.param + ") " + this.priority;
   }
}
