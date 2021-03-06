/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.sql.expression.function.scalar.datetime;

import org.elasticsearch.xpack.sql.expression.Expression;
import org.elasticsearch.xpack.sql.expression.function.scalar.datetime.DateTimeProcessor.DateTimeExtractor;
import org.elasticsearch.xpack.sql.expression.gen.processor.Processor;
import org.elasticsearch.xpack.sql.expression.gen.script.ParamsBuilder;
import org.elasticsearch.xpack.sql.expression.gen.script.ScriptTemplate;
import org.elasticsearch.xpack.sql.tree.Location;
import org.elasticsearch.xpack.sql.type.DataType;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

import static org.elasticsearch.xpack.sql.expression.gen.script.ParamsBuilder.paramsBuilder;

public abstract class DateTimeFunction extends BaseDateTimeFunction {

    private final DateTimeExtractor extractor;

    DateTimeFunction(Location location, Expression field, ZoneId zoneId, DateTimeExtractor extractor) {
        super(location, field, zoneId);
        this.extractor = extractor;
    }

    @Override
    protected Object doFold(ZonedDateTime dateTime) {
        return dateTimeChrono(dateTime, extractor.chronoField());
    }

    public static Integer dateTimeChrono(ZonedDateTime dateTime, String tzId, String chronoName) {
        ZonedDateTime zdt = dateTime.withZoneSameInstant(ZoneId.of(tzId));
        return dateTimeChrono(zdt, ChronoField.valueOf(chronoName));
    }

    private static Integer dateTimeChrono(ZonedDateTime dateTime, ChronoField field) {
        return Integer.valueOf(dateTime.get(field));
    }
    
    @Override
    public ScriptTemplate asScript() {
        ParamsBuilder params = paramsBuilder();

        ScriptTemplate script = super.asScript();
        String template = formatTemplate("{sql}.dateTimeChrono(" + script.template() + ", {}, {})");
        params.script(script.params())
              .variable(zoneId().getId())
              .variable(extractor.chronoField().name());
        
        return new ScriptTemplate(template, params.build(), dataType());

    }

    @Override
    protected Processor makeProcessor() {
        return new DateTimeProcessor(extractor, zoneId());
    }

    @Override
    public DataType dataType() {
        return DataType.INTEGER;
    }

    // used for applying ranges
    public abstract String dateTimeFormat();
}