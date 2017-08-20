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

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpalVariable {
  private final String name;
  private final String entityType;
  private final String valueType;
  private final boolean repeatable;
  private final int index;

  public OpalVariable(@JsonProperty("name") String name,
                      @JsonProperty("entityType") String entityType,
                      @JsonProperty("valueType") String valueType,
                      @JsonProperty("repeatable") boolean repeatable,
                      @JsonProperty("index") int index) {
    this.name = name;
    this.entityType = entityType;
    this.valueType = valueType;
    this.repeatable = repeatable;
    this.index = index;
  }

  public String getName() {
    return name;
  }

  public String getEntityType() {
    return entityType;
  }

  public String getValueType() {
    return valueType;
  }

  public boolean isRepeatable() {
    return repeatable;
  }

  public int getIndex() {
    return index;
  }
}
