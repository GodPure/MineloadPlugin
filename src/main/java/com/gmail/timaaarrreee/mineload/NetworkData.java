/**
 * This class aims to collect network bytes transmitted, received and the rates
 * in KB/s for each platform. Edit: decided not to calculate rates here.
 *
 * @author Tim Sullivan
 */
package com.gmail.timaaarrreee.mineload;

import java.io.*;
import java.util.StringTokenizer;

public class NetworkData {

  private long transmitted;
  private long received;

  public NetworkData() {
    update();
  }

  private void processWindows() {
    //TODO
  }

  private void processMac() {
    //reset fields
    transmitted = 0;
    received = 0;
    String result = cmdExec("netstat -ib");
    String[] lines = result.split("\n");
    for (int i = 0; i < lines.length; i++) {
      StringTokenizer st = new StringTokenizer(lines[i]);
      //ignore the first line (contains column names)
      if (i > 0) {
        String[] data = new String[11];
        int x = 0;
        while (st.hasMoreTokens()) {
          data[x] = st.nextToken();
          x++;
        }

        if (data[0].equals("en0")) {
          transmitted = Long.valueOf(data[9]);
          received = Long.valueOf(data[6]);
        } else if (data[0].equals("en1")) {
          //System.out.println("Got wireless interface. Bytes In: " + data[6] + " Bytess out: " + data[9]);
          transmitted += Long.valueOf(data[9]);
          received += Long.valueOf(data[6]);
          return;
        }
      }
    }
  }

  private void processLinux() {
    transmitted = 0;
    received = 0;
    String result;
    try{
      result = fileToString(new File("/proc/net/dev"));
    } catch (IOException ioe){
      return;
    }
    String[] lines = result.split("\n");
    for (int i = 0; i < lines.length; i++) {
      StringTokenizer st = new StringTokenizer(lines[i]);
      //ignore the first line (contains column names)
      if (i > 1) {
        String[] data = new String[20];
        int x = 0;
        while (st.hasMoreTokens()) {
          data[x] = st.nextToken();
          x++;
        }
        String[] firstchunk = data[0].split(":");
        //ignore the loopback interface
        if (!firstchunk[0].equals("lo")) {
          received += Long.parseLong(firstchunk[1]);
          transmitted += Long.parseLong(data[8]);
        }
      }
    }
  }

  private boolean isWindows(String os) {
    return (os.indexOf("win") >= 0);
  }

  private boolean isMac(String os) {
    return (os.indexOf("mac") >= 0);
  }

  private boolean isLinux(String os) {
    return (os.indexOf("linux") >= 0);
  }

  private String cmdExec(String cmdLine) {
    String line;
    String output = "";
    try {
      Process p = Runtime.getRuntime().exec(cmdLine);
      BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
      while ((line = input.readLine()) != null) {
        output += (line + '\n');
      }
      input.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return output;
  }

  /**
   * Update data fields with new network data. Blocks for at least 1000ms.
   */
  public final void update() {
    String os = System.getProperty("os.name").toLowerCase();
    if (isWindows(os)) {
      //grr
      processWindows();
    } else if (isLinux(os)) {
      //yay
      processLinux();
    } else if (isMac(os)) {
      //grr
      processMac();
    } else {
      //wtf...
    }
  }

  //getters
  public long getTx() {
    return transmitted;
  }

  public long getRx() {
    return received;
  }

  public static String fileToString(File file) throws IOException {
    int len;
    char[] chr = new char[4096];
    final StringBuffer buffer = new StringBuffer();
    final FileReader reader = new FileReader(file);
    try {
      while ((len = reader.read(chr)) > 0) {
        buffer.append(chr, 0, len);
      }
    } finally {
      reader.close();
    }
    return buffer.toString();
  }
}
