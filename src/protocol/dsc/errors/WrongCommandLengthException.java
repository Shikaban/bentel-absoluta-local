package protocol.dsc.errors;

public class WrongCommandLengthException extends DscProtocolException {
   public WrongCommandLengthException(int numericValue, String text) {
      super(numericValue, text);
   }

   public int getErrorCode() {
      return 1;
   }
}
