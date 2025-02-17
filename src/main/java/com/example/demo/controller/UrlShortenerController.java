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

  /**
   * Constructs a new UrlShortenerController.
   *
   * <p>This constructor initializes the controller with the specified UrlShortenerService,
   * which provides the necessary functionalities for URL shortening, redirection, statistics
   * retrieval, and alias management.</p>
   *
   * @param urlShortenerService the service that handles URL shortening operations
   */
  public UrlShortenerController(UrlShortenerService urlShortenerService) {
    this.urlShortenerService = urlShortenerService;
  }

  /**
   * Shortens the specified long URL and returns a mapping containing the generated short URL.
   *
   * <p>This method handles POST requests to the "/shorten" endpoint. It accepts a required long URL
   * and optionally an alias and a time-to-live (TTL) value. The provided parameters are passed to the
   * underlying URL shortening service, which generates a short URL. The method then returns a response
   * containing a single key "shortUrl" mapped to the shortened URL.</p>
   *
   * @param longUrl the original URL to be shortened (required)
   * @param alias an optional custom alias for the short URL; if not provided, a unique alias will be generated
   * @param ttl an optional time-to-live (TTL) for the short URL
   * @return a {@code ResponseEntity} containing a map with a single entry where the key is "shortUrl" and the value is the generated short URL
   */
  @PostMapping("/shorten")
  public ResponseEntity<Map<String, String>> shortenUrl(@RequestParam String longUrl,
      @RequestParam(required = false) String alias,
      @RequestParam(required = false) Long ttl) {
    String shortUrl = urlShortenerService.shortenUrl(longUrl, alias, ttl);
    return ResponseEntity.ok(Collections.singletonMap("shortUrl", shortUrl));
  }

  /**
   * Redirects to the long URL corresponding to the provided alias.
   *
   * <p>This method retrieves the long URL associated with the given alias using
   * the UrlShortenerService and returns a RedirectView to redirect the client to that URL.
   *
   * @param alias the alias representing the shortened URL (must be a valid path variable)
   * @return a RedirectView instance for redirecting the client to the long URL
   */
  @GetMapping("/{alias}")
  public RedirectView redirectToLongUrl(@PathVariable String alias) {
    String longUrl = urlShortenerService.getLongUrl(alias);
    return new RedirectView(longUrl);
  }

  /**
   * Retrieves statistics for the specified alias.
   * <p>
   * This endpoint handles a GET request to "/{alias}/stats". It fetches and returns a list of timestamps
   * representing the activity or access events associated with the given alias by delegating the call
   * to the UrlShortenerService.
   * </p>
   *
   * @param alias the alias identifier for which to retrieve statistics
   * @return a ResponseEntity containing a list of Instant objects with the alias's statistics
   */
  @GetMapping("/{alias}/stats")
  public ResponseEntity<List<Instant>> getAliasStats(@PathVariable String alias) {
    return ResponseEntity.ok(urlShortenerService.getAliasStats(alias));
  }

  /**
   * Retrieves all URL aliases.
   *
   * <p>This endpoint is mapped to {@code GET /aliases} and returns a HTTP 200 OK response containing a list of
   * all alias strings available from the URL shortener service.</p>
   *
   * @return a ResponseEntity containing a List of alias strings
   */
  @GetMapping("/aliases")
  public ResponseEntity<List<String>> getAllAliases() {
    return ResponseEntity.ok(urlShortenerService.getAllAliases());
  }

  /**
   * Deletes a URL alias.
   *
   * <p>This endpoint handles DELETE requests to remove the specified alias from the system.
   * The deletion operation is delegated to {@code urlShortenerService.deleteAlias(alias)}.
   * If the alias is successfully deleted, the method returns a ResponseEntity with an HTTP OK status
   * and a success message. If the alias does not exist, it returns a ResponseEntity with an HTTP 404 status.
   *
   * @param alias the alias identifier to be deleted, extracted from the URL path.
   * @return a ResponseEntity containing a success message if the alias was deleted, or a 404 status if the alias does not exist.
   */
  @DeleteMapping("/{alias}")
  public ResponseEntity<String> deleteAlias(@PathVariable String alias) {
    boolean deleted = urlShortenerService.deleteAlias(alias);
    return deleted ? ResponseEntity.ok("Alias deleted successfully") : ResponseEntity.notFound().build();
  }
}
