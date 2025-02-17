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

  public String getLongUrl(String alias) {
    UrlEntry entry = urlStore.get(alias);
    if (entry == null || (entry.getExpiry() != null && Instant.now().isAfter(entry.getExpiry()))) {
      throw new RuntimeException("Alias not found or expired.");
    }

    entry.addAccessTimestamp();
    return entry.getLongUrl();
  }

  public List<Instant> getAccessHistory(String alias) {
    UrlEntry entry = urlStore.get(alias);
    if (entry == null) {
      throw new RuntimeException("Alias not found.");
    }
    return entry.getLast10Accesses();
  }

  public List<String> getAllAliases() {
    return new ArrayList<>(urlStore.keySet());
  }

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

    public UrlEntry(String longUrl, Instant expiry) {
      this.longUrl = longUrl;
      this.expiry = expiry;
    }

    public String getLongUrl() {
      return longUrl;
    }

    public Instant getExpiry() {
      return expiry;
    }

    public void addAccessTimestamp() {
      accessHistory.addFirst(Instant.now());
      if (accessHistory.size() > 10) {
        accessHistory.removeLast();
      }
    }

    public List<Instant> getLast10Accesses() {
      return new ArrayList<>(accessHistory);
    }
  }

  public List<Instant> getAliasStats(String alias) {
    UrlEntry entry = urlStore.get(alias);
    if (entry == null) {
      throw new RuntimeException("Alias not found.");
    }
    return entry.getLast10Accesses();
  }
}
