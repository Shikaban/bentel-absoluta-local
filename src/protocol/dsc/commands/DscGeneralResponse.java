package protocol.dsc.commands;

public interface DscGeneralResponse extends DscResponse {
   boolean isSuccess();

   String getDescription();
}
