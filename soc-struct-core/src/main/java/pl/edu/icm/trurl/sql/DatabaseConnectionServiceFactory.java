package pl.edu.icm.trurl.sql;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.BentoFactory;

public enum DatabaseConnectionServiceFactory implements BentoFactory<DatabaseConnectionService> {
  IT;

  public DatabaseConnectionService createInContext(Bento bento) {
    return new DatabaseConnectionService(bento.getString("jdbcUrl"), bento.getString("jdbcUser"), bento.getString("jdbcPassword"));
  }
}
