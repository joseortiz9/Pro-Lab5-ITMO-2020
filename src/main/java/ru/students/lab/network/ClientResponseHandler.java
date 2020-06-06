package ru.students.lab.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.students.lab.database.Credentials;
import ru.students.lab.database.CurrentUser;
import ru.students.lab.util.ListEntrySerializable;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.List;

public class ClientResponseHandler {

    private class ResponseReceiver extends Thread {

        protected volatile Object receivedObject = null;

        @Override
        public void run() {
            while (true) {
                try {
                    receiveData();
                } catch (ClosedChannelException ignored) {
                } catch (EOFException ex) {
                    System.err.println("Reached limit of data to receive");
                    LOG.error("Reached Limit", ex);
                } catch (IOException | ClassNotFoundException e) {
                    LOG.error("I/O Problems", e);
                }
            }
        }

        /**
         * Функция для получения данных
         */
        public void receiveData() throws IOException, ClassNotFoundException {
            final ByteBuffer buf = ByteBuffer.allocate(AbsUdpSocket.DATA_SIZE);
            final SocketAddress addressFromServer = channel.receiveDatagram(buf);
            buf.flip();

            byte[] bytes = new byte[buf.remaining()];
            buf.get(bytes);

            if (bytes.length < 1)
                return;

            synchronized (ClientResponseHandler.class) {
                channel.setRequestSent(false);
                if (bytes.length < AbsUdpSocket.DATA_SIZE)
                    receivedObject = processResponse(bytes);
                else
                    throw new EOFException();
            }
        }

        /**
         * Функция для десериализации полученных данных
         * @param petitionBytes - данные
         * @return obj - объект десериализованных данных
         */
        private Object processResponse(byte[] petitionBytes) throws IOException, ClassNotFoundException {
            try (ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(petitionBytes))) {
                final Object obj = stream.readObject();
                LOG.info("received object: " + obj);
                if (obj == null)
                    throw new ClassNotFoundException();
                return obj;
            }
        }
    }


    protected static final Logger LOG = LogManager.getLogger(ClientResponseHandler.class);
    private final ResponseReceiver receiverThread;
    private final ClientUdpChannel channel;
    private final CurrentUser currentUser;

    public ClientResponseHandler(ClientUdpChannel channel, CurrentUser currentUser) {
        this.channel = channel;
        this.currentUser = currentUser;
        LOG.info("starting receiver");
        receiverThread = new ResponseReceiver();
        receiverThread.setName("ClientReceiverThread");
        receiverThread.start();
    }

    public Object checkForResponse() {
        Object received = receiverThread.receivedObject;

        final long start = System.currentTimeMillis();
        while (channel.requestWasSent()) {
            if (channel.requestWasSent() && System.currentTimeMillis() - start > 1500) {
                LOG.error("Seems the server went down!");
                channel.setConnectionToFalse();
                return "The server is down, please check the connection";
            }
        }

        if (received != null) {
            channel.setConnected(true);
            /* In the case a weird user was put cheating, we logout */
            if (received instanceof Credentials && ((Credentials)received).id == -1) {
                //TODO: LOGOUT TO THE FIRST STAGE
            }
        }

        return received;
    }

    public void setReceivedObjectToNull() {
        synchronized (this) {
            receiverThread.receivedObject = null;
        }
    }

    public void setCurrentUser(Credentials credentials) {
        currentUser.setCredentials(credentials);
    }
    public CurrentUser getCurrentUser() {
        return currentUser;
    }

    /**
     * Функция для вывода объектов коллекции
     * @param obj- коллекция с объектами
     */
    public void printResponse(Object obj) throws ClassNotFoundException {
        if (obj instanceof String) {
            System.out.println(obj);
        }
        else if (obj instanceof List) {
            printList(obj);
        }
        else if (obj instanceof Credentials) {
            handleCredentialsResponse((Credentials) obj);
        }
        else
            throw new ClassNotFoundException();
    }

    private void handleCredentialsResponse(Credentials obj) {
        if (obj.id == -1) {
            currentUser.setCredentials(obj);
            System.out.println("Logged out! Weird behaviour checking your credentials");
            return;
        }
        currentUser.setCredentials(obj);
        System.out.println("Welcome back " + obj.username + "!");
    }


    private void printList(Object obj) {
        if (((List) obj).size() == 0) {
            System.out.println("Elements found: 0");
            return;
        }
        if (((List) obj).get(0) instanceof ListEntrySerializable) {
            ((List<ListEntrySerializable>) obj).stream().forEach(e -> System.out.println("key:" + e.getKey() + " -> " + e.getDragon().toString()));
            System.out.println("Elements found: "+ ((List) obj).size());
        }else {
            for (Object objFromScript: (List)obj) {
                if (objFromScript instanceof String)
                    System.out.println(objFromScript);
                else if (objFromScript instanceof List) {
                    ((List<ListEntrySerializable>) objFromScript).stream().forEach(e -> System.out.println("key:" + e.getKey() + " -> " + e.getDragon().toString()));
                    System.out.println("Elements found: "+ ((List) objFromScript).size());
                }
            }
        }
    }


    public void finishReceiver() {
        receiverThread.interrupt();
    }


    public ResponseReceiver getReceiver() {
        return receiverThread;
    }
}
