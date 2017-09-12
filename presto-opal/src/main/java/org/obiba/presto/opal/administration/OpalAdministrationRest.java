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

package org.obiba.presto.opal.administration;

import com.facebook.presto.spi.ConnectorTableMetadata;
import com.facebook.presto.spi.PrestoException;
import com.facebook.presto.spi.SchemaTableName;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.obiba.presto.RestColumnHandle;
import org.obiba.presto.opal.OpalRest;
import org.obiba.presto.opal.model.LocaleText;
import org.obiba.presto.opal.model.Taxonomy;
import retrofit2.Response;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.facebook.presto.spi.StandardErrorCode.GENERIC_INTERNAL_ERROR;

public class OpalAdministrationRest extends OpalRest {

  private static final String[] localeTexts = new String[]{"title", "description", "keywords"};

  // schema table name vs. columns
  private Map<SchemaTableName, ConnectorTableMetadata> connectorTableMap = Maps.newConcurrentMap();

  public OpalAdministrationRest(String url, String username, String password, int cacheDelay) {
    super(url, username, password, cacheDelay);
  }

  @Override
  public synchronized ConnectorTableMetadata getTableMetadata(SchemaTableName schemaTableName) {
    initialize();
    if (connectorTableMap.containsKey(schemaTableName)) return connectorTableMap.get(schemaTableName);
    ConnectorTableMetadata connectorTableMetadata;
    if ("taxonomy".equals(schemaTableName.getTableName()))
      connectorTableMetadata = new TaxonomyTableMetadata(schemaTableName, opalConfCache);
    else if ("vocabulary".equals(schemaTableName.getTableName()))
      connectorTableMetadata = new VocabularyTableMetadata(schemaTableName, opalConfCache);
    else if ("term".equals(schemaTableName.getTableName()))
      connectorTableMetadata = new TermTableMetadata(schemaTableName, opalConfCache);
    else
      throw new RuntimeException("Unknown opal system schema table: " + schemaTableName);
    connectorTableMap.put(schemaTableName, connectorTableMetadata);
    return connectorTableMetadata;
  }

  @Override
  public List<String> listSchemas() {
    return ImmutableList.of("system");
  }

  @Override
  public List<SchemaTableName> listTables(String schema) {
    if ("system".equals(schema))
      return ImmutableList.of(new SchemaTableName(schema, "taxonomy"),
          new SchemaTableName(schema, "vocabulary"),
          new SchemaTableName(schema, "term"));
    else
      return Lists.newArrayList();
  }

  @Override
  public Collection<? extends List<?>> getRows(SchemaTableName schemaTableName, List<RestColumnHandle> restColumnHandles) {
    initialize();
    try {
      // TODO use the tuple domain constraints
      Response<List<Taxonomy>> execute = service.listTaxonomies(token).execute();
      if (!execute.isSuccessful())
        throw new IllegalStateException("Unable to read taxonomies: " + execute.message());
      List<String> columnNames = restColumnHandles.stream().map(RestColumnHandle::getName).collect(Collectors.toList());
      if ("taxonomy".equals(schemaTableName.getTableName()))
        return getTaxonomyRows(columnNames, execute.body());
      if ("vocabulary".equals(schemaTableName.getTableName()))
        return getVocabularyRows(columnNames, execute.body());
      if ("term".equals(schemaTableName.getTableName()))
        return getTermRows(columnNames, execute.body());
    } catch (IOException e) {
      throw new PrestoException(GENERIC_INTERNAL_ERROR, e);
    }
    throw new PrestoException(GENERIC_INTERNAL_ERROR, "Unknown opal system schema table: " + schemaTableName);
  }

  private Collection<? extends List<?>> getTaxonomyRows(List<String> columnNames, List<Taxonomy> taxonomies) {
    return taxonomies.stream().map(taxo -> {
      List<Object> row = Lists.newArrayList();
      for (String colName : columnNames) {
        if ("name".equals(colName)) row.add(taxo.getName());
        else if ("author".equals(colName)) row.add(taxo.getAuthor());
        else if ("license".equals(colName)) row.add(taxo.getLicense());
        else if (colName.startsWith("title:"))
          row.add(findText(taxo.getTitle(), extractLocale(colName)));
        else if (colName.startsWith("description:"))
          row.add(findText(taxo.getDescription(), extractLocale(colName)));
        else if (colName.startsWith("keywords:"))
          row.add(findText(taxo.getKeywords(), extractLocale(colName)));
        else row.add(null); // TODO parse attribute
      }
      return row;
    }).collect(Collectors.toList());
  }

  private Collection<? extends List<?>> getVocabularyRows(List<String> columnNames, List<Taxonomy> taxonomies) {
    Collection<List<?>> rows = Lists.newArrayList();
    taxonomies.forEach(taxo ->
        taxo.getVocabularies().forEach(voc -> {
          List<Object> row = Lists.newArrayList();
          for (String colName : columnNames) {
            if ("name".equals(colName)) row.add(voc.getName());
            else if ("taxonomy".equals(colName)) row.add(taxo.getName());
            else if (colName.startsWith("title:"))
              row.add(findText(voc.getTitle(), extractLocale(colName)));
            else if (colName.startsWith("description:"))
              row.add(findText(voc.getDescription(), extractLocale(colName)));
            else if (colName.startsWith("keywords:"))
              row.add(findText(voc.getKeywords(), extractLocale(colName)));
            else row.add(null); // TODO parse attribute
          }
          rows.add(row);
        }));
    return rows;
  }

  private Collection<? extends List<?>> getTermRows(List<String> columnNames, List<Taxonomy> taxonomies) {
    Collection<List<?>> rows = Lists.newArrayList();
    taxonomies.forEach(taxo ->
        taxo.getVocabularies().forEach(voc -> {
          voc.getTerms().forEach(term -> {
            List<Object> row = Lists.newArrayList();
            for (String colName : columnNames) {
              if ("name".equals(colName)) row.add(term.getName());
              else if ("taxonomy".equals(colName)) row.add(taxo.getName());
              else if ("vocabulary".equals(colName)) row.add(voc.getName());
              else if (colName.startsWith("title:"))
                row.add(findText(term.getTitle(), extractLocale(colName)));
              else if (colName.startsWith("description:"))
                row.add(findText(term.getDescription(), extractLocale(colName)));
              else if (colName.startsWith("keywords:"))
                row.add(findText(term.getKeywords(), extractLocale(colName)));
              else row.add(null); // TODO parse attribute
            }
            rows.add(row);
          });
        }));
    return rows;
  }

  private String extractLocale(String columnName) {
    return Splitter.on(":").splitToList(columnName).get(1);
  }

  private String findText(List<LocaleText> texts, String locale) {
    if (texts == null || texts.isEmpty()) return null;
    return texts.stream().filter(lt -> locale.equals(lt.getLocale())).map(LocaleText::getText).findFirst().orElse(null);
  }

}