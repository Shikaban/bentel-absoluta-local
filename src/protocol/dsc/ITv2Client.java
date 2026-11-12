package protocol.dsc;

public interface ITv2Client {
   void connect(ITv2Client.Callback callback2) throws InterruptedException;

   public interface Callback {
      void connected(Endpoint endpoint2);
   }
}
