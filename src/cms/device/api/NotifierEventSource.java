package cms.device.api;

public interface NotifierEventSource extends Model {
   DeviceOrPanel getParent();

   int getNumber();
}
