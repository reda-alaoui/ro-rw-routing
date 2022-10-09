package me.redaalaoui.ro_rw_routing.data_source;

import java.util.Optional;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author Réda Housni Alaoui
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

  public static final ThreadLocal<String> CURRENT_DATASOURCE = new ThreadLocal<>();

  @Override
  protected Object determineCurrentLookupKey() {
    return Optional.ofNullable(CURRENT_DATASOURCE.get()).orElse(DataSourceTypes.MAIN);
  }
}
