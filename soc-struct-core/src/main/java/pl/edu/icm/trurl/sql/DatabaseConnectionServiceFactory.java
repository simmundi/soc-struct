/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.trurl.sql;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.BentoFactory;

public enum DatabaseConnectionServiceFactory implements BentoFactory<DatabaseConnectionService> {
  IT;

  public DatabaseConnectionService createInContext(Bento bento) {
    return new DatabaseConnectionService(bento.getString("jdbcUrl"), bento.getString("jdbcUser"), bento.getString("jdbcPassword"));
  }
}
