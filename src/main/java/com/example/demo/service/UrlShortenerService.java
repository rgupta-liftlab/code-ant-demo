package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UrlShortenerService {

  @Value("${shortener.base-url}")
  private String baseUrl;

  private final Map<String, UrlEntry> urlStore = new ConcurrentHashMap<>();

  /**
   * Creates a shortened URL from the provided long URL.
   *
   * <p>If the supplied alias is null or empty, a random 6-character alias is generated.
   * The method checks whether the alias already exists in the URL store and throws a RuntimeException
   * if it does. If a non-null TTL (time-to-live) in seconds is provided, an expiration timestamp
   * is computed by adding the TTL to the current time; otherwise, no expiration is set.
   * The URL entry is then stored, and the method returns the complete shortened URL.
   *
   * @param longUrl the original URL that needs to be shortened
   * @param alias the desired custom alias for the shortened URL; if null or empty, a random alias is generated
   * @param ttlSeconds the time-to-live in seconds for the shortened URL; may be null for no expiry
   * @return a string representing the complete shortened URL in the format "baseUrl/alias"
   * @throws RuntimeException if the alias is already in use by another URL entry
   */
  public String shortenUrl(String longUrl, String alias, Long ttlSeconds) {
    if (alias == null || alias.isEmpty()) {
      alias = UUID.randomUUID().toString().substring(0, 6);
    }

    if (urlStore.containsKey(alias)) {
      throw new RuntimeException("Alias already in use.");
    }

    Instant expiry = (ttlSeconds != null) ? Instant.now().plusSeconds(ttlSeconds) : null;
    urlStore.put(alias, new UrlEntry(longUrl, expiry));

    return baseUrl + "/" + alias;
  }

  /**
   * Retrieves the long URL associated with the specified alias.
   *
   * <p>
   * This method retrieves the URL entry corresponding to the given alias from the URL store. If no entry exists
   * or if the entry has expired, a <code>RuntimeException</code> is thrown. Otherwise, the method logs the current
   * access timestamp and returns the stored long URL.
   * </p>
   *
   * @param alias the alias identifier for the shortened URL
   * @return the original long URL associated with the specified alias
   * @throws RuntimeException if the alias is not found or the URL entry has expired
   */
  public String getLongUrl(String alias) {
    UrlEntry entry = urlStore.get(alias);
    if (entry == null || (entry.getExpiry() != null && Instant.now().isAfter(entry.getExpiry()))) {
      throw new RuntimeException("Alias not found or expired.");
    }

    entry.addAccessTimestamp();
    return entry.getLongUrl();
  }

  /**
   * Retrieves the last 10 access timestamps for the specified alias.
   *
   * <p>This method fetches the URL entry associated with the given alias from the URL store
   * and returns its access history limited to the most recent 10 timestamps. If the alias is not found,
   * a RuntimeException is thrown.
   *
   * @param alias the alias used to identify the URL entry
   * @return a list of the last 10 access timestamps as Instant objects
   * @throws RuntimeException if the alias does not exist in the URL store
   */
  public List<Instant> getAccessHistory(String alias) {
    UrlEntry entry = urlStore.get(alias);
    if (entry == null) {
      throw new RuntimeException("Alias not found.");
    }
    return entry.getLast10Accesses();
  }

  /**
   * Retrieves all the aliases currently stored in the URL shortener service.
   *
   * <p>This method collects all keys from the internal URL store and returns them as a new list.
   *
   * @return a list of all alias strings available in the service
   */
  public List<String> getAllAliases() {
    return new ArrayList<>(urlStore.keySet());
  }

  /**
   * Deletes the specified alias from the URL store.
   *
   * <p>This method attempts to remove the alias from the internal store. If the alias does not exist,
   * a {@code RuntimeException} is thrown.</p>
   *
   * @param alias the alias to delete
   * @return {@code true} if the alias was successfully deleted
   * @throws RuntimeException if no alias matching the provided key is found in the store
   */
  public boolean deleteAlias(String alias) {
    if (urlStore.remove(alias) == null) {
      throw new RuntimeException("Alias not found.");
    }
    return true;
  }

  private static class UrlEntry {
    private final String longUrl;
    private final Instant expiry;
    private final LinkedList<Instant> accessHistory = new LinkedList<>();

    /**
     * Constructs a new UrlEntry with the specified long URL and expiry timestamp.
     *
     * @param longUrl the original long URL associated with this entry
     * @param expiry  the expiration time for this URL entry
     */
    public UrlEntry(String longUrl, Instant expiry) {
      this.longUrl = longUrl;
      this.expiry = expiry;
    }

    /**
     * Retrieves the long URL associated with this URL entry.
     *
     * @return the original long URL as a String
     */
    public String getLongUrl() {
      return longUrl;
    }

    /**
     * Retrieves the expiry time of this URL entry.
     *
     * @return an {@link Instant} representing the expiry time of the URL entry,
     *         or {@code null} if no expiry is set.
     */
    public Instant getExpiry() {
      return expiry;
    }

    /**
     * Adds the current access timestamp to the access history.
     *
     * <p>This method records the current time by adding it to the beginning of the access history list.
     * If the history grows beyond 10 entries, the oldest timestamp is removed to ensure that the list 
     * always contains only the 10 most recent access timestamps.</p>
     */
    public void addAccessTimestamp() {
      accessHistory.addFirst(Instant.now());
      if (accessHistory.size() > 10) {
        accessHistory.removeLast();
      }
    }

    /**
     * Retrieves a copy of the last 10 access timestamps for this URL entry.
     *
     * <p>This method returns a new list containing the timestamps of the most recent access events.
     * It provides a snapshot of the current access history without affecting the original internal list,
     * which is maintained to store at most 10 access timestamps.</p>
     *
     * @return a list of {@link Instant} objects representing the last 10 access timestamps
     */
    public List<Instant> getLast10Accesses() {
      return new ArrayList<>(accessHistory);
    }
  }

  /**
   * Retrieves the last 10 access timestamps for the specified alias.
   *
   * <p>This method fetches the {@code UrlEntry} associated with the given alias from the internal store. If the alias
   * does not exist, a {@code RuntimeException} is thrown. Otherwise, it returns the most recent 10 access timestamps
   * recorded for the URL.</p>
   *
   * @param alias the alias for which the access statistics are requested; must not be {@code null}
   * @return a list of up to 10 {@link java.time.Instant} objects representing the most recent access times
   * @throws RuntimeException if the alias is not found in the store
   */
  public List<Instant> getAliasStats(String alias) {
    UrlEntry entry = urlStore.get(alias);
    if (entry == null) {
      throw new RuntimeException("Alias not found.");
    }
    return entry.getLast10Accesses();
  }
}
