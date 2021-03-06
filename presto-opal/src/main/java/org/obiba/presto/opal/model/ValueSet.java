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
import java.util.Map;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValueSet {
  private final String identifier;
  private final List<Map<String, Object>> values;
  private final Timestamps timestamps;

  public ValueSet(@JsonProperty("identifier") String identifier,
                  @JsonProperty("values") List<Map<String, Object>> values,
                  @JsonProperty("timestamps") Timestamps timestamps) {
    this.identifier = identifier;
    this.values = values;
    this.timestamps = timestamps;
  }

  public String getIdentifier() {
    return identifier;
  }

  public Timestamps getTimestamps() {
    return timestamps;
  }

  public List<?> getStringValues(List<Integer> positions) {
    return positions.stream().map(pos -> {
      if (pos < 0) return identifier;
      if (pos >= values.size()) return null;
      Map<String, Object> valueMap = values.get(pos);
      if (valueMap.containsKey("length")) return asString(valueMap.get("length")); // size of the binary data
      if (valueMap.containsKey("value")) return asString(valueMap.get("value"));
      if (valueMap.containsKey("values")) return asString(valueMap.get("values"));
      return null;
    }).collect(Collectors.toList());
  }

  private Object asString(Object obj) {
    if (obj == null) return null;
    if (obj instanceof Collection)
      return ((Collection<?>) obj).stream().map(o -> {
        Map<String, Object> valueMap = (Map<String, Object>) o;
        if (valueMap.containsKey("length")) return asString(valueMap.get("length"));
        return asString(valueMap.get("value"));
      }).collect(Collectors.toList());
    return obj.toString();
  }
}
