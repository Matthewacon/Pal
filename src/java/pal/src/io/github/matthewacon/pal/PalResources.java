package io.github.matthewacon.pal;

import java.io.File;

public final class PalResources {
 public static final File TEMP_DIR;

 static {
  TEMP_DIR = new File(System.getProperty("java.io.tmpdir") + "/palResources");
  TEMP_DIR.deleteOnExit();
 }
}
