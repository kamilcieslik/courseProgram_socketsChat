
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Server extends Thread {
    private static int iteratedKeyForClientThreadIp;
    private ArrayList<ClientThread> listOfClientThreads;
    private ServerGUI serverGUI;
    private SimpleDateFormat simpleDateFormat;
    private int port;
    private boolean keepGoing;
    private List<BannedIp> listOfBannedIp = new ArrayList<>();
    private String fileNameBannedIp = "bannedIp.bin";

    Server(int port, ServerGUI serverGUI) throws ClassNotFoundException {

        this.serverGUI = serverGUI;
        this.port = port;
        simpleDateFormat = new SimpleDateFormat("[HH:mm:ss]");
        listOfClientThreads = new ArrayList<>();
        LoadBannedIp();
    }

    /*
     * Stworzenie gniazdka serwera na odpowiednim porcie.
     * Dopóki inna funkcja - stopServer() nie zmieni parametru keepGoing na false to gniazdko serwera jest w trybie
     * nasłuchu - oczekuje na połączenia.
     * Tworzony jest wątek klienta, następuje jego dodanie do listy wątków klienta oraz uruchamiany jest jego wątek.
     */
    @Override
    public void run() {
        keepGoing = true;
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            while (keepGoing) {
                display("Serwer oczekuje na połączenia na porcie " + port + ".");

                Socket socket = serverSocket.accept();

                if (!keepGoing)
                    break;
                ClientThread t = new ClientThread(socket);

                listOfClientThreads.add(t);
                t.start();

            }
            /*
              Jeżeli (keepGoing==false) to zamknięcie gniazdka serwera i wszystkich gniazdek z tablicy wątków
			  klientów i ich strumieni wejścia/wyjścia.
			 */
            try {
                serverSocket.close();
                for (ClientThread tc : listOfClientThreads) {
                    try {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    } catch (IOException ignored) {
                    }
                }
            } catch (Exception e) {
                display("Wyjątek przy zamykaniu serwera i klientów: " + e);
            }
        } catch (IOException e) {
            String msg = simpleDateFormat.format(new Date()) + " " + e;
            serverGUI.SetEditableTextFieldWithServerPort(true);
            display(msg);
        }
    }

    /*
      Zatrzymanie pracy serwera. Komentarz wyżej opisuje działanie.
     */
    void stopServer() {
        keepGoing = false;
    }

    /*
      Wyświetlenie argumentu funkcji w Chacie na Serwerze.
      @param msg
     */
    private void display(String msg) {
        String time = simpleDateFormat.format(new Date()) + " " + msg;
        serverGUI.AddTextToEventLog(time + "\n");
    }

    /*
      Nadanie wiadomości ogólnej - do wszystkich wątków klienta.
     */
    private void broadcast(int senderId, String username, String message) {

        String time = simpleDateFormat.format(new Date());
        String finishedMessage = time + " ID: " + senderId + " " + username + ": " + message + "\n";
        serverGUI.AddTextToChat(finishedMessage);
        for (int i = listOfClientThreads.size(); --i >= 0; ) {
            ClientThread ct = listOfClientThreads.get(i);
            if (!ct.writeMessage(finishedMessage)) {
                listOfClientThreads.remove(i);
                display("Rozłączonego klienta " + ct.username + " usunięto z listy.");
            }
        }
    }

    /*
      Funkcja wywołująca listę klientów - jej wynik przekazywany jest do klientów.
     */
    private String ListOfUsers() {
        StringBuilder tmp = new StringBuilder("681"); //Wiadomość zakodowana aby z poziomu klienta wiadome było że to
        // lista klientów.
        for (ClientThread ctx : listOfClientThreads) {
            tmp.append("ID: ").append(ctx.id).append(" ").append(ctx.username).append("\n");
        }
        return tmp.toString();
    }

    /*
     * Odświeżenie listy klientow w poziomie serwera.
     */
    private void RefreshListOfUsersWithIPAddress() {
        StringBuilder tmp = new StringBuilder();
        for (ClientThread ctx : listOfClientThreads) {
            tmp.append("ID: ").append(ctx.id).append(" ").append(ctx.username).append(" IP: ").append(ctx.socket
                    .getInetAddress().getHostAddress()).append("\n");
        }
        serverGUI.RefreshOnlineUsersWithIPAddress(tmp.toString());

    }

    //Usunięcie odpowiedniego wątku klienta z tablicy wątków klientów w serwerze.
    private synchronized void remove(int id) {
        for (int i = 0; i < listOfClientThreads.size(); ++i) {
            ClientThread ct = listOfClientThreads.get(i);
            if (ct.id == id) {
                listOfClientThreads.remove(i);
                return;
            }
        }
    }

    /*
     * Nadawanie wiadomości prywatnej.
     * Wyszukanie odpowiedniego klienta po numerze id.
     * Przesyłanie odpowiedniej wiadomości do chatu serwera oraz do klienta.
     * Jeżeli (recipientId==senderId) to wyświetlony odpowiedni komunikat u wysyłającego i w chacie serwera.
     */
    private void privateBroadcast(int recipientId, int senderId, String username, String message) {
        String time = simpleDateFormat.format(new Date());
        String recipientUsername = null;

        if (recipientId != senderId) {
            for (int i = 0; i < listOfClientThreads.size(); ++i) {
                ClientThread ct = listOfClientThreads.get(i);
                if (ct.id == recipientId) {
                    recipientUsername = ct.username;
                    String finishedMessage = time + " Prywatna Wiadomość Od ID: " + senderId + " " + username + " Do " +
                            "ID: "
                            + recipientId + " " + recipientUsername + ": " + message + "\n";
                    serverGUI.AddTextToChat(finishedMessage);
                    finishedMessage = "997812" + time + " Prywatna Wiadomość Od ID: " + senderId + " " + username + ": "
                            + message //Wiadomość zakodowana aby z poziomu klienta wiadome było że jest prywatna
                            + "\n";
                    if (!ct.writeMessage(finishedMessage)) {
                        listOfClientThreads.remove(i);
                        display("Rozłączonego klienta " + ct.username + " usunięto z listy.");
                    }
                }
            }
            for (int i = 0; i < listOfClientThreads.size(); ++i) {
                ClientThread ct = listOfClientThreads.get(i);
                if (ct.id == senderId) {
                    String finishedMessage = time + " Prywatna Wiadomość Do ID: " + recipientId + " " +
                            recipientUsername
                            + ": " + message + "\n";
                    if (!ct.writeMessage(finishedMessage)) {
                        listOfClientThreads.remove(i);
                        display("Rozłączonego klienta " + ct.username + " usunięto z listy.");
                    }
                }
            }
        } else {
            for (int i = 0; i < listOfClientThreads.size(); ++i) {
                ClientThread ct = listOfClientThreads.get(i);
                if (ct.id == recipientId) {
                    String finishedMessage = time + " ID: " + senderId + " " + username + " napisać do siebie: " +
                            message
                            + "\n";
                    serverGUI.AddTextToChat(finishedMessage);
                    finishedMessage = time + " Prywatna Wiadomość Od Ciebie: " + message + "\n";
                    if (!ct.writeMessage(finishedMessage)) {
                        listOfClientThreads.remove(i);
                        display("Rozłączonego klienta " + ct.username + " usunięto z listy.");
                    }
                }
            }
        }

    }

    /*
      Dodanie ip do listy zbanowanych IP.
     */
    void AddIPToBannedIP(String ip, String reason) {
        listOfBannedIp.add(new BannedIp(ip, reason));
    }

    /*
      Zapisanie obiektów klasy zbanowaneIP do pliku.
     */
    void SaveBannedIp() {
        try {
            ObjectOutputStream osBannedIp = new ObjectOutputStream(new FileOutputStream(fileNameBannedIp));
            osBannedIp.writeObject(listOfBannedIp);
            osBannedIp.close();
        } catch (IOException e) {
            display("Wyjątek przy zapisie pliku ze zbanowanymi IP: " + e);

        }
    }

    /*
      Odczytanie obiektów klasy zbanowaneIP z pliku.
     */
    @SuppressWarnings("unchecked")
    private void LoadBannedIp() throws ClassNotFoundException {
        try {
            ObjectInputStream isBannedIp = new ObjectInputStream(new FileInputStream(fileNameBannedIp));
            listOfBannedIp = (List<BannedIp>) isBannedIp.readObject();
            isBannedIp.close();
        } catch (IOException e) {
            display("Wyjątek przy odczycie pliku ze zbanowanymi IP: " + e);
        }
    }

    /*
     * Sprawdzanie czy IP jest na liście zbanowanych.
     */
    private String IsBannedIp(ClientThread ct) {
        for (BannedIp bndIp : listOfBannedIp) {
            if (ct.socket.getInetAddress().getHostAddress().contains(bndIp.getIp())) {
                return bndIp.getReason();
            }
        }
        return null;
    }

    /*
     * Funkcja potrzebna do wyświetlania IP obiektu zbanowanegoIP jeżeli nastąpi wykrycie zbanowanego IP.
     */
    private String GetBannedIp(ClientThread ct) {
        for (BannedIp bndIp : listOfBannedIp) {
            if (ct.socket.getInetAddress().getHostAddress().contains(bndIp.getIp())) {
                return bndIp.getIp();
            }
        }
        return "Błąd przy odczycie.";
    }

    /*
      Odblokowanie zbanowanego IP.
     */
    void UnlockIPAddress(String ip) {
        for (int i = 0; i < listOfBannedIp.size(); ++i) {
            BannedIp bndIp = listOfBannedIp.get(i);
            if (bndIp.getIp().contains(ip)) {
                listOfBannedIp.remove(i);
                break;
            }
        }

    }

    /*
      Wątek klienta.
     */
    class ClientThread extends Thread {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;

        ClientThread(Socket socket) {

			/*
			 Przypisywanie IP następuje jak w kolumnach ID w bazach danych po dodaniu prefiksu [Key] - iterowanie id
			 - każdy następny obiekt ma wyższe ip.
			 */
            id = ++iteratedKeyForClientThreadIp;
            this.socket = socket;

			/*
			 Tworzenie strumieni wejścia, wyjścia, odczytanie imienia ze strumienia wejścia - w Kliencie zostało
			 wysłane na strumien wyjścia po pomyślnym stworzeniu gniazda klienta
			 */
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
                if (IsBannedIp(this) == null) {
                    display("ID: " + id + " " + username + " połączył się z serwerem.");
                } else {
                    display("ID: " + id + " " + username + " próbował połączyć się z serwerem ze zbanowanego IP: "
                            + GetBannedIp(this));
                }
            } catch (IOException e) {
                display("Wyjątek przy tworzeniu strumieni I/O: " + e);
            } catch (ClassNotFoundException ignored) {
            }
        }

        /*
          Tutaj dzieje się cała magia odczytywania wiadomości i odpowiedniego ich przekierowywania.
         */
        @Override
        public void run() {
            boolean keepGoing = true;
            while (keepGoing) {

				/*
				 Jeżeli wątek ma zbanowane ip..
				 */
                if (IsBannedIp(this) != null) {
                    this.writeMessage("Zostałeś zbanowany/a.\nPowód: " + IsBannedIp(this) + "\n");
                    display("ID: " + id + " " + username + " został zbanowany.\nPowód: " + IsBannedIp(this));
                    remove(id);
                    break;

					/*
					  W innym wypadku odbywa się odczytywanie wiadomości i odpowiednie ich przekierowywanie.
					 */
                } else {

					/*
			 		 Odświeżenie listy klientow w serwer GUI - teraz już można bo wiadomo,
			 		 że klient nie jest zbanowany.
			 		*/
                    RefreshListOfUsersWithIPAddress();

					/*
					 Wysłanie wszystkim klientom aktualnej listy klientów.
					 */
                    for (int i = listOfClientThreads.size(); --i >= 0; ) {
                        ClientThread ct = listOfClientThreads.get(i);
                        if (!ct.writeMessage(ListOfUsers())) {
                            listOfClientThreads.remove(i);
                            display("Rozłączonego klienta " + ct.username + " usunięto z listy.");
                        }
                    }
                    String message;
                    try {
						/*
						  Odczytanie wiadomości ze strumienia wejscia watku klienta.
						 */
                        message = (String) sInput.readObject();
                    } catch (IOException e) {
                        display(username + " Wyj�tek przy odczytywaniu ze strumienia wej�cia: " + e);
                        break;
                    } catch (ClassNotFoundException e2) {
                        break;
                    }

					/*
					 Jeżeli wiadomość prywatna to wysyłana do wątku o odpowiednim id.
					 */
                    if (message.contains("328")) {
                        message = message.replaceAll("^[3]{1}[2]{1}[8]{1}\\s{1}", "");
                        String userId = message.substring(0, message.indexOf(" "));
                        message = message.replaceAll("^" + userId, "");
                        privateBroadcast(Integer.parseInt(userId), id, username, message);
                    } else

                    {
						/*
						 Jeżeli wiadomość zakodowana (logout) to jest to ostatni przebieg pętli while i po wyjsciu z
						 niej nastepuje rozlaczenie klienta.
						 */
                        if (!message.equals("logout")) { //
                            broadcast(id, username, message);
                        } else if (message.equals("logout")) {
                            display("ID: " + id + " " + username + " rozłączył się z serwerem.");
                            keepGoing = false;
                        }

						/*
						 Wysłanie wszysktim klientom aktualnej listy klientow.
						 */
                        for (int i = listOfClientThreads.size(); --i >= 0; ) {
                            ClientThread ct = listOfClientThreads.get(i);
                            if (!ct.writeMessage(ListOfUsers())) {
                                listOfClientThreads.remove(i);
                                display("Roz��czonego klienta " + ct.username + " usuni�to z listy.");
                            }
                        }
                    }
                }
            }
            remove(id);

			/*
			  Odświeżenie listy klientow w serwer GUI - teraz już można bo wiadomo,
			  że klient nie jest zbanowany.
			 */
            RefreshListOfUsersWithIPAddress();

			/*
			  Wysłanie wszysktim klientom aktualnej listy
		      klientow - po usunięciu obecnego klienta.
			 */
            for (int i = listOfClientThreads.size(); --i >= 0; ) { //
                ClientThread ct = listOfClientThreads.get(i);
                if (!ct.writeMessage(ListOfUsers())) {
                    listOfClientThreads.remove(i);
                    display("Rozłączonego klienta " + ct.username + " usunięto z listy.");
                }
            }
            close();
        }

        /*
         * Zamknięcie strumieni wejścia, wyjścia oraz gniazdka klienta.
         */
        private void close() {
            try {
                if (sOutput != null)
                    sOutput.close();
            } catch (Exception ignored) {
            }
            try {
                if (sInput != null)
                    sInput.close();
            } catch (Exception ignored) {
            }
            try {
                if (socket != null)
                    socket.close();
            } catch (Exception ignored) {
            }
        }

        /*
         * Wysłanie wiadomości na strumien wyjścia wątku klienta.
         */
        private boolean writeMessage(String message) {
            if (!socket.isConnected()) {
                close();
                return false;
            }
            try {
                sOutput.writeObject(message);
            } catch (IOException e) {
                display("Błąd przy wysyłaniu wiadomości do " + username);
                display(e.toString());
            }
            return true;
        }
    }
}
