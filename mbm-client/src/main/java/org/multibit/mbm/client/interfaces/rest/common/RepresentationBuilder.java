package org.multibit.mbm.client.interfaces.rest.common;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory;
import com.theoryinpractise.halbuilder.api.Representation;
import org.multibit.mbm.client.common.pagination.Pagination;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * <p>Builder to provide the following to DTOs:</p>
 * <ul>
 * <li>Building generic representations</li>
 * </ul>
 *
 * @since 0.0.1
 *         
 */
public class RepresentationBuilder {

  // Mandatory configuration
  private final URI self;
  private final Optional<Object> beanOptional;

  // Optional configuration
  private Optional<Pagination> paginationOptional = Optional.absent();
  private Map<String, String> links = Maps.newHashMap();
  private Map<String, List<Representation>> embedded = Maps.newHashMap();

  // Control
  private boolean isBuilt = false;

  public RepresentationBuilder(URI self, Optional<Object> beanOptional) {
    this.self = self;
    this.beanOptional = beanOptional;
  }

  /**
   * @param self         The URI representing this resource
   * @param beanOptional An optional bean providing a DTO representation
   *
   * @return A new instance of the builder
   */
  public static RepresentationBuilder newInstance(URI self, Optional<Object> beanOptional) {
    return new RepresentationBuilder(self, beanOptional);
  }

  /**
   * Handles the building process. No further configuration is possible after this.
   *
   * @return The item instance
   */
  public Representation build() {

    validateState();

    // Representation is a DTO so requires a default constructor
    DefaultRepresentationFactory factory = new DefaultRepresentationFactory();

    final Representation representation;

    // Check for pagination links
    if (paginationOptional.isPresent()) {
      Pagination pagination = paginationOptional.get();

      // Build a self URI with pagination parameters
      URI paginatedSelf = UriBuilder
        .fromUri(self)
        .queryParam("pn", pagination.getCurrentPage())
        .queryParam("ps",pagination.getResultsPerPage())
        .build();

      representation = factory.newRepresentation(paginatedSelf);
      representation
        .withLink("first", UriBuilder
          .fromUri(self)
          .queryParam("pn", 1)
          .queryParam("ps", pagination.getResultsPerPage())
          .build())
        .withLink("previous", UriBuilder
          .fromUri(self)
          .queryParam("pn", pagination.getPreviousPage())
          .queryParam("ps", pagination.getResultsPerPage())
          .build())
        .withLink("current", UriBuilder
          .fromUri(self)
          .queryParam("pn", pagination.getPreviousPage())
          .queryParam("ps", pagination.getResultsPerPage())
          .build())
        .withLink("next", UriBuilder
          .fromUri(self)
          .queryParam("pn", pagination.getNextPage())
          .queryParam("ps", pagination.getResultsPerPage())
          .build())
        .withLink("last", UriBuilder
          .fromUri(self)
          .queryParam("pn", pagination.getTotalPages())
          .queryParam("ps", pagination.getResultsPerPage())
          .build());
    } else {
      representation = factory.newRepresentation(self);
    }

    // Add any links
    for (Map.Entry<String, String> entry : links.entrySet()) {
      representation.withLink(entry.getKey(), entry.getValue());
    }

    // Add the bean
    if (beanOptional.isPresent()) {
      representation.withBean(beanOptional.get());
    }

    // Add the embedded entries
    for (Map.Entry<String, List<Representation>> entry : embedded.entrySet()) {
      List<Representation> embeddedList = entry.getValue();
      // Duplicated keys will be represented as an array
      for (Representation embedded : embeddedList) {
        representation.withRepresentation(entry.getKey(), embedded);
      }
    }

    isBuilt = true;

    return representation;
  }

  private void validateState() {
    if (isBuilt) {
      throw new IllegalStateException("Build process is complete - no further changes can be made");
    }
  }

  /**
   * Add standard pagination links
   *
   * @param pagination The pagination information
   *
   * @return This builder
   */
  public RepresentationBuilder withPagination(Pagination pagination) {
    this.paginationOptional = Optional.fromNullable(pagination);
    return this;
  }

  /**
   * Add a map of embedded representations (useful for side-loading associated representations)
   *
   * @param embedded A map to allow each collection to be named
   *
   * @return This builder
   */
  public RepresentationBuilder withEmbedded(Map<String, List<Representation>> embedded) {
    this.embedded = embedded;
    return this;

  }

  /**
   * Add a collection of links.
   *
   * @param links The string representation of the URIs to allow URI templates to be included
   *
   * @return This builder
   */
  public RepresentationBuilder withLinks(Map<String, String> links) {

    this.links = links;
    return this;

  }

}
