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

package org.obiba.presto.opal.values;

import com.facebook.presto.spi.ColumnMetadata;
import com.facebook.presto.spi.type.*;
import org.obiba.presto.opal.model.Variable;

import static com.facebook.presto.spi.type.VarcharType.createUnboundedVarcharType;

public class OpalColumnMetadata extends ColumnMetadata {

  // original variable name
  private final Variable variable;

  public OpalColumnMetadata(Variable variable) {
    super(variable.getName(), variable.isRepeatable() ? createUnboundedVarcharType() : convertType(variable.getValueType()));
    this.variable = variable;
  }

  public String getVariableName() {
    return variable.getName();
  }

  private static Type convertType(String valueType) {
    if ("text".equals(valueType)) return createUnboundedVarcharType();
    if ("integer".equals(valueType) || "binary".equals(valueType)) return BigintType.BIGINT;
    if ("decimal".equals(valueType)) return DoubleType.DOUBLE;
    if ("boolean".equals(valueType)) return BooleanType.BOOLEAN;
    if ("date".equals(valueType)) return DateType.DATE;
    if ("datetime".equals(valueType)) return TimestampType.TIMESTAMP;
    return createUnboundedVarcharType();
  }
}
