package com.youtube.gaming.mightybot.modules;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youtube.gaming.mightybot.MightyContext;
import com.youtube.gaming.mightybot.Module;

public class CurrentTime extends Module {
  private static final Logger logger = LoggerFactory.getLogger(CurrentTime.class);

  private static final String SIMPLE_DATE_FORMAT = "hh:mm a";
  private static final String OUTPUT_FILE = "outputFile";

  private SimpleDateFormat simpleDateFormat;
  private Path outputPath;

  @Override
  public void checkProperties() {
    simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);

    getProperties().throwIfNullOrEmpty(OUTPUT_FILE, "Output file can't be empty.");
    Path outputPath = Paths.get(getProperties().get(OUTPUT_FILE));
    if (!Files.exists(outputPath)) {
      try {
        Files.createFile(outputPath);
      } catch (IOException e) {
        throw new RuntimeException(String.format("Could not create output file: %s", outputPath),
            e);
      }
    }
    if (!Files.isWritable(outputPath)) {
      throw new RuntimeException(String.format("Output file is not writeable: %s", outputPath));
    }
  }

  @Override
  public void init() {
    outputPath = Paths.get(getProperties().get(OUTPUT_FILE));

    // Displaying startup information
    logger.info(
        String.format("Writing current time to file: %s", outputPath.toAbsolutePath().toString()));
  }

  @Override
  public long getIntervalSecond() {
    return 1;
  }

  @Override
  @Nullable
  public Set<String> getRequiredOauthScopes() {
    return null;
  }

  @Override
  public void run(MightyContext context) throws IOException {
    // Writing output
    try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
      writer.write(simpleDateFormat.format(Date.from(context.clock().instant())));
    } catch (FileSystemException e) {
      logger.warn("Output writing failed. Skipping...");
    }
  }
}
