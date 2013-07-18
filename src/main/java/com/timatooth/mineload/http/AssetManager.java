package com.timatooth.mineload.http;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Load resources from disk such as html, css, js, images etc.
 *
 * @author tim
 */
public class AssetManager {

  public static final Map<String, String> MIME = new HashMap<String, String>();
  public static File rootDir = new File("plugins/MineloadPlugin/www");
  private String namespace;

  static {
    MIME.put("css", "text/css");
    MIME.put("html", "text/html");
    MIME.put("xml", "application/xml");
    MIME.put("js", "application/javascript");
    MIME.put("jpg", "image/jpeg");
    MIME.put("gif", "image/gif");
    MIME.put("png", "image/png");
    MIME.put("svg", "image/svg+xml");
    MIME.put("zip", "application/zip");
  }

  public AssetManager(String namespace) {
    this.namespace = namespace;
  }

  /**
   * Open the contents of a file as String.
   *
   * @param filename Filename relative to inside its own folder.
   * @return String contents of file.
   */
  public byte[] loadAsset(String filename) throws FileNotFoundException {
    File path = new File(rootDir, filename);
    if(path.isDirectory()){
      File[] listings = path.listFiles();
      StringBuilder sb = new StringBuilder();
      sb.append("<h1>Contents of: " + filename + "</h1>");
      for(File f : listings){
        sb.append(f.toString());
        sb.append("<br>");
      }
      
      return sb.toString().getBytes();
    }
    if (filename.matches("^.+\\.(htm|html|css|xml|js|php)+$")) {
      //plain text files.
      // Use scanner to open file as text, \\a is for EOF as delimiter.
      String text = new Scanner(path.getAbsoluteFile(), "UTF-8").useDelimiter("\\A").next();
      return text.getBytes();
      
    } else if (filename.matches("^.+\\.(jpg|png|gif)+$")) {
      //binary files
      byte[] buf = new byte[(int)path.length()];
      DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(path)));
      try {
        dataInputStream.readFully(buf);
      } catch (IOException ex) {
        Logger.getLogger(AssetManager.class.getName()).log(Level.SEVERE, null, ex);
      }
      return buf;
    }

    System.out.println("Unknown file extension not processed: " + filename);
    return "Error, unknown file request could not be processed.".getBytes();
  }
}
