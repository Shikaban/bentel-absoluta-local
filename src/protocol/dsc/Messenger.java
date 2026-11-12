package protocol.dsc;

public interface Messenger {
   <V> void send(Message<Void, V> message2);

   <V> void send(Message<Void, V> message2, Priority priority2);

   <P, V> void send(Message<P, V> message2, P p2);

   <P, V> void send(Message<P, V> message2, P p2, Priority priority2);

   void addMessageListener(MessageListener messageListener2);

   void removeMessageListener(MessageListener messageListener2);
}
