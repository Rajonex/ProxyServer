package App;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class App {

    private static List<URL> blackList;
    private static Map<String, Statistics> statisticsMap;
    private static final String fileName = "data.csv";


    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Hook());
        int port = 8000;
        blackList = Utils.readFromFileBlackList("C:\\workspace_intelliJ\\serwerproxy\\blacklist.txt");
        statisticsMap = Utils.openAndReadData(fileName);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler());
        System.out.println("Starting server on port: " + port);
        server.start();
    }

    static class Hook extends Thread{
        public void run(){
            Utils.saveData(statisticsMap, fileName);
        }
    }

    static class RootHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            URI serviceUri = exchange.getRequestURI();
            String serviceURL = serviceUri.toString();

            /**
             * Rozpoczete zadanie do kolejnego punktu z opisu zadania
             */
            URL testUrl = new URL(serviceURL);

            boolean flagFilter = true; // Kod odpowiedzialny za filtrowanie URL
            if (testUrl != null) {

                for (URL str : blackList) {
                    if (testUrl.equals(str)) { // sprawdzanie bezposrednich URL-i
                        flagFilter = false;
                        System.out.println("Block");
                        break;
                    }
                    if(str != null) // sprawdzanie czy URL nie jest nullem, do usuniecia caly if z zawartoscia jesli mamy tylko sprawdzac konkretny url
                    {
                        System.out.println(testUrl.getHost() + " : " + str.getHost());
                        if(testUrl.getHost().equals(str.getHost())) // sprawdzanie czy hosty sa inne
                        {
                            flagFilter = false;
                            System.out.println("Block host");
                            break;
                        }
                    }
                }
            }



            if (flagFilter) {
                String domain = serviceUri.getHost();
                long sended;
                long received;
                domain = domain.startsWith("www.") ? domain.substring(4) : domain;


                URL myURL = new URL(serviceURL);
//                System.out.println("URL=" + myURL);
                HttpURLConnection myURLConnection = (HttpURLConnection) myURL.openConnection();
                Headers headers = exchange.getRequestHeaders();


//                System.out.println("SENDED HEADERS:");
                for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                    for (String value : header.getValue()) {
//                        System.out.println(header.getKey() + " : " + value);
                        myURLConnection.setRequestProperty(header.getKey(), value);
                    }

                }
                myURLConnection.setRequestMethod(exchange.getRequestMethod());
//                System.out.println("SENDED REQUEST METHOD=" + exchange.getRequestMethod());
                myURLConnection.setInstanceFollowRedirects(false);
                if (exchange.getRequestMethod().equals("GET")) {
                    myURLConnection.setDoInput(true);
                    myURLConnection.setDoOutput(false);
                    sended = 0L;
                } else {
                    if (exchange.getRequestMethod().equals("POST") || exchange.getRequestMethod().equals("PUT") || exchange.getRequestMethod().equals("DELETE")) {
                        myURLConnection.setDoInput(true);

                        myURLConnection.setDoOutput(true);
                        byte[] clientBytes = IOUtils.toByteArray(exchange.getRequestBody());
                        sended = clientBytes.length;
                        OutputStream osConnection = myURLConnection.getOutputStream();
                        osConnection.write(clientBytes);
                        osConnection.flush();
                        osConnection.close();
                        exchange.getRequestBody().close();


                    } else {
                        myURLConnection.setDoInput(true);
                        myURLConnection.setDoOutput(true);
                        sended =0;
                    }
                }


                myURLConnection.connect();


                InputStream is = myURLConnection.getInputStream();
                OutputStream os = exchange.getResponseBody();
                byte[] bytes = IOUtils.toByteArray(is);
//                System.out.println("RECEIVED HEADERS:");
                received = bytes.length;
                for (Map.Entry<String, List<String>> header : myURLConnection.getHeaderFields().entrySet()) {
                    for (String value : header.getValue()) {

                        if (header.getKey() != null && !header.getKey().equals("Transfer-Encoding")) {
//                            System.out.println(header.getKey() + " : " + value);
                            exchange.getResponseHeaders().set(header.getKey(), value);
                        }

                    }

                }
                exchange.sendResponseHeaders(myURLConnection.getResponseCode(), bytes.length);
                os.write(bytes);
                os.flush();
                is.close();
                os.close();

                if(statisticsMap.containsKey(domain))
                {
                    statisticsMap.get(domain).increaseQuestionNumber(1);
                    statisticsMap.get(domain).increaseSendedData(sended);
                    statisticsMap.get(domain).increaseReceivedData(received);
                } else
                {
                    Statistics statistics = new Statistics(1, sended, received);
                    statisticsMap.put(domain, statistics);
                }

                myURLConnection.disconnect();
            } else {
                exchange.sendResponseHeaders(403, -1);
            }
        }
    }


}
