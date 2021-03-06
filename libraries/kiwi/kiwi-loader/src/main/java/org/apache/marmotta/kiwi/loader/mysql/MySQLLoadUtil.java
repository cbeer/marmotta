/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.marmotta.kiwi.loader.mysql;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.marmotta.kiwi.loader.csv.LanguageProcessor;
import org.apache.marmotta.kiwi.loader.csv.NodeIDProcessor;
import org.apache.marmotta.kiwi.loader.csv.NodeTypeProcessor;
import org.apache.marmotta.kiwi.loader.csv.SQLBooleanProcessor;
import org.apache.marmotta.kiwi.loader.csv.SQLTimestampProcessor;
import org.apache.marmotta.kiwi.model.rdf.KiWiAnonResource;
import org.apache.marmotta.kiwi.model.rdf.KiWiBooleanLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiDateLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiDoubleLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiIntLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiNode;
import org.apache.marmotta.kiwi.model.rdf.KiWiStringLiteral;
import org.apache.marmotta.kiwi.model.rdf.KiWiTriple;
import org.apache.marmotta.kiwi.model.rdf.KiWiUriResource;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.encoder.DefaultCsvEncoder;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public class MySQLLoadUtil {

    private static Logger log = LoggerFactory.getLogger(MySQLLoadUtil.class);

    final static CellProcessor[] nodeProcessors = new CellProcessor[] {
            new NotNull(),                             // node ID
            new NodeTypeProcessor(),                  // ntype
            new NotNull(),                            // svalue
            new Optional(),                           // dvalue
            new Optional(),                           // ivalue
            new SQLTimestampProcessor(),              // tvalue
            new Optional(new SQLBooleanProcessor()),  // bvalue
            new Optional(new NodeIDProcessor()),      // ltype
            new Optional(new LanguageProcessor()),    // lang
            new SQLTimestampProcessor(),              // createdAt
    };


    final static CellProcessor[] tripleProcessors = new CellProcessor[] {
            new NotNull(),                             // triple ID
            new NodeIDProcessor(),                    // subject
            new NodeIDProcessor(),                    // predicate
            new NodeIDProcessor(),                    // object
            new Optional(new NodeIDProcessor()),      // context
            new Optional(new NodeIDProcessor()),      // creator
            new SQLBooleanProcessor(),                // inferred
            new SQLBooleanProcessor(),                // deleted
            new SQLTimestampProcessor(),              // createdAt
            new SQLTimestampProcessor(),              // deletedAt
    };


    // PostgreSQL expects the empty string to be quoted to distinguish between null and empty
    final static CsvPreference nodesPreference = new CsvPreference.Builder('"', ',', "\r\n").useEncoder(new DefaultCsvEncoder() {
        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(String input, CsvContext context, CsvPreference preference) {
            if("".equals(input)) {
                return "\"\"";
            } else {
                return super.encode(input, context, preference);
            }
        }
    }).build();



    public static InputStream flushTriples(Iterable<KiWiTriple> tripleBacklog) throws IOException {
        StringWriter out = new StringWriter();
        CsvListWriter writer = new CsvListWriter(out, CsvPreference.STANDARD_PREFERENCE);

        // reuse the same array to avoid unnecessary object allocation
        Object[] rowArray = new Object[10];
        List<Object> row = Arrays.asList(rowArray);

        for(KiWiTriple t : tripleBacklog) {
            rowArray[0] = t.getId();
            rowArray[1] = t.getSubject();
            rowArray[2] = t.getPredicate();
            rowArray[3] = t.getObject();
            rowArray[4] = t.getContext();
            rowArray[5] = t.getCreator();
            rowArray[6] = t.isInferred();
            rowArray[7] = t.isDeleted();
            rowArray[8] = t.getCreated();
            rowArray[9] = t.getDeletedAt();

            writer.write(row, tripleProcessors);
        }
        writer.close();

        return IOUtils.toInputStream(out.toString());
    }



    public static InputStream flushNodes(Iterable<KiWiNode> nodeBacklog) throws IOException {
        StringWriter out = new StringWriter();
        CsvListWriter writer = new CsvListWriter(out, nodesPreference);

        // reuse the same array to avoid unnecessary object allocation
        Object[] rowArray = new Object[10];
        List<Object> row = Arrays.asList(rowArray);

        for(KiWiNode n : nodeBacklog) {
            if(n instanceof KiWiUriResource) {
                KiWiUriResource u = (KiWiUriResource)n;
                createNodeList(rowArray, u.getId(), u.getClass(), u.stringValue(), null, null, null, null, null, null, u.getCreated());
            } else if(n instanceof KiWiAnonResource) {
                KiWiAnonResource a = (KiWiAnonResource)n;
                createNodeList(rowArray, a.getId(), a.getClass(), a.stringValue(), null, null, null, null, null, null, a.getCreated());
            } else if(n instanceof KiWiIntLiteral) {
                KiWiIntLiteral l = (KiWiIntLiteral)n;
                createNodeList(rowArray, l.getId(), l.getClass(), l.getContent(), l.getDoubleContent(), l.getIntContent(), null, null, l.getDatatype(), l.getLocale(), l.getCreated());
            } else if(n instanceof KiWiDoubleLiteral) {
                KiWiDoubleLiteral l = (KiWiDoubleLiteral)n;
                createNodeList(rowArray, l.getId(), l.getClass(), l.getContent(), l.getDoubleContent(), null, null, null, l.getDatatype(), l.getLocale(), l.getCreated());
            } else if(n instanceof KiWiBooleanLiteral) {
                KiWiBooleanLiteral l = (KiWiBooleanLiteral)n;
                createNodeList(rowArray, l.getId(), l.getClass(), l.getContent(), null, null, null, l.booleanValue(), l.getDatatype(), l.getLocale(), l.getCreated());
            } else if(n instanceof KiWiDateLiteral) {
                KiWiDateLiteral l = (KiWiDateLiteral)n;
                createNodeList(rowArray, l.getId(), l.getClass(), l.getContent(), null, null, l.getDateContent(), null, l.getDatatype(), l.getLocale(), l.getCreated());
            } else if(n instanceof KiWiStringLiteral) {
                KiWiStringLiteral l = (KiWiStringLiteral)n;

                Double dbl_value = null;
                Long   lng_value = null;
                if(l.getContent().length() < 64 && NumberUtils.isNumber(l.getContent()))  {
                    try {
                        dbl_value = Double.parseDouble(l.getContent());
                        lng_value = Long.parseLong(l.getContent());
                    } catch (NumberFormatException ex) {
                        // ignore, keep NaN
                    }
                }
                createNodeList(rowArray, l.getId(), l.getClass(), l.getContent(), dbl_value, lng_value, null, null, l.getDatatype(), l.getLocale(), l.getCreated());
            } else {
                log.warn("unknown node type, cannot flush to import stream: {}", n.getClass());
            }

            writer.write(row, nodeProcessors);
        }
        writer.close();

        return IOUtils.toInputStream(out.toString());
    }

    private static void createNodeList(Object[] a, Long id, Class type, String content, Double dbl, Long lng, Date date, Boolean bool, URI dtype, Locale lang, Date created) {
        a[0] = id;
        a[1] = type;
        a[2] = content;
        a[3] = dbl;
        a[4] = lng;
        a[5] = date;
        a[6] = bool;
        a[7] = dtype;
        a[8] = lang;
        a[9] = created;
    }

}
