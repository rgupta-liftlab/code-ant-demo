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
   * Constructs a new UrlShortenerController with the provided UrlShortenerService instance.
   *
   * @param urlShortenerService the service that handles URL shortening operations
   */
  public UrlShortenerController(UrlShortenerService urlShortenerService) {
    this.urlShortenerService = urlShortenerService;
  }

  /**
   * Shortens a given long URL by generating a corresponding short URL.
   *
   * <p>This method handles POST requests mapped to "/shorten". It accepts a required long URL along with optional
   * parameters for a custom alias and a time-to-live (TTL) value. The URL shortening logic is delegated to the
   * UrlShortenerService, which returns the short URL. The result is packaged in a ResponseEntity as a singleton map with the key "shortUrl".
   *
   * @param longUrl the original URL to be shortened
   * @param alias an optional custom alias for the shortened URL; if not provided, a random alias is generated
   * @param ttl an optional time-to-live value in seconds for the shortened URL; if null, the default TTL is applied
   * @return a ResponseEntity containing a singleton map where the key "shortUrl" maps to the generated short URL
   */
  @PostMapping("/shorten")
  public ResponseEntity<Map<String, String>> shortenUrl(@RequestParam String longUrl,
      @RequestParam(required = false) String alias,
      @RequestParam(required = false) Long ttl) {
    String shortUrl = urlShortenerService.shortenUrl(longUrl, alias, ttl);
    return ResponseEntity.ok(Collections.singletonMap("shortUrl", shortUrl));
  }

  /**
   * Redirects the client to the long URL associated with the given alias.
   *
   * <p>This method handles HTTP GET requests to "/{alias}". It retrieves the original long URL
   * corresponding to the provided alias by invoking {@link UrlShortenerService#getLongUrl(String)}
   * and then returns a {@link org.springframework.web.servlet.view.RedirectView} to perform the redirect.
   *
   * @param alias the alias representing the shortened URL
   * @return a RedirectView that redirects to the corresponding long URL
   */
  @GetMapping("/{alias}")
  public RedirectView redirectToLongUrl(@PathVariable String alias) {
    String longUrl = urlShortenerService.getLongUrl(alias);
    return new RedirectView(longUrl);
  }

  /**
   * Retrieves the access statistics for a specific URL alias.
   *
   * <p>This method handles GET requests to the endpoint "/{alias}/stats". It fetches a list of timestamps
   * indicating when the alias was accessed by querying the URL shortening service.</p>
   *
   * @param alias the URL alias for which statistics are to be retrieved
   * @return a ResponseEntity containing a list of instants representing the access timestamps
   */
  @GetMapping("/{alias}/stats")
  public ResponseEntity<List<Instant>> getAliasStats(@PathVariable String alias) {
    return ResponseEntity.ok(urlShortenerService.getAliasStats(alias));
  }

  /**
   * Retrieves all URL aliases.
   * 
   * <p>This endpoint handles GET requests to "/aliases" and returns a list of all shortened URL aliases
   * as obtained from the UrlShortenerService.
   *
   * @return a ResponseEntity containing the list of alias strings
   */
  @GetMapping("/aliases")
  public ResponseEntity<List<String>> getAllAliases() {
    return ResponseEntity.ok(urlShortenerService.getAllAliases());
  }

  /**
   * Deletes the specified alias.
   *
   * <p>This endpoint attempts to delete the alias provided in the URL path by invoking the URL shortener service.
   * If the alias is successfully deleted, the method returns an HTTP 200 response with a success message.
   * If the alias is not found, it returns an HTTP 404 response.
   *
   * @param alias the unique alias identifier to delete
   * @return a ResponseEntity containing a success message if deletion is successful, or a 404 response if the alias does not exist
   */
  @DeleteMapping("/{alias}")
  public ResponseEntity<String> deleteAlias(@PathVariable String alias) {
    boolean deleted = urlShortenerService.deleteAlias(alias);
    return deleted ? ResponseEntity.ok("Alias deleted successfully") : ResponseEntity.notFound().build();
  }
}
