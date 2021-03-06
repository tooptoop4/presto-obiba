/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.obiba.presto.opal.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Datasource {
  private final String name;
  private final List<String> tableNames;
  private final String type;
  private final Timestamps timestamps;

  public Datasource(@JsonProperty("name") String name,
                    @JsonProperty("table") List<String> tableNames,
                    @JsonProperty("type") String type,
                    @JsonProperty("timestamps") Timestamps timestamps) {
    this.name = name;
    this.tableNames = tableNames;
    this.type = type;
    this.timestamps = timestamps;
  }

  public String getName() {
    return name;
  }

  public List<String> getTableNames() {
    return tableNames == null ? Lists.newArrayList() : tableNames;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public String getType() {
    return type;
  }
}
