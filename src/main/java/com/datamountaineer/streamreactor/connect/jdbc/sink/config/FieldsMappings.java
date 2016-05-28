/**
 * Copyright 2015 Datamountaineer.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package com.datamountaineer.streamreactor.connect.jdbc.sink.config;

import com.datamountaineer.streamreactor.connect.jdbc.common.ParameterValidator;
import com.google.common.base.Joiner;
import org.apache.kafka.connect.sink.SinkRecord;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains the Schema field names to consider as well as their mappings to the target table columns
 */
public final class FieldsMappings {

  public final static String CONNECT_TOPIC_COLUMN = "__connect_topic";
  public final static String CONNECT_OFFSET_COLUMN = "__connect_offset";
  public final static String CONNECT_PARTITION_COLUMN = "__connect_partition";
  private final String tableName;
  private final String incomingTopic;
  private final boolean allFieldsIncluded;
  private final Map<String, FieldAlias> mappings;

  private final boolean autoCreateTable;
  private final boolean evolveTableSchema;

  /**
   * Creates a new instance of FieldsMappings
   *
   * @param tableName         - The target RDBMS table to insert the records into
   * @param incomingTopic     - The source Kafka topic
   * @param allFieldsIncluded - If set to true it considers all fields in the payload; if false it will rely on the
   *                          defined fields to include
   * @param mappings          - Provides the list of schema fields to include and their alias. It could be set to Map.empty if all fields
   *                          are to be included.
   */
  public FieldsMappings(final String tableName,
                        final String incomingTopic,
                        final boolean allFieldsIncluded,
                        final Map<String, FieldAlias> mappings) {
    this(tableName, incomingTopic, allFieldsIncluded, mappings, false, false);
  }

  /**
   * Creates a new instance of FieldsMappings
   *
   * @param tableName         - The target RDBMS table to insert the records into
   * @param incomingTopic     - The source Kafka topic
   * @param allFieldsIncluded - If set to true it considers all fields in the payload; if false it will rely on the
   *                          defined fields to include
   * @param mappings          - Provides the map of fields to include and their alias. It could be set to Map.empty if all fields
   *                          are to be included.
   * @param evolveTableSchema - If true it allows auto table creation and table evolution
   */
  public FieldsMappings(final String tableName,
                        final String incomingTopic,
                        final boolean allFieldsIncluded,
                        final Map<String, FieldAlias> mappings,
                        final boolean autoCreateTable,
                        final boolean evolveTableSchema) {

    ParameterValidator.notNullOrEmpty(tableName, "tableName");
    ParameterValidator.notNullOrEmpty(incomingTopic, "incomingTopic");
    ParameterValidator.notNull(mappings, "map");

    this.tableName = tableName;
    this.incomingTopic = incomingTopic;
    this.allFieldsIncluded = allFieldsIncluded;
    this.mappings = mappings;
    this.autoCreateTable = autoCreateTable;
    this.evolveTableSchema = evolveTableSchema;
  }


  public FieldsMappings(final String tableName, final String incomingTopic) {
    this(tableName, incomingTopic, true, new HashMap<String, FieldAlias>());
  }

  /**
   * If set to true all the incoming SinkRecord payload fields are considered for inserting into the table.
   *
   * @return true - if all payload fields should be included; false - otherwise
   */
  public boolean areAllFieldsIncluded() {
    return allFieldsIncluded;
  }

  public Map<String, FieldAlias> getMappings() {
    return mappings;
  }

  /**
   * Returns the source Kafka topic.
   *
   * @return
   */
  public String getIncomingTopic() {
    return incomingTopic;
  }

  /**
   * Returns the target database table name.
   *
   * @return
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * Returns true if any of the filed mappings provided are part of the table primary key.
   *
   * @return
   */
  public boolean hasPrimaryKeys() {
    for (Map.Entry<String, FieldAlias> e : mappings.entrySet()) {
      if (e.getValue().isPrimaryKey())
        return true;
    }
    return false;
  }

  public boolean autoCreateTable() {
    return autoCreateTable;
  }

  public boolean evolveTableSchema() {
    return evolveTableSchema;
  }

  @Override
  public String toString() {
    Joiner.MapJoiner mapJoiner = Joiner.on(",\n").withKeyValueSeparator("=");
    return "{\n" +
            "topic:" + incomingTopic + "\n" +
            "table:" + tableName + "\n" +
            "auto-create:" + autoCreateTable + "\n" +
            "evolve-schema:" + evolveTableSchema + "\n" +
            "include-all-fields:" + allFieldsIncluded + "\n" +
            "mappings:" + mapJoiner.join(mappings) + "\n" +
            "}";
  }

  /**
   * Constructs the primary key value when the table is set with autocreate and no fields are specified
   *
   * @param record - The connect record structure
   * @return - The string formed by topic.partition.offset
   */
  public static String generateConnectAutoPKValue(final SinkRecord record) {
    return String.format("%s.%d.%d", record.topic(), record.kafkaPartition(), record.kafkaOffset());
  }
}
