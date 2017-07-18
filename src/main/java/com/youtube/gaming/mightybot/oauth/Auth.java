package com.youtube.gaming.mightybot.oauth;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.youtube.gaming.mightybot.properties.MightyProperties;
import com.youtube.gaming.mightybot.util.DynamicPath;

public class Auth {
  private static final Logger logger = LoggerFactory.getLogger(MightyProperties.class);

  /** Define a global instance of the HTTP transport. */
  private static final HttpTransport httpTransport = new NetHttpTransport();

  /** Define a global instance of the JSON factory. */
  private static final JsonFactory jsonFactory = new JacksonFactory();

  private static final String CLIENT_SECRETS = "client_secrets.json";

  /**
   * This is the directory that will be used under the user's home directory where OAuth tokens will
   * be stored.
   */
  private static final String CREDENTIALS_DIRECTORY = ".oauth-credentials";

  /**
   * Authorizes the installed application to access user's protected data.
   *
   * @param scopes list of scopes needed to access personal YouTube data
   * @param credentialDatastore name of the credential datastore to cache OAuth tokens
   */
  public static Credential authorize(List<String> scopes, String credentialDatastore)
      throws IOException {
    // Load client secrets.
    Path clientSecretsPath = DynamicPath.locate(CLIENT_SECRETS);
    if (!clientSecretsPath.toFile().exists()) {
      throw new IOException(
          "Create an OAuth Client ID from https://console.developers.google.com/apis/credentials, "
              + "download it and save it into "
              + clientSecretsPath.toAbsolutePath().toFile().toString());
    }

    GoogleClientSecrets clientSecrets;
    try (InputStream input = new FileInputStream(clientSecretsPath.toAbsolutePath().toFile());
        Reader clientSecretReader = new InputStreamReader(input, "UTF-8")) {
      clientSecrets = GoogleClientSecrets.load(jsonFactory, clientSecretReader);
    }

    // This creates the credentials datastore at ~/.oauth-credentials/${credentialDatastore}
    FileDataStoreFactory fileDataStoreFactory = new FileDataStoreFactory(
        new File(System.getProperty("user.home") + "/" + CREDENTIALS_DIRECTORY));
    DataStore<StoredCredential> datastore = fileDataStoreFactory.getDataStore(credentialDatastore);

    logger.info("Saving/using OAuth credentials in {}",
        fileDataStoreFactory.getDataDirectory().toString());

    GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, scopes)
            .setCredentialDataStore(datastore).build();

    // Build the local server and bind it to port 8080
    LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(8080).build();

    // Authorize.
    return new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");
  }
}
