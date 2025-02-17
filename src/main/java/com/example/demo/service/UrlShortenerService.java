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
   * Shortens the provided long URL by assigning it an alias and optional expiration.
   *
   * <p>If the supplied alias is null or empty, a random 6-character alias is generated. The method checks if the alias
   * already exists in the store and throws a {@code RuntimeException} if it does. If a time-to-live in seconds is provided,
   * an expiration time is calculated from the current instant; if not, the URL entry will not expire. The shortened URL
   * is composed by appending the alias to the base URL.
   *
   * @param longUrl    the original URL to be shortened
   * @param alias      the desired alias for the shortened URL; if null or empty, a random alias is generated
   * @param ttlSeconds the time-to-live (in seconds) for the shortened URL; if null, the URL does not expire
   * @return           the complete shortened URL composed of the base URL and the alias
   * @throws RuntimeException if the alias is already in use
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
   * Retrieves the long URL associated with the given alias.
   *
   * <p>This method attempts to locate the URL entry for the provided alias from the internal store.
   * If the entry is not found or if it has an expired timestamp, a {@code RuntimeException} is thrown.
   * Upon successful verification, the method records the current access by adding a timestamp to the
   * access history and returns the corresponding long URL.</p>
   *
   * @param alias the unique identifier for the shortened URL
   * @return the original long URL linked to the given alias
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
   * Retrieves the last ten access timestamps associated with the given alias.
   *
   * <p>This method fetches the access history of the URL entry that matches the provided alias.
   * If the alias does not exist in the store, a RuntimeException is thrown.
   *
   * @param alias the alias corresponding to the shortened URL
   * @return a list of up to ten {@link Instant} objects representing the recent access timestamps
   * @throws RuntimeException if the alias is not found in the URL store
   */
  public List<Instant> getAccessHistory(String alias) {
    UrlEntry entry = urlStore.get(alias);
    if (entry == null) {
      throw new RuntimeException("Alias not found.");
    }
    return entry.getLast10Accesses();
  }

  /**
   * Retrieves all aliases currently stored in the URL store.
   *
   * <p>This method returns a new list containing all the keys from the internal store,
   * representing the aliases for shortened URLs. Modifications to the returned list do not
   * affect the underlying store.
   *
   * @return a list of all stored URL aliases.
   */
  public List<String> getAllAliases() {
    return new ArrayList<>(urlStore.keySet());
  }

  /**
   * Deletes the specified alias from the URL store.
   *
   * <p>This method removes the given alias from the internal URL store. If the alias does not exist,
   * a {@link RuntimeException} is thrown with the message "Alias not found."
   *
   * @param alias the alias to be deleted
   * @return {@code true} if the alias has been successfully deleted from the store
   * @throws RuntimeException if the alias is not present in the URL store
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
     * Constructs a new UrlEntry with the specified long URL and expiration timestamp.
     *
     * @param longUrl the original long URL to be stored
     * @param expiry the expiration time for the URL entry; may be null if no expiration is set
     */
    public UrlEntry(String longUrl, Instant expiry) {
      this.longUrl = longUrl;
      this.expiry = expiry;
    }

    /**
     * Retrieves the long URL stored in this URL entry.
     *
     * @return the long URL as a String.
     */
    public String getLongUrl() {
      return longUrl;
    }

    /**
     * Retrieves the expiry timestamp for this URL entry.
     *
     * <p>This method returns the {@code Instant} indicating when the URL entry will expire.
     * If no expiry time is configured, it may return {@code null}.
     *
     * @return the expiry time as an {@code Instant}, or {@code null} if the URL does not have an expiry.
     */
    public Instant getExpiry() {
      return expiry;
    }

    /**
     * Adds the current timestamp to the access history.
     *
     * <p>This method inserts the current time, obtained via {@link Instant#now()},
     * at the beginning of the access history collection. If the total number of
     * timestamps exceeds 10 after insertion, it removes the oldest timestamp to
     * maintain a maximum of 10 entries.
     */
    public void addAccessTimestamp() {
      accessHistory.addFirst(Instant.now());
      if (accessHistory.size() > 10) {
        accessHistory.removeLast();
      }
    }

    /**
     * Returns a new list containing the last 10 access timestamps recorded for this URL entry.
     *
     * <p>This method creates a snapshot of the current access history, ensuring that modifications to the returned
     * list do not affect the internal state of the URL entry.</p>
     *
     * @return a list of {@link java.time.Instant} objects representing the last 10 access timestamps
     */
    public List<Instant> getLast10Accesses() {
      return new ArrayList<>(accessHistory);
    }
  }

  /**
   * Retrieves the last 10 access timestamps for the specified alias.
   *
   * <p>This method looks up the corresponding URL entry in the store and returns the most recent
   * 10 access timestamps recorded for that alias. If the alias does not exist, a {@code RuntimeException}
   * is thrown.
   *
   * @param alias the alias to retrieve access history for
   * @return a list of up to 10 most recent access timestamps associated with the alias
   * @throws RuntimeException if the alias is not found in the URL store
   */
  public List<Instant> getAliasStats(String alias) {
    UrlEntry entry = urlStore.get(alias);
    if (entry == null) {
      throw new RuntimeException("Alias not found.");
    }
    return entry.getLast10Accesses();
  }
}
