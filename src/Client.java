
import javax.sound.sampled.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Client extends Thread {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;
    private ClientGUI clientGUI;
    private String serverAddress;
    private String username;
    private int serverPort;

    Client(String serverAddress, int serverPort, String username, ClientGUI clientGUI) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.username = username;
        this.clientGUI = clientGUI;

    }

    /*
     * Stworzenie gniazdka klienta, strumieni wejścia i wyjścia, uruchomienie wątku, wysłanie imienia klienta na
     * strumień wyjścia.
     * Dane zostaną odczytane na serwerze.
     */
    boolean startDialog() {
        try {
            socket = new Socket(serverAddress, serverPort);
        } catch (Exception ec) {
            display("Błąd w połączeniu z serwerem:" + ec);
            return false;
        }

        String msg = "Nawiązano połączenie z serwerem " + socket.getInetAddress() + ":" + socket.getPort();
        display(msg);

        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eIO) {
            display("Wyjątek przy tworzeniu strumieni I/O: " + eIO);
            return false;
        }

        this.start();
        try {
            sOutput.writeObject(username);
        } catch (IOException eIO) {
            display("Wyjątek podczas logowania: " + eIO);
            disconnect();
            return false;
        }
        return true;
    }

    /*
     * Wyświetlenie argumentu funkcji w Chacie Klienta.
     */
    private void display(String msg) {
        clientGUI.AddTextToChat(msg + "\n");
    }

    /*
     * Wysłanie wiadomości z poziomu klienta na strumien wyjścia.
     */
    void sendMessage(String msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            display("Wyjątek podczas wysyłania: " + e);
        }
    }

    /*
     * Zamknięcie strumieni wejścia, wyjścia oraz gniazdka klienta.
     * Wywołanie metody disconnect obiektu klasy ClientGUI w celu ustawienia odpowiednich parametrów komponentów.
     */
    private void disconnect() {
        try {
            if (sInput != null)
                sInput.close();
        } catch (Exception ignored) {
        }
        try {
            if (sOutput != null)
                sOutput.close();
        } catch (Exception ignored) {
        }
        try {
            if (socket != null)
                socket.close();
        } catch (Exception ignored) {
        }

        clientGUI.disconnected();

    }

    /*
     * Odtwarzanie sygnału przychodzącej wiadomości prywatnej.
     */
    private static void PlayMessageSound() {
        try {
            AudioInputStream inputStream = AudioSystem
                    .getAudioInputStream(Client.class.getClass().getResource("/resources/messageSound.wav"));
            Clip clip = AudioSystem.getClip();
            clip.open(inputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /*
     * Metoda run - praca wątku.
     * Na strumień wejścia gniazdka klienta przysyłane są wiadomości.
     * W zależności od kodu, który zawarty jest (bądz nie) w wiadomości są one odczytywane jako prywatne, ogólne lub
     * jako odświeżona lista użytkowników.
     * Następnie kierowane są one w odpowiednie miejsca w GUI klienta.
     */
    @Override
    public void run() {
        while (true) {
            try {
                String msg = (String) sInput.readObject();

				/*
                  Jeżeli wiadomość zawiera odświeżoną listę klientów to usuwa kod, następuje zamiana stringa na tablicę
				  stringów (oddzielenie po spacji) - klientów z numerem id.
				  Następnie lista klientów wyświetlana jest w odpowiednim miejscu GUI klienta.
				 */
                if (msg.contains("681")) {
                    String result = msg.replaceAll("^[6]{1}[8]{1}[1]{1}", "");
                    String[] lines = result.split("\n");
                    ArrayList<String> arrayList;
                    arrayList = new ArrayList<String>(Arrays.asList(lines));
                    clientGUI.AddTextToOnlineUsers(arrayList);
                } else {
					/*
					 Jeżeli wiadomość prywatna to usuwa kod i wyświetla w odpowiedni sposób tekst w okienku chatu GUI
					 klienta i odtwarza sygnał dzwiekowy (jeżeli włączony)
					 */
                    if (msg.contains("997812")) {
                        msg = msg.replaceAll("^[9]{1}[9]{1}[7]{1}[8]{1}[1]{1}[2]{1}", "");
                        clientGUI.AddTextToChat(msg);
                        if (clientGUI.isSoundOn)
                            PlayMessageSound();
                    } else {
                        clientGUI.AddTextToChat(msg);
                    }

                }

            } catch (IOException e) {
                display("Rozłączono z serwerem: " + e);
                clientGUI.disconnected();
                break;
            } catch (ClassNotFoundException ignored) {
            }
        }
    }

}
