package protocol.dsc.errors;

public class WrongCommandLengthException extends DscProtocolException {
   public WrongCommandLengthException(int var1, String var2) {
      super(var1, var2);
   }

   public int getErrorCode() {
      return 1;
   }
}
