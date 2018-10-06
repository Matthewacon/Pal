package io.github.matthewacon.pal.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public final class JarUtils {
 public static URL getResource(final String url) {
  return ClassLoader.getSystemClassLoader().getResource(url);
 }

 public static byte[] readResource(final String url) throws IOException {
  final URL resource = getResource(url);
  final InputStream is = resource.openStream();
  final ByteArrayOutputStream baos = new ByteArrayOutputStream();
  int nRead;
  byte[] data = new byte[8192];
  while ((nRead = is.read(data, 0, data.length)) != -1) {
   baos.write(data, 0, nRead);
  }
  is.close();
  baos.flush();
  data = baos.toByteArray();
  baos.close();
  return data;
 }

 //TODO
// public static void writeResource(final String url, final byte[] data) {
//  final URL resource = getResource(url);
//  final OutputStream os =
// }
}
