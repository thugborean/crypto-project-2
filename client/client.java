import java.io.*;
import java.net.*;
import java.security.*;
import javax.net.ssl.*;

public class client {
    private static final String HOST = "localhost";
    private static final int PORT = 8043;
    
    // Client PKCS12 file path
    private static final String PKCS12Location = "../client/client.p12";
    private static final String PKCS12Password = "client"; // Update if password changed
    
    // Add custom TrustStore password if not using cacerts
    // private static final String TSPassword = "changeit";
    
    public static void main(String[] args) throws Exception {
        char[] passphrase_ks = PKCS12Password.toCharArray();
        
        // Adding custom TrustStore
        // char[] passphrase_ts = CACERTSPassword.toCharArray();
        KeyStore ts = KeyStore.getInstance("PKCS12");
        ts.load(new FileInputStream(PKCS12Location), passphrase_ks);
        // TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        // tmf.init(ts);
        // TrustManager[] trustManagers = tmf.getTrustManagers();
        
        // Add code for Keystore
        // ??
        
        SSLContext context = SSLContext.getInstance("TLSv1.3");
    
        // KeyManagers from the KeyManagerFactory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ts, passphrase_ks);
        KeyManager[] keyManagers = kmf.getKeyManagers();

        // TrustManager (2nd arg) is null to use the default trust manager cacerts
        // To use custom TrustStore, 2nd argument changes to ’trustManagers’
        // context.init(??, ??, ??); // Add correct arguments
        context.init(keyManagers, null, null);
        
        SSLSocketFactory sf = context.getSocketFactory();
        
        try (SSLSocket s = (SSLSocket) sf.createSocket(HOST, PORT)) {
            OutputStream toserver = s.getOutputStream();
            toserver.write("\nConnection established.\n\n".getBytes());
            System.out.print("\nConnection established.\n\n");
            
            int inCharacter = System.in.read();
            
            try {
                while (inCharacter != '~') {
                    toserver.write(inCharacter);
                    toserver.flush();
                    inCharacter = System.in.read();
                }
            } catch (SocketException | EOFException e) {
                System.out.print("\nClient Closing.\n\n");
                e.printStackTrace();
                toserver.close();
                s.shutdownOutput();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("Cannot establish connection to server.");
            e.printStackTrace();
        } finally {
            System.out.println("Client stopped.");
        }
    }
}