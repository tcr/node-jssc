import javax.xml.bind.DatatypeConverter;
import java.util.Scanner;
import java.io.*;
import jssc.SerialPort;
import jssc.SerialPortList;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class Main {

    static SerialPort serialPort;

    static void outCommand (char type, byte[] buf) {
        synchronized (System.out) {
            byte[] c = new byte[1 + buf.length];
            System.out.print(type);
            if (buf.length > 0) {
                System.out.print(DatatypeConverter.printBase64Binary(buf));
            }
            System.out.println();
        }
    }

    static void outError (Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        try {
            outCommand('E', sw.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) { }
    }

    static void silentClose () {
        try {
            serialPort.closePort();
        } catch (SerialPortException ex) { }
    }

    public static void main(String[] args) {
        // System.out.println("STARTING.");

        if (args[0].equals("--list")) {
            String[] portNames = SerialPortList.getPortNames();
            for (int i = 0; i < portNames.length; i++){
                outCommand('p', portNames[i].getBytes());
            }
            outCommand('p', new byte[] { });
            System.exit(0);
        }

        serialPort = new SerialPort(args[0]); 
        try {
            serialPort.openPort();//Open port
            serialPort.setParams(9600, 8, 1, 0);//Set params
            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR;//Prepare mask
            serialPort.setEventsMask(mask);//Set mask
            serialPort.addEventListener(new SerialPortReader());//Add SerialPortEventListener
        }
        catch (SerialPortException ex) {
            System.err.println("Error opening serial port.");
            outError(ex);
            silentClose();
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                silentClose();
            }
        });

        outCommand('L', new byte[] {});

      //  read the username from the command-line; need to use try/catch with the
      //  readLine() method
        while (true) {
            // System.out.println("TRYING TO READING...");
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));  
            try {
                String input = stdin.readLine();
                if (input.substring(0, 1).equals("O")) {
                    byte[] o = DatatypeConverter.parseBase64Binary(input.substring(1));
                    serialPort.writeBytes(o);
                } else if (input.substring(0, 1).equals("B")) {
                    System.err.println("Break 5 seconds");
                    serialPort.sendBreak(5000);
                }
            } catch (IOException ex) {
                System.err.println("Stdin error.");
                outError(ex);
                silentClose();
                System.exit(2);
            } catch (SerialPortException ex) {
                System.err.println("Writing error.");
                outError(ex);
                silentClose();
                System.exit(3);
            }
        }
    }
    
    /*
     * In this class must implement the method serialEvent, through it we learn about 
     * events that happened to our port. But we will not report on all events but only 
     * those that we put in the mask. In this case the arrival of the data and change the 
     * status lines CTS and DSR
     */
    static class SerialPortReader implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR()){//If data is available
                if(event.getEventValue() > 0){//Check bytes count in the input buffer
                    //Read data, if 10 bytes available 
                    try {
                        byte buffer[] = serialPort.readBytes(event.getEventValue());
                        outCommand('I', buffer);
                        // System.out.print(new String(buffer));
                        // if (new String(buffer).contains("0 bytes available.\n")) {
                        //     serialPort.writeBytes("0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789".getBytes());
                        // }
                    }
                    catch (SerialPortException ex) {
                        System.err.println("Reading error.");
                        outError(ex);
                        silentClose();
                        System.exit(4);
                    }
                }
            }

            // Clear to send
            else if(event.isCTS()){
                if(event.getEventValue() == 1){
                    // on
                   outCommand('C', new byte[] { });
                }
                else {
                    // off
                    outCommand('c', new byte[] { });
                }
            }

            // Data set ready
            else if(event.isDSR()){
                if(event.getEventValue() == 1){
                    // on
                    outCommand('D', new byte[] { });
                }
                else {
                    outCommand('d', new byte[] { });
                }
            }
        }
    }
}

