package protocol.dsc.errors;

public class WrongSequenceNumberException extends DscProtocolException {
   public WrongSequenceNumberException(int numericValue, String text) {
      super(numericValue, text);
   }

   public int getErrorCode() {
      return 3;
   }
}
