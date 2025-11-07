package com.kanban.graphql;

import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class GraphQLScalarConfig {

    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(dateTimeScalar())
                .scalar(longScalar());
    }

    private GraphQLScalarType dateTimeScalar() {
        return GraphQLScalarType.newScalar()
                .name("DateTime")
                .description("DateTime scalar")
                .coercing(new Coercing<LocalDateTime, String>() {
                    @Override
                    public String serialize(Object dataFetcherResult) {
                        if (dataFetcherResult instanceof LocalDateTime) {
                            return ((LocalDateTime) dataFetcherResult).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                        return dataFetcherResult != null ? dataFetcherResult.toString() : null;
                    }

                    @Override
                    public LocalDateTime parseValue(Object input) {
                        if (input instanceof String) {
                            return LocalDateTime.parse((String) input, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                        return null;
                    }

                    @Override
                    public LocalDateTime parseLiteral(Object input) {
                        if (input instanceof String) {
                            return LocalDateTime.parse((String) input, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                        return null;
                    }
                })
                .build();
    }

    private GraphQLScalarType longScalar() {
        return GraphQLScalarType.newScalar()
                .name("Long")
                .description("Long scalar")
                .coercing(new Coercing<Long, Long>() {
                    @Override
                    public Long serialize(Object dataFetcherResult) {
                        if (dataFetcherResult instanceof Long) {
                            return (Long) dataFetcherResult;
                        }
                        if (dataFetcherResult instanceof Number) {
                            return ((Number) dataFetcherResult).longValue();
                        }
                        if (dataFetcherResult != null) {
                            return Long.parseLong(dataFetcherResult.toString());
                        }
                        return null;
                    }

                    @Override
                    public Long parseValue(Object input) {
                        if (input instanceof Long) {
                            return (Long) input;
                        }
                        if (input instanceof Number) {
                            return ((Number) input).longValue();
                        }
                        if (input != null) {
                            return Long.parseLong(input.toString());
                        }
                        return null;
                    }

                    @Override
                    public Long parseLiteral(Object input) {
                        if (input instanceof Number) {
                            return ((Number) input).longValue();
                        }
                        if (input != null) {
                            return Long.parseLong(input.toString());
                        }
                        return null;
                    }
                })
                .build();
    }
}

