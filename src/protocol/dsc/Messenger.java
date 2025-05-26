package protocol.dsc;

public interface Messenger {
   <V> void send(Message<Void, V> var1);

   <V> void send(Message<Void, V> var1, Priority var2);

   <P, V> void send(Message<P, V> var1, P var2);

   <P, V> void send(Message<P, V> var1, P var2, Priority var3);

   void addMessageListener(MessageListener var1);

   void removeMessageListener(MessageListener var1);
}
