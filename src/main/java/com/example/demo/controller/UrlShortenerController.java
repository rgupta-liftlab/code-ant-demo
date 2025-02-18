package com.example.demo.controller;

import com.example.demo.service.UrlShortenerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/url")
public class UrlShortenerController {

  private final UrlShortenerService urlShortenerService;

  public UrlShortenerController(UrlShortenerService urlShortenerService) {
    this.urlShortenerService = urlShortenerService;
  }

  @PostMapping("/shorten")
  public ResponseEntity<Map<String, String>> shortenUrl(@RequestParam String longUrl,
      @RequestParam(required = false) String alias,
      @RequestParam(required = false) Long ttl) {
    String shortUrl = urlShortenerService.shortenUrl(longUrl, alias, ttl);
    return ResponseEntity.ok(Collections.singletonMap("shortUrl", shortUrl));
  }

  @GetMapping("/{alias}")
  public RedirectView redirectToLongUrl(@PathVariable String alias) {
    String longUrl = urlShortenerService.getLongUrl(alias);
    return new RedirectView(longUrl);
  }

  @GetMapping("/{alias}/stats")
  public ResponseEntity<List<Instant>> getAliasStats(@PathVariable String alias) {
    return ResponseEntity.ok(urlShortenerService.getAliasStats(alias));
  }

  @GetMapping("/aliases")
  public ResponseEntity<List<String>> getAllAliases() {
    return ResponseEntity.ok(urlShortenerService.getAllAliases());
  }

  @DeleteMapping("/{alias}")
  public ResponseEntity<String> deleteAlias(@PathVariable String alias) {
    boolean deleted = urlShortenerService.deleteAlias(alias);
    return deleted ? ResponseEntity.ok("Alias deleted successfully") : ResponseEntity.notFound().build();
  }
}
