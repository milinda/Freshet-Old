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

package org.pathirage.freshet.operators.select;

import org.pathirage.freshet.data.StreamDefinition;
import org.pathirage.freshet.data.StreamElement;

/**
 * Evaluate boolean expressions used in WHERE clause.
 */
public class ExpressionEvaluator {

    public boolean evalToBool(StreamElement se,
                              StreamDefinition streamDefinition,
                              Expression expression) {
        if (expression.isPredicate()) {
            PredicateType predicateType = expression.getPredicate();

            if (predicateType == PredicateType.AND) {
                return evalToBool(se, streamDefinition, expression.getLhs()) && evalToBool(se, streamDefinition, expression.getRhs());
            } else if (predicateType == PredicateType.OR) {
                return evalToBool(se, streamDefinition, expression.getLhs()) || evalToBool(se, streamDefinition, expression.getRhs());
            } else if (predicateType == PredicateType.NOT) {
                return !evalToBool(se, streamDefinition, expression.getLhs());
            } else if (predicateType == PredicateType.EQUAL) {

            } else if (predicateType == PredicateType.NOT_EQUAL){

            } else if (predicateType == PredicateType.GREATER_THAN){

            } else if (predicateType == PredicateType.LESS_THAN){

            } else if (predicateType == PredicateType.GREATER_THAN_OR_EQUAL){

            } else if (predicateType == PredicateType.LESS_THAN_OR_EQUAL){

            }
        } else {
            throw new ExpressionEvaluationException("Expression type " + expression.getType() + " is not valid at this state.");
        }

        return false;
    }

    public int compare(StreamElement se, StreamDefinition streamDefinition, Expression lhs, Expression rhs){
        if(lhs == null || rhs == null){
            throw new ExpressionEvaluationException("Compare operator requires two operands. lhs: " + lhs + " rhs: " + rhs);
        }

        Object lhsValue = null;
        Object rhsValue = null;

        lhsValue = evalExpValue(se, streamDefinition, lhs);
        rhsValue = evalExpValue(se, streamDefinition, rhs);

        if(lhsValue instanceof String || rhsValue instanceof String){
            return (lhsValue.equals(rhsValue) ? 0 : -1);
        }

        return 0;
    }

    public Object evalExpValue(StreamElement se, StreamDefinition streamDefinition, Expression expression) {
        if (expression.isField()) {
            String fieldName = expression.getField();

            if (!streamDefinition.isValidField(fieldName)){
                throw new ExpressionEvaluationException("Unknown field: " + fieldName);
            }

            StreamDefinition.FieldType fieldType = streamDefinition.getType(fieldName);

            if (fieldType == StreamDefinition.FieldType.STRING) {
                return se.getStringField(fieldName);
            } else if (fieldType == StreamDefinition.FieldType.INTEGER) {
                return se.getIntegerField(fieldName);
            } else if (fieldType == StreamDefinition.FieldType.BOOL) {
                return se.getBoolField(fieldName);
            } else if (fieldType == StreamDefinition.FieldType.FLOAT) {
                return se.getFloatField(fieldName);
            } else {
                throw new ExpressionEvaluationException("Unsupported field type " + fieldType + "!");
            }
        } else if (expression.isValue()) {
            return expression.getValue();
        } else {
            throw new ExpressionEvaluationException("Unsupported value expression type " + expression.getType());
        }
    }

    public class ExpressionEvaluationException extends RuntimeException {
        public ExpressionEvaluationException() {
            super();
        }

        public ExpressionEvaluationException(String message) {
            super(message);
        }

        public ExpressionEvaluationException(String message, Throwable cause) {
            super(message, cause);
        }

        public ExpressionEvaluationException(Throwable cause) {
            super(cause);
        }

        protected ExpressionEvaluationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
