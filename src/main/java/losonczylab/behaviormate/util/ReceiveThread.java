package losonczylab.behaviormate.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * ?
 */
class ReceiveThread extends Thread {

    /**
     * ?
     */
    private final DatagramSocket sock;
    /**
     * ?
     */
    private final ConcurrentLinkedQueue<String> messageQueue;
    /**
     * ?
     */
    private boolean run;
    /**
     * ?
     */
    private final FileWriter mL;
    /**
     * ?
     */
    private Thread t;

    /**
     * ?
     *
     * @param sock ?
     * @param mL ?
     */
    ReceiveThread(DatagramSocket sock, FileWriter mL) {
        this.run = true;
        //boolean debug = false;
        this.mL = mL;
        this.sock = sock;
        try {
            this.sock.setSoTimeout(50);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        messageQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * ?
     *
     * @return ?
     */
    public String poll() {
        return messageQueue.poll();
    }

    public void run() {
        while (this.run) {
            byte[] receiveData = new byte[1024];
            DatagramPacket incomingUdp = new DatagramPacket(receiveData, receiveData.length);
            try {
                sock.receive(incomingUdp);
            } catch (IOException e) {
                continue;
            }

            String message = new String(incomingUdp.getData(), 0, incomingUdp.getLength());
            this.mL.write("[RECEIVE] " + message);
            messageQueue.add(message);
        }
        this.sock.close();
    }

    public void start() {
        if (t == null) {
            t = new Thread (this, "name " + System.nanoTime());
            t.start();
        }
    }

    public void stop_thread() {
        this.run = false;
    }

    public String getMessagesInQueue() {
        return Arrays.toString(messageQueue.toArray());
    }

    public void clearQueuedMessages() {
        messageQueue.clear();
    }
}