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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpalValueSets {
  private final String entityType;
  private final List<String> variables;
  private final List<OpalValueSet> valueSets;

  public OpalValueSets(@JsonProperty("entityType") String entityType,
                       @JsonProperty("variables") List<String> variables,
                       @JsonProperty("valueSets") List<OpalValueSet> valueSets) {
    this.entityType = entityType;
    this.variables = variables;
    this.valueSets = valueSets;
  }

  public String getEntityType() {
    return entityType;
  }

  public List<String> getVariables() {
    return variables;
  }

  public List<OpalValueSet> getValueSets() {
    return valueSets;
  }

  /**
   * Get values as strings ordered by the given variable names.
   *
   * @param opalVariables
   * @return
   */
  public Collection<List<String>> getStringValues(List<OpalVariable> opalVariables) {
    List<Integer> positions = opalVariables.stream().map(var -> variables.indexOf(var.getName())).collect(Collectors.toList());
    return valueSets.stream().map(vs -> vs.getStringValues(positions)).collect(Collectors.toList());
  }

}
