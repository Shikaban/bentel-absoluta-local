package protocol.dsc;

public interface ITv2Client {
   void connect(ITv2Client.Callback var1) throws InterruptedException;

   public interface Callback {
      void connected(Endpoint var1);
   }
}
