package twins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Server {

    private final int port;

    private Writer writer;

    /**
     * Initialise a new Twins server. To start the server, call start().
     *
     * @param port the port number on which the server will listen for
     * connections
     */
    public Server(int port) {
        this.port = port;
    }

    /**
     * Start the server.
     *
     * @throws IOException
     */
    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                System.out.println("Server listening on port: " + port);
                Socket conn = serverSocket.accept();
                System.out.println("Connected to " + conn.getInetAddress() + ":" + conn.getPort());
                session(conn);
            }
        }
    }

    /**
     * Run a Twins protocol session over an established network connection.
     *
     * @param connection the network connection
     * @throws IOException
     */
    public void session(Socket connection) throws IOException {

        /*Database txt.file START*/
        File database = new File("TwinsList.txt");
        if (!database.exists()) {
            database.createNewFile();
        } else {
            System.out.println("File already exists");
        }

        Map<String, String> twinsMap = new HashMap<String, String>();
        ArrayList<String> twinsList = new ArrayList<String>();
        String twinsString = "";

        String line;
        BufferedReader readerTxt = new BufferedReader(new FileReader(database));
        FileWriter fileWriterTxt = new FileWriter(database, true);
        PrintWriter writerTxt = new PrintWriter(fileWriterTxt);
        while ((line = readerTxt.readLine()) != null) {
            String[] parts = line.split("=", 2);
            if (parts.length >= 2) {
                String key = parts[0];
                String value = parts[1];
                twinsMap.put(key, value);
            } else {
                System.out.println("ignoring line: " + line);
            }
        }

        for (String key : twinsMap.keySet()) {
            System.out.println(key + ":" + twinsMap.get(key));
        }
        readerTxt.close();
        /*Database txt.file END*/

        while (true) {
            writer = new OutputStreamWriter(connection.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            // TODO: replace this with the actual protocol logic
            
            //STATEMENT NEW
            String helloMsg = reader.readLine();

            if (helloMsg == null) {
                connection.close();
                break;
            }
            helloMsg = helloMsg.trim();

            try {
                if ("hello".equalsIgnoreCase(helloMsg)) {
                    sendMessage("What is your name?");
                } else {
                    throw new Exception();
                }

            } catch (Exception e) {
                //System.out.println("Error 0");
                //System.out.println("Other case is wrong");
                sendMessage("Error 0");
                connection.close();
                break;
            } finally {
                //System.out.println("The 'try catch' of Error 0 is finished.");
            }

            //STATEMENT RECEIVE_NAME
            String nameMsg = reader.readLine();

            if (nameMsg == null) {
                connection.close();
                break;
            }

            if (!nameMsg.matches("^\\s*$")) {
                //if the name does not consist only of whitespace(s),then remove the leading and trailing whitespaces
                nameMsg = nameMsg.trim();
            }

            String dateMsg;

            try {
                //name is an no empty string and can not contain "=" because "=" is the symbol which split the name from the date
                //so if we let the user to write "=", the data will be splitted in a wrong way
                if (!nameMsg.isEmpty() && !nameMsg.contains("=")) {
                    //try {
                    dateMsg = twinsMap.get(nameMsg);
                    if (twinsMap.containsKey(nameMsg)) {
                        //throw new Exception(); 
                        //For multiple-threaded server (uncomment the lines 147,150,257-267 and put in comments the lines 152-162)
                        for (String key : twinsMap.keySet()) {
                            if (twinsMap.get(key).substring(0, 5).equals(dateMsg.substring(0, 5)) && !key.equals(nameMsg)) {
                                twinsList.add(key);
                            }
                        }

                        for (String s : twinsList) {
                            twinsString += s + "\n";
                        }
                        sendMessage("BEGIN TWINS");
                        sendMessage(twinsString + "END TWINS");
                    } else {

                        //STATEMENT RECEIVE_DATE
                        sendMessage("When were you born?");
                        dateMsg = reader.readLine();

                        if (dateMsg == null) {
                            connection.close();
                            break;
                        }

                        dateMsg = dateMsg.trim();

                        try {
                            String[] values = dateMsg.split(":");
                            int day = Integer.parseInt(values[0]);
                            int month = Integer.parseInt(values[1]);
                            int year = Integer.parseInt(values[2]);
                            int count = dateMsg.length() - dateMsg.replaceAll(":", "").length();

                            if (count == 2 && year >= 1900 && year <= 2019 && (((month == 4 || month == 6 || month == 9 || month == 11) && day >= 1 && day <= 30) || ((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) && day >= 1 && day <= 31) || (month == 2 && day >= 1 && day <= 29))) {

                                writerTxt.println(nameMsg + "=" + dateMsg);
                                writerTxt.close();

                                twinsMap.put(nameMsg, dateMsg);
                                for (String key : twinsMap.keySet()) {
                                    if (twinsMap.get(key).contains(dateMsg.substring(0, 5)) && !key.equals(nameMsg)) {
                                        twinsList.add(key);
                                    }
                                }

                                for (String s : twinsList) {
                                    twinsString += s + "\n";
                                }

                                sendMessage("BEGIN TWINS");
                                sendMessage(twinsString + "END TWINS");

                            } else {
                                throw new Exception();
                            }
                        } catch (Exception e) {
                            //System.out.println("Error 2");
                            //System.out.println("Wrong date");
                            sendMessage("Error 2");
                            connection.close();
                            break;
                        } finally {
                            //System.out.println("The 'try catch' of Error 2 is finished.");
                        }

                    }

                    //STATEMENT RECEIVE_REQ
                    while (true) {
                        String reqMsg = reader.readLine();

                        if (reqMsg == null) {
                            connection.close();
                            break;
                        }

                        reqMsg = reqMsg.trim();
                        try {
                            if ("quit".equalsIgnoreCase(reqMsg)) {
                                System.out.println("Closing connection");
                                connection.close();
                                break;
                            } else if ("refresh".equalsIgnoreCase(reqMsg)) {
                                sendMessage("BEGIN TWINS");
                                sendMessage(twinsString + "END TWINS");
                            } else if ("delete me".equalsIgnoreCase(reqMsg)) {
                                String delete = nameMsg + "=" + dateMsg;
                                deleteMe(delete);
                                twinsList.remove(nameMsg);
                                twinsMap.remove(dateMsg, nameMsg);
                                //System.out.println("The user called " + "'" + nameMsg + "'" + " was deleted");
                                connection.close();
                                break;
                            } else {
                                throw new Exception();
                            }
                        } catch (Exception e) {
                            //System.out.println("Error 0");
                            //System.out.println("Other cases are wrong");
                            sendMessage("Error 0");
                            connection.close();
                            break;
                        } finally {
                            //System.out.println("The 'try catch' of Error 0 is finished.");
                        }
                    }
                    
                    /*    
                    } catch (Exception e) {
                        //System.out.println("User already exist");
                        //System.out.println("Error 3");
                        sendMessage("Error 3");
                        connection.close();
                        break;
                    } finally {
                        //System.out.println("The 'try catch' is finished.");
                    }
                     */

                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                //System.out.println("Error 1");
                //System.out.println("Wrong name");
                sendMessage("Error 1");
                connection.close();
                break;
            } finally {
                //System.out.println("The 'try catch' of Error 1 is finished.");
            }
            break;
        }
    }

    public void deleteMe(String delete) throws IOException {
        File file = new File("TwinsList.txt");
        List<String> out = Files.lines(file.toPath()).filter(line -> !line.contains(delete)).collect(Collectors.toList());
        Files.write(file.toPath(), out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Send a newline-terminated message on the output stream to the client.
     *
     * @param msg the message to send, not including the newline
     * @throws IOException
     */
    private void sendMessage(String msg) throws IOException {
        writer.write(msg);
        writer.write("\n");
        // this flush() is necessary, otherwise ouput is buffered locally and
        // won't be sent to the client until it is too late 
        writer.flush();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        String usage = "Usage: java twins.Server [<port-number>] ";
        if (args.length > 1) {
            throw new Error(usage);
        }
        int port = 8123;
        try {
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }
        } catch (NumberFormatException e) {
            throw new Error(usage + "\n" + "<port-number> must be an integer");
        }
        try {
            InetAddress ip = InetAddress.getLocalHost();
            System.out.println("Server host: " + ip.getHostAddress() + " (" + ip.getHostName() + ")");
        } catch (IOException e) {
            System.err.println("could not determine local host name");
        }
        Server server = new Server(port);
        server.start();
        System.err.println("Server loop terminated!"); // not supposed to happen
    }
}
