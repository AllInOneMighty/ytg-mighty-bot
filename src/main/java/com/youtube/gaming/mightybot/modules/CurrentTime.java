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

import com.google.common.base.Optional;
import com.youtube.gaming.mightybot.MightyContext;
import com.youtube.gaming.mightybot.Module;
import com.youtube.gaming.mightybot.util.ModuleUtils;

/** Outputs the current time in a specified file on the computer. */
public class CurrentTime extends Module {
  private static final Logger logger = LoggerFactory.getLogger(CurrentTime.class);

  private static final String SIMPLE_DATE_FORMAT = "hh:mm a";
  private static final String OUTPUT_FILE = "outputFile";

  private SimpleDateFormat simpleDateFormat;
  private Path outputPath;

  @Override
  public void checkProperties() {
    simpleDateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);

    ModuleUtils.assertModuleFileExistsAndWriteable(getProperties(), OUTPUT_FILE, Optional.absent());
  }

  @Override
  public void init() {
    outputPath = Paths.get(getProperties().get(OUTPUT_FILE));

    // Displaying startup information
    logger.info("Writing current time to file: {}", outputPath.toAbsolutePath().toString());
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
