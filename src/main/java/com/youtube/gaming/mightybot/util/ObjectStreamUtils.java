package com.youtube.gaming.mightybot.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * Utility methods for handling object streams.
 */
public class ObjectStreamUtils {
  /**
   * Reads the requested object from the file located at the given path.
   * <p>
   * Can be used by simply doing (example):
   * <pre>Integer i = ObjectStreamUtils.readObjectStreamFromFile(dbPath)</pre>
   *
   * @param <T> the type of object to read
   * @param path where the object stream is located
   * @return the requested object, deserialized
   *
   * @throws RuntimeException if anything bad occurs
   */
  public static <T> T readObjectStreamFromFile(Path path) {
    try (InputStream inputStream = new FileInputStream(path.toAbsolutePath().toFile());
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
      @SuppressWarnings("unchecked")
      T object = (T) objectInputStream.readObject();
      return object;
    } catch (ClassNotFoundException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Writes the given object serialized in the file located at the given path.
   *
   * @param path where to save the object in serialized form
   * @param object the object to serialize
   */
  public static void writeObjectStreamToFile(Path path, Object object) {
    try (OutputStream outputStream = new FileOutputStream(path.toFile());
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
      objectOutputStream.writeObject(object);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
