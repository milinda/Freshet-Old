/*
 * (C) Copyright 2014 Milinda Pathirage.
 *
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
 *
 */

package org.pathirage.freshet.utils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pathirage.freshet.operators.select.Expression;
import org.pathirage.freshet.operators.select.ExpressionType;
import org.pathirage.freshet.operators.select.PredicateType;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Serialize/deserialize expressions to/from JSON.
 */
public class ExpressionSerde {

    public static String serialize(Expression expression) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        StringWriter sw = new StringWriter();
        objectMapper.writeValue(sw, expression);

        return sw.toString();
    }

    public static Expression deserialize(String expression) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(expression, Expression.class);
    }

    public static void main(String[] args) throws IOException {
        String exp = "{\"type\":\"PREDICATE\",\"predicate\":\"EQUAL\",\"field\":null,\"value\":null,\"lhs\":{\"type\":\"FIELD\",\"predicate\":null,\"field\":\"name\",\"value\":null,\"lhs\":null,\"rhs\":null},\"rhs\":{\"type\":\"VALUE\",\"predicate\":null,\"field\":null,\"value\":\"Milinda\",\"lhs\":null,\"rhs\":null}}\n";
        Expression test = new Expression(ExpressionType.PREDICATE);
        test.setPredicate(PredicateType.EQUAL);

        Expression lhs = new Expression(ExpressionType.FIELD);
        lhs.setField("age");
        test.setLhs(lhs);

        Expression rhs = new Expression(ExpressionType.VALUE);
        rhs.setValue(20);
        test.setRhs(rhs);

        System.out.println(ExpressionSerde.serialize(test));


        Expression e = ExpressionSerde.deserialize(exp);
        System.out.println(ExpressionSerde.serialize(e));
    }
}
