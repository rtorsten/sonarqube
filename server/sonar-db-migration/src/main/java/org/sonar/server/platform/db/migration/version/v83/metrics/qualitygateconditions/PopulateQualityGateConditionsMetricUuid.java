/*
 * SonarQube
 * Copyright (C) 2009-2020 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.platform.db.migration.version.v83.metrics.qualitygateconditions;

import java.sql.SQLException;
import org.sonar.db.Database;
import org.sonar.server.platform.db.migration.step.DataChange;
import org.sonar.server.platform.db.migration.step.MassUpdate;

public class PopulateQualityGateConditionsMetricUuid extends DataChange {

  public PopulateQualityGateConditionsMetricUuid(Database db) {
    super(db);
  }

  @Override
  protected void execute(Context context) throws SQLException {
    // delete quality gate conditions without a metric
    context.prepareUpsert("delete from quality_gate_conditions where metric_id is null").execute();

    MassUpdate massUpdate = context.prepareMassUpdate();

    massUpdate.select("select qgc.uuid, m.uuid " +
      "from quality_gate_conditions qgc " +
      "join metrics m on qgc.metric_id = m.id");

    massUpdate.update("update quality_gate_conditions set metric_uuid = ? where uuid = ?");

    massUpdate.execute((row, update) -> {
      update.setString(1, row.getString(2));
      update.setString(2, row.getString(1));
      return true;
    });
  }
}
