package protocol.dsc.errors;

public class WrongSequenceNumberException extends DscProtocolException {
   public WrongSequenceNumberException(int var1, String var2) {
      super(var1, var2);
   }

   public int getErrorCode() {
      return 3;
   }
}
