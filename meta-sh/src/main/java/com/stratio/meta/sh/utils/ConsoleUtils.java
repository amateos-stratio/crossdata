/*
 * Licensed to STRATIO (C) under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  The STRATIO (C) licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.stratio.meta.sh.utils;

import com.stratio.meta.common.data.CassandraResultSet;
import com.stratio.meta.common.data.Cell;
import com.stratio.meta.common.data.ResultSet;
import com.stratio.meta.common.data.Row;
import com.stratio.meta.common.metadata.structures.ColumnMetadata;
import com.stratio.meta.common.result.CommandResult;
import com.stratio.meta.common.result.ConnectResult;
import com.stratio.meta.common.result.ErrorResult;
import com.stratio.meta.common.result.QueryResult;
import com.stratio.meta.common.result.Result;
import com.stratio.meta2.common.api.Manifest;
import com.stratio.meta2.common.api.generated.connector.ConnectorFactory;
import com.stratio.meta2.common.api.generated.connector.ConnectorType;
import com.stratio.meta2.common.api.generated.connector.OptionalPropertiesType;
import com.stratio.meta2.common.api.generated.datastore.ClusterType;
import com.stratio.meta2.common.api.generated.datastore.DataStoreFactory;
import com.stratio.meta2.common.api.generated.datastore.DataStoreType;
import com.stratio.meta2.common.api.generated.datastore.HostsType;
import com.stratio.meta2.common.api.generated.datastore.RequiredPropertiesType;

import jline.console.ConsoleReader;
import jline.console.history.History;
import jline.console.history.MemoryHistory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ConsoleUtils {

  /**
   * Class logger.
   */
  private static final Logger LOG = Logger.getLogger(ConsoleUtils.class);

  /**
   * Number of days a history entry is maintained.
   */
  private static final int DAYS_HISTORY_ENTRY_VALID = 30;

  /**
   * Private class constructor as all methods are static.
   */
  private ConsoleUtils() {

  }

  /**
   * Convert Result {@link com.stratio.meta.common.result.Result} structure to String.
   * 
   * @param result {@link com.stratio.meta.common.result.Result} from execution.
   * @return String representing the result.
   */
  public static String stringResult(Result result) {
    if (ErrorResult.class.isInstance(result)) {
      return ErrorResult.class.cast(result).getErrorMessage();
    }
    if (result instanceof QueryResult) {
      QueryResult queryResult = (QueryResult) result;
      return stringQueryResult(queryResult);
    } else if (result instanceof CommandResult) {
      CommandResult commandResult = (CommandResult) result;
      return String.class.cast(commandResult.getResult());
    } else if (result instanceof ConnectResult) {
      ConnectResult connectResult = (ConnectResult) result;
      return String.valueOf("Connected with SessionId=" + connectResult.getSessionId());
    } else {
      return "Unknown result";
    }
  }

  /**
   * Convert QueryResult {@link com.stratio.meta.common.result.QueryResult} structure to String.
   * 
   * @param queryResult {@link com.stratio.meta.common.result.QueryResult} from execution.
   * @return String representing the result.
   */

  private static String stringQueryResult(QueryResult queryResult){
    if(queryResult.getResultSet().isEmpty()){
      return System.lineSeparator() + "OK";
    }

    CassandraResultSet resultSet = (CassandraResultSet) queryResult.getResultSet();

    Map<String, Integer> colWidths = calculateColWidths(resultSet);

    String bar =
        StringUtils.repeat('-', getTotalWidth(colWidths) + (colWidths.values().size() * 3) + 1);

    StringBuilder sb = new StringBuilder(System.lineSeparator());
    sb.append("Partial result: ");
    sb.append(!queryResult.isLastResultSet());
    sb.append(System.lineSeparator());
    sb.append(bar).append(System.lineSeparator());
    sb.append("| ");
    for (ColumnMetadata columnMetadata : resultSet.getColumnMetadata()) {
      sb.append(
          StringUtils.rightPad("\033[34;1m" + columnMetadata.getColumnNameToShow() + "\033[0m ",
              colWidths.get(columnMetadata.getColumnName()) + 12)).append("| ");
    }

    sb.append(System.lineSeparator());
    sb.append(bar);
    sb.append(System.lineSeparator());

    for (Row row : resultSet) {
      sb.append("| ");
      for (Map.Entry<String, Cell> entry : row.getCells().entrySet()) {
        String str = String.valueOf(entry.getValue().getValue());
        sb.append(StringUtils.rightPad(str, colWidths.get(entry.getKey())));
        sb.append(" | ");
      }
      sb.append(System.lineSeparator());
    }
    sb.append(bar).append(System.lineSeparator());
    return sb.toString();
  }

  /**
   * In order to print the result, this method calculates the maximum width of every column.
   * 
   * @param resultSet structure representing the result of a execution.
   * @return Map<String, Integer> where the key is the name of the column and Integer is the maximum
   *         width.
   */
  private static Map<String, Integer> calculateColWidths(ResultSet resultSet) {
    Map<String, Integer> colWidths = new HashMap<>();

    // Get column names or aliases width
    CassandraResultSet cassandraResultSet = (CassandraResultSet) resultSet;
    for (ColumnMetadata columnMetadata : cassandraResultSet.getColumnMetadata()) {
      colWidths.put(columnMetadata.getColumnName(), columnMetadata.getColumnNameToShow().length());
    }

    // Find widest cell content of every column
    for (Row row : resultSet) {
      for (String key : row.getCells().keySet()) {
        String cellContent = String.valueOf(row.getCell(key).getValue());
        int currentWidth = colWidths.get(key);
        if (cellContent.length() > currentWidth) {
          colWidths.put(key, cellContent.length());
        }
      }
    }

    return colWidths;
  }

  /**
   * In order to create a separator line in tables, this method calculates the total width of a
   * table.
   * 
   * @param colWidths columns widths of a table.
   * @return total width of a table.
   */
  private static int getTotalWidth(Map<String, Integer> colWidths) {
    int totalWidth = 0;
    for (int width : colWidths.values()) {
      totalWidth += width;
    }
    return totalWidth;
  }

  /**
   * Get previous history of the Meta console from a file.
   * 
   * @param console Meta console created from a JLine console
   * @param sdf Simple Date Format to read dates from history file
   * @return File inserted in the JLine console with the previous history
   * @throws IOException
   */
  public static File retrieveHistory(ConsoleReader console, SimpleDateFormat sdf)
      throws IOException {
    Date today = new Date();
    String workingDir = System.getProperty("user.home");
    File dir = new File(workingDir, ".meta");
    if (!dir.exists() && !dir.mkdir()) {
      LOG.error("Cannot create history directory: " + dir.getAbsolutePath());
    }
    File file = new File(dir.getPath() + "/history.txt");
    if (!file.exists() && !file.createNewFile()) {
      LOG.error("Cannot create history file: " + file.getAbsolutePath());
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Retrieving history from " + file.getAbsolutePath());
    }
    BufferedReader br =
        new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
    History oldHistory = new MemoryHistory();
    DateTime todayDate = new DateTime(today);
    String line;
    String[] lineArray;
    Date lineDate;
    String lineStatement;
    try {
      while ((line = br.readLine()) != null) {
        lineArray = line.split("\\|");
        lineDate = sdf.parse(lineArray[0]);
        if (Days.daysBetween(new DateTime(lineDate), todayDate).getDays() < DAYS_HISTORY_ENTRY_VALID) {
          lineStatement = lineArray[1];
          oldHistory.add(lineStatement);
        }
      }
    } catch (ParseException ex) {
      LOG.error("Cannot parse date", ex);
    } catch (Exception ex) {
      LOG.error("Cannot read all the history", ex);
    } finally {
      br.close();
    }
    console.setHistory(oldHistory);
    LOG.info("History retrieved");
    return file;
  }

  /**
   * This method save history extracted from the Meta console to be persisted in the disk.
   * 
   * @param console Meta console created from a JLine console
   * @param file represents the file to be created of updated with the statements from the current
   *        session
   * @param sdf Simple Date Format to create dates for the history file
   * @throws IOException file couldn't be created or read
   */
  public static void saveHistory(ConsoleReader console, File file, SimpleDateFormat sdf)
      throws IOException {
    boolean created = file.createNewFile();
    OutputStreamWriter isr;
    if (created) {
      isr = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
    } else {
      isr = new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8");
    }
    try (BufferedWriter bufferWriter = new BufferedWriter(isr)) {
      History history = console.getHistory();
      ListIterator<History.Entry> histIter = history.entries();
      while (histIter.hasNext()) {
        History.Entry entry = histIter.next();
        bufferWriter.write(sdf.format(new Date()));
        bufferWriter.write("|");
        bufferWriter.write(entry.value().toString());
        bufferWriter.newLine();
      }
      bufferWriter.flush();
    }
  }

  public static Manifest parseFromXmlToManifest(int manifestType, String path) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(new File(path));
      if(manifestType == Manifest.TYPE_DATASTORE){
        return parseFromXmlToDataStoreManifest(document);
      } else {
        return parseFromXmlToConnectorManifest(document);
      }
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
      return null;
    } catch (SAXException e) {
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static DataStoreType parseFromXmlToDataStoreManifest(Document document) {
    DataStoreFactory dataStoreFactory = new DataStoreFactory();
    DataStoreType manifest = dataStoreFactory.createDataStoreType();
    // NAME
    manifest.setName(document.getElementsByTagName("Name").item(0).getNodeValue());
    // VERSION
    manifest.setVersion(document.getElementsByTagName("Version").item(0).getNodeValue());
    // REQUIRED PROPERTIES
    RequiredPropertiesType requiredPropertiesType = dataStoreFactory.createRequiredPropertiesType();
    NodeList requiredPropertiesNode = document.getElementsByTagName("RequiredProperties");
    Element requiredPropertiesElement = (Element) requiredPropertiesNode.item(0);
      // CLUSTER
    ClusterType clusterType = dataStoreFactory.createClusterType();
    Element clusterElement = (Element) requiredPropertiesElement.getElementsByTagName("cluster").item(0);
        // NAME
    clusterType.setName(clusterElement.getElementsByTagName("name").item(0).getNodeValue());
        // HOSTS
    HostsType hostsType = dataStoreFactory.createHostsType();
    Element hostsElement = (Element) clusterElement.getElementsByTagName("hosts").item(0);
          // HOST
    hostsType.setHost(hostsElement.getElementsByTagName("host").item(0).getNodeValue());
          // PORT
    hostsType.setPort(hostsElement.getElementsByTagName("port").item(0).getNodeValue());

    /*clusterType.setHosts(hostsType);
    requiredPropertiesType.setCluster(clusterType);
    manifest.setRequiredProperties(requiredPropertiesType);

    // OPTIONAL PROPERTIES
    OptionalPropertiesType optionalPropertiesType = dataStoreFactory.createOptionalPropertiesType();
    manifest.setOptionalProperties(
        document.getElementsByTagName("OptionalProperties").item(0).getNodeValue());*/
    return manifest;
  }

  private static ConnectorType parseFromXmlToConnectorManifest(Document document) {
    return null;
  }

  /*public static Manifest parseFromXmlToManifest(int manifestType, String path) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(new File(path));
      NodeList nodeList = document.getDocumentElement().getChildNodes();
      if(manifestType == Manifest.TYPE_DATASTORE){
        return parseFromXmlToDataStoreManifest(nodeList);
      } else {
        return parseFromXmlToConnectorManifest(nodeList);
      }
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
      return null;
    } catch (SAXException e) {
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private static Object parsePartialXmlToObject(int depth, NodeList nodeList){
    Object object = null;
    if(depth == 1){
      DataStoreFactory dataStoreFactory = new DataStoreFactory();
      object = dataStoreFactory.createDataStoreType();
    }
    for(int i = 0; i < nodeList.getLength(); i++){
      Node node = nodeList.item(i);
      if (node.getNodeType() == Node.TEXT_NODE) {
        String nodeValue = node.getNodeValue().trim();
        if(!nodeValue.isEmpty()){
          System.out.println(">>>>>>> TRACE: nodeValue = "+nodeValue);
        }
        return null;
      } else {
        Element elem = (Element) node;
        System.out.println(">>>>>>> TRACE: elem = " + elem.getTagName());
        NodeList newNodeList = elem.getChildNodes();
        parsePartialXmlToObject(depth+1, newNodeList);
        return null;
      }
    }
    return object;
  }

  private static Manifest parseFromXmlToDataStoreManifest(NodeList nodeList){
    return (DataStoreType) parsePartialXmlToObject(1, nodeList);
  }

  private static Manifest parseFromXmlToConnectorManifest(NodeList nodeList) {
    ConnectorFactory connectorFactory = new ConnectorFactory();
    ConnectorType conManifest = connectorFactory.createConnectorType();
    return conManifest;
  }*/

}
