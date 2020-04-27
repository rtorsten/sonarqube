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
package org.sonar.db.qualitygate;

import java.util.Collection;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.System2;
import org.sonar.core.util.Uuids;
import org.sonar.db.DbSession;
import org.sonar.db.DbTester;
import org.sonar.db.metric.MetricDto;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;

public class QualityGateConditionDaoTest {

  @Rule
  public DbTester dbTester = DbTester.create(System2.INSTANCE);

  private DbSession dbSession = dbTester.getSession();
  private QualityGateConditionDao underTest = dbTester.getDbClient().gateConditionDao();

  @Test
  public void testInsert() {
    QualityGateConditionDto newCondition = insertQGCondition(1L, "2", "GT", "20");

    assertThat(newCondition.getUuid()).isNotNull();
    QualityGateConditionDto actual = underTest.selectByUuid(newCondition.getUuid(), dbSession);
    assertEquals(actual, newCondition);
  }

  @Test
  public void testSelectForQualityGate() {
    long qg1Id = 1L;
    long qg2Id = 2L;
    int qg1Conditions = 2 + new Random().nextInt(5);
    int qg2Conditions = 10 + new Random().nextInt(5);

    IntStream.range(0, qg1Conditions).forEach(i -> insertQGCondition(qg1Id));
    IntStream.range(0, qg2Conditions).forEach(i -> insertQGCondition(qg2Id));

    Collection<QualityGateConditionDto> conditions = underTest.selectForQualityGate(dbSession, qg1Id);
    assertThat(conditions).hasSize(qg1Conditions);
    assertThat(conditions)
      .extracting("uuid")
      .containsExactly(conditions.stream()
        .sorted(Comparator.comparing(QualityGateConditionDto::getCreatedAt))
        .map(QualityGateConditionDto::getUuid).toArray());

    conditions = underTest.selectForQualityGate(dbSession, qg2Id);
    assertThat(conditions).hasSize(qg2Conditions);
    assertThat(conditions)
      .extracting("uuid")
      .containsExactly(conditions.stream()
        .sorted(Comparator.comparing(QualityGateConditionDto::getCreatedAt))
        .map(QualityGateConditionDto::getUuid)
        .toArray());

    assertThat(underTest.selectForQualityGate(dbSession, 5)).isEmpty();
  }

  @Test
  public void testSelectByUuid() {
    QualityGateConditionDto condition = insertQGCondition(1L, "2", "GT", "20");

    assertEquals(underTest.selectByUuid(condition.getUuid(), dbSession), condition);
    assertThat(underTest.selectByUuid("uuid1", dbSession)).isNull();
  }

  @Test
  public void testDelete() {
    QualityGateConditionDto condition1 = insertQGCondition(2L);
    QualityGateConditionDto condition2 = insertQGCondition(3L);

    underTest.delete(condition1, dbSession);
    dbSession.commit();

    assertThat(underTest.selectByUuid(condition1.getUuid(), dbSession)).isNull();
    assertThat(underTest.selectByUuid(condition2.getUuid(), dbSession)).isNotNull();
  }

  @Test
  public void testUpdate() {
    QualityGateConditionDto condition1 = insertQGCondition(2L);
    QualityGateConditionDto condition2 = insertQGCondition(3L);

    QualityGateConditionDto newCondition1 = new QualityGateConditionDto()
      .setUuid(condition1.getUuid())
      .setQualityGateId(condition1.getQualityGateId())
      .setMetricUuid("7")
      .setOperator(">")
      .setErrorThreshold("80");
    underTest.update(newCondition1, dbSession);
    dbSession.commit();


    assertEquals(underTest.selectByUuid(condition1.getUuid(), dbSession), newCondition1);
    assertEquals(underTest.selectByUuid(condition2.getUuid(), dbSession), condition2);
  }

  @Test
  public void shouldCleanConditions() {
    MetricDto enabledMetric = dbTester.measures().insertMetric(t -> t.setEnabled(true));
    MetricDto disabledMetric = dbTester.measures().insertMetric(t -> t.setEnabled(false));
    QualityGateConditionDto condition1 = insertQGCondition(1L, enabledMetric.getUuid());
    QualityGateConditionDto condition2 = insertQGCondition(1L, disabledMetric.getUuid());
    QualityGateConditionDto condition3 = insertQGCondition(1L, "299");

    underTest.deleteConditionsWithInvalidMetrics(dbTester.getSession());
    dbTester.commit();


    assertThat(underTest.selectByUuid(condition1.getUuid(), dbSession)).isNotNull();
    assertThat(underTest.selectByUuid(condition2.getUuid(), dbSession)).isNull();
    assertThat(underTest.selectByUuid(condition3.getUuid(), dbSession)).isNull();
  }

  private QualityGateConditionDto insertQGCondition(long qualityGateId) {
    return insertQGCondition(qualityGateId, randomAlphabetic(2));
  }

  private QualityGateConditionDto insertQGCondition(long qualityGateId, String metricUuid) {
    return insertQGCondition(qualityGateId, metricUuid, randomAlphabetic(2), randomAlphabetic(3));
  }

  private QualityGateConditionDto insertQGCondition(long qualityGateId, String metricUuid, String operator, String threshold) {
    QualityGateConditionDto res = new QualityGateConditionDto()
      .setUuid(Uuids.create())
      .setQualityGateId(qualityGateId)
      .setMetricUuid(metricUuid)
      .setOperator(operator)
      .setErrorThreshold(threshold);
    underTest.insert(res, dbTester.getSession());
    dbTester.commit();
    return res;
  }

  private void assertEquals(QualityGateConditionDto actual, QualityGateConditionDto expected) {
    assertThat(actual.getQualityGateId()).isEqualTo(expected.getQualityGateId());
    assertThat(actual.getMetricUuid()).isEqualTo(expected.getMetricUuid());
    assertThat(actual.getOperator()).isEqualTo(expected.getOperator());
    assertThat(actual.getErrorThreshold()).isEqualTo(expected.getErrorThreshold());
  }
}
