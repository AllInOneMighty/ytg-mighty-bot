package com.youtube.gaming.mightybot.modules;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.youtube.gaming.mightybot.MightyContext;
import com.youtube.gaming.mightybot.Module;
import com.youtube.gaming.mightybot.exceptions.InvalidConfigurationException;
import com.youtube.gaming.mightybot.properties.MightyProperty;

/** Outputs the user's channel number of subscribers to a specified file on the computer. */
public class SubCount extends Module {
  private static final Logger logger = LoggerFactory.getLogger(SubCount.class);

  private static final String FORMAT_INPUT_FILE = "formatInputFile";
  private static final String OUTPUT_FILE = "outputFile";
  private static final String INTERVAL = "interval";
  private static final int MINIMUM_INTERVAL = 5;

  private Path formatInputPath;
  private Path outputPath;
  private String format;

  @Override
  public void checkProperties() {
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
    getProperties().throwIfNullOrEmpty(FORMAT_INPUT_FILE, "Format input file can't be empty.");
    Path formatInputPath = Paths.get(getProperties().get(FORMAT_INPUT_FILE));
    if (!Files.exists(formatInputPath)) {
      throw new InvalidConfigurationException(
          String.format("Format input file does not exist: %s", formatInputPath));
    }
    if (!Files.isReadable(formatInputPath)) {
      throw new RuntimeException(
          String.format("Format input file is not readable: %s", formatInputPath));
    }
    getProperties().throwIfNullOrEmpty(INTERVAL, "Interval can't be empty");
    if (getProperties().getInt(INTERVAL) < MINIMUM_INTERVAL) {
      throw new InvalidConfigurationException(getProperties().addPrefix(INTERVAL),
          "Interval can't be less than 5s");
    }
  }

  @Override
  public void init() {
    logger.info("Watching sub count of channel {}", getProperties().get(MightyProperty.CHANNEL_ID));

    formatInputPath = Paths.get(getProperties().get(FORMAT_INPUT_FILE));
    outputPath = Paths.get(getProperties().get(OUTPUT_FILE));

    updateFormat();
    if (format == null) {
      // We have no format available, so quit immediately
      logger.error("Can't read format file and no format was already loaded. Quitting.");
      throw new RuntimeException();
    }

    // Displaying startup information
    logger.info(
        String.format("Writing sub count to file: %s", outputPath.toAbsolutePath().toString()));
  }

  private void updateFormat() {
    try (BufferedReader reader = Files.newBufferedReader(formatInputPath)) {
      format = reader.readLine();
    } catch (IOException e) {
      logger.warn("Could not read format file. Continuing with previous format.", e);
    }
  }

  @Override
  public long getIntervalSecond() {
    return getProperties().getInt(INTERVAL);
  }

  @Override
  @Nullable
  public Set<String> getRequiredOauthScopes() {
    return null;
  }

  @Override
  public void run(MightyContext context) throws IOException {
    // Retrieving channel statistics
    YouTube.Channels.List request = context.youTube().channels().list("statistics");
    request.setId(getProperties().get(MightyProperty.CHANNEL_ID));
    request.setKey(getProperties().get(MightyProperty.API_KEY));
    ChannelListResponse response = request.execute();
    if (response.getItems().size() != 1) {
      throw new RuntimeException(
          String.format("YouTube API didn't return one channel: %s", response.getItems()));
    }
    Channel channel = response.getItems().get(0);
    String subscriberCount = String.valueOf(channel.getStatistics().getSubscriberCount());

    // Update format
    updateFormat();

    // Writing output
    try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
      writer.write(String.format(format, subscriberCount));
    } catch (FileSystemException e) {
      logger.warn("Output writing failed. Skipping...");
    }
  }
}
