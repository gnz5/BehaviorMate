package losonczylab.behaviormate.util;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * ?
 */
public class UdpClient {
    /**
     * ?
     */
    private final SocketAddress arduinoAddress;

    /**
     * ?
     */
    private final DatagramSocket udpSocket;

    /**
     * ?
     */
    private final String address;

    /**
     * ?
     */
    private ReceiveThread rt;

    /**
     * ?
     */
    private String id;

    /**
     * ?
     */
    private FileWriter mL;

    /**
     * ?
     */
    private boolean status;

    /**
     * ?
     *
     * @param ip ?
     * @param arduinoPort ?
     * @param receivePort ?
     * @param id ?
     * @throws IOException ?
     */
    public UdpClient(String ip, int arduinoPort, int receivePort, String id) throws IOException {
        arduinoAddress = new InetSocketAddress(ip,arduinoPort);
        this.address = ip + ":" + receivePort;
        this.id = id;
        this.status = true;

        try {
            udpSocket = new DatagramSocket(receivePort);
        } catch (IOException e) {
            e.printStackTrace();
            this.status = false;
            throw new IOException("error connecting to " + this.address);
        }

        File log_directory = new File("logs");
        if (!log_directory.exists()) {
            log_directory.mkdirs();
        }
        mL = new FileWriter("logs/" + ip + "." + receivePort + ".log");
        rt = new ReceiveThread(udpSocket, mL);
        rt.start();
    }

    /**
     * ?
     *
     * @param ip ?
     * @param arduinoPort ?
     * @throws IOException ?
     */
    public UdpClient(String ip, int arduinoPort) throws IOException {
        arduinoAddress = new InetSocketAddress(ip,arduinoPort);
        this.address = ip;
        this.status = true;

        try {
            udpSocket = new DatagramSocket(null);
        } catch (IOException e) {
            e.printStackTrace();
            this.status = false;
            throw new IOException("error connecting to " + this.address);
        }
    }

    public String getAddress() {
        return address;
    }

    public String getId() {
        return id;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    /**
     * ?
     *
     * @param message ?
     */
    public void sendMessage(String message) {
      message = message.replaceAll("[\r|\n|\\s]", "");

      try {
          this.mL.write("[SEND] " + message);
          byte[] sendData = message.getBytes(StandardCharsets.UTF_8);
          DatagramPacket sendPacket = new DatagramPacket(sendData, 0,
            sendData.length, arduinoAddress);
          udpSocket.send(sendPacket);
      } catch (IOException e) {
          System.out.println("error sending to " + this.address + ": " + message);
          System.out.println(e);
      }
    }

    /**
     * ?
     *
     * @param json ?
     * @return ?
     */
    public boolean receiveMessage(JSONBuffer json) {
        String message = this.rt.poll();
        if (message != null) {
            json.json = new JSONObject();
            try {
                json.json.put(this.id, new JSONObject(message));
                //System.out.println(json.json.toString());
            } catch (RuntimeException | JSONException e) {
                System.out.println("[" + this.id + "] ERROR parsing message: " + message);
                return false;
            }

            return true;
        }

        return false;
    }

    public void printReceiveThreadMessages() {
        System.out.println(rt.getMessagesInQueue());
    }

    public void clearQueuedMessages() {
        rt.clearQueuedMessages();
    }

    public void closeSocket() {
        if (rt != null) {
            rt.stop_thread();
        } else {
            udpSocket.close();
        }
    }
}
