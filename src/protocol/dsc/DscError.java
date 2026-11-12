package protocol.dsc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class DscError extends Message.Response {
   private final DscError.Type type;
   private final String description;
   private final Integer responseCode;

   private <P> DscError(DscError.Type type2, String text, Message<P, ?> message2, P p2, Integer responseCode) {
      super(message2, p2);
      this.type = (DscError.Type)Preconditions.checkNotNull(type2);
      this.description = (String)Preconditions.checkNotNull(text);
      this.responseCode = responseCode;
   }

   public static DscError newFatalError(String text) {
      return new DscError(DscError.Type.FATAL, text, (Message<Object, ?>)null, (Object)null, (Integer)null);
   }

   public static DscError newFatalError(Throwable exception) {
      return newFatalError(getDescription(exception));
   }

   public static DscError newGenericError(String text) {
      return new DscError(DscError.Type.GENERIC, text, (Message<Object, ?>)null, (Object)null, (Integer)null);
   }

   public static DscError newGenericError(Throwable exception) {
      return newGenericError(getDescription(exception));
   }

   public static <P> DscError newMessageError(Message<P, ?> message2, P p2, Integer integerValue, String text) {
      return new DscError(DscError.Type.MESSAGE, text, Preconditions.checkNotNull(message2), p2, integerValue);
   }

   public static <P> DscError newMessageError(Message<P, ?> message2, P p2, Throwable exception) {
      return newMessageError(message2, p2, (Integer)null, getDescription(exception));
   }

   public DscError.Type getType() {
      return this.type;
   }

   public String getDescription() {
      return this.description;
   }

   public Integer getResponseCode() {
      return this.responseCode;
   }

   public String toString() {
      switch(this.type) {
      case FATAL:
         return "Fatal error: " + this.description;
      case GENERIC:
         return "Generic error: " + this.description;
      case MESSAGE:
         return "Message related error for " + this.message + "(" + this.param + "): " + this.description + (this.responseCode != null ? String.format(" [response code: 0x%02X]", this.responseCode) : "");
      default:
         throw new AssertionError(this.type);
      }
   }

   private static String getDescription(Throwable exception) {
      return (String)MoreObjects.firstNonNull(exception.getMessage(), MoreObjects.firstNonNull(exception.toString(), exception.getClass().getSimpleName()));
   }

   public static enum Type {
      FATAL,
      GENERIC,
      MESSAGE;
   }
}
