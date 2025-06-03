package protocol.dsc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class DscError extends Message.Response {
   private final DscError.Type type;
   private final String description;
   private final Integer responseCode;

   private <P> DscError(DscError.Type var1, String var2, Message<P, ?> var3, P var4, Integer var5) {
      super(var3, var4);
      this.type = (DscError.Type)Preconditions.checkNotNull(var1);
      this.description = (String)Preconditions.checkNotNull(var2);
      this.responseCode = var5;
   }

   public static DscError newFatalError(String var0) {
      return new DscError(DscError.Type.FATAL, var0, (Message<Object, ?>)null, (Object)null, (Integer)null);
   }

   public static DscError newFatalError(Throwable var0) {
      return newFatalError(getDescription(var0));
   }

   public static DscError newGenericError(String var0) {
      return new DscError(DscError.Type.GENERIC, var0, (Message<Object, ?>)null, (Object)null, (Integer)null);
   }

   public static DscError newGenericError(Throwable var0) {
      return newGenericError(getDescription(var0));
   }

   public static <P> DscError newMessageError(Message<P, ?> var0, P var1, Integer var2, String var3) {
      return new DscError(DscError.Type.MESSAGE, var3, Preconditions.checkNotNull(var0), var1, var2);
   }

   public static <P> DscError newMessageError(Message<P, ?> var0, P var1, Throwable var2) {
      return newMessageError(var0, var1, (Integer)null, getDescription(var2));
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

   private static String getDescription(Throwable var0) {
      return (String)MoreObjects.firstNonNull(var0.getMessage(), MoreObjects.firstNonNull(var0.toString(), var0.getClass().getSimpleName()));
   }

   public static enum Type {
      FATAL,
      GENERIC,
      MESSAGE;
   }
}
