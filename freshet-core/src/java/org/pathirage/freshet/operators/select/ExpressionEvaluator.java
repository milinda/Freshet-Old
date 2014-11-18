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

    public boolean evalPredicate(StreamElement se,
                                 StreamDefinition streamDefinition,
                                 Expression expression) {
        if (expression.isPredicate()) {
            PredicateType predicateType = expression.getPredicate();

            if(predicateType == PredicateType.AND || predicateType == PredicateType.OR || predicateType == PredicateType.NOT){
                Expression lhs = expression.getLhs();
                Expression rhs = expression.getRhs();

                if(lhs.isField() || lhs.isNumerical() || lhs.isValue() ||
                        rhs.isField() || rhs.isNumerical() || rhs.isValue()){
                    throw new ExpressionEvaluationException("Unsupported operands for operator: " + predicateType);
                }
            }

            if (predicateType == PredicateType.AND) {
                return evalPredicate(se, streamDefinition, expression.getLhs()) && evalPredicate(se, streamDefinition, expression.getRhs());
            } else if (predicateType == PredicateType.OR) {
                return evalPredicate(se, streamDefinition, expression.getLhs()) || evalPredicate(se, streamDefinition, expression.getRhs());
            } else if (predicateType == PredicateType.NOT) {
                return !evalPredicate(se, streamDefinition, expression.getLhs());
            } else if (predicateType == PredicateType.EQUAL) {
                return compare(se, streamDefinition, expression.getLhs(), expression.getRhs()) == 0;
            } else if (predicateType == PredicateType.NOT_EQUAL){
                return compare(se, streamDefinition, expression.getLhs(), expression.getRhs()) != 0;
            } else if (predicateType == PredicateType.GREATER_THAN){
                return compare(se, streamDefinition, expression.getLhs(), expression.getRhs()) > 0;
            } else if (predicateType == PredicateType.LESS_THAN){
                return compare(se, streamDefinition, expression.getLhs(), expression.getRhs()) < 0;
            } else if (predicateType == PredicateType.GREATER_THAN_OR_EQUAL){
                return compare(se, streamDefinition, expression.getLhs(), expression.getRhs()) >= 0;
            } else if (predicateType == PredicateType.LESS_THAN_OR_EQUAL){
                return compare(se, streamDefinition, expression.getLhs(), expression.getRhs()) <= 0;
            }
        } else {
            throw new ExpressionEvaluationException("Expression type " + expression.getType() + " is not valid at this state.");
        }

        return false;
    }

    public double compare(StreamElement se, StreamDefinition streamDefinition, Expression lhs, Expression rhs){
        if(lhs == null || rhs == null){
            throw new ExpressionEvaluationException("Compare operator requires two operands. lhs: " + lhs + " rhs: " + rhs);
        }

        Object lhsValue = evalExpValue(se, streamDefinition, lhs);
        Object rhsValue = evalExpValue(se, streamDefinition, rhs);

        if(lhsValue instanceof String || rhsValue instanceof String){
            return (lhsValue.equals(rhsValue) ? 0 : -1);
        } else if(lhsValue instanceof Number && rhsValue instanceof Number){
            return ((Number)lhsValue).doubleValue() - ((Number)rhsValue).doubleValue();
        } else if(lhsValue instanceof Boolean && rhsValue instanceof Boolean) {
            if((Boolean)lhsValue == (Boolean)rhsValue){
                return 0;
            } else {
                return -1;
            }
        } else {
            throw new ExpressionEvaluationException("Unsupported expression.");
        }
    }

    public Double evalNumericalExpression(StreamElement se, StreamDefinition streamDefinition, Expression expression){
        if(expression == null || expression.getOperator() == null){
            throw  new ExpressionEvaluationException("Undefined expression or empty operator.");
        }

        OperatorType operator = expression.getOperator();

        Object lhsValue = evalExpValue(se, streamDefinition, expression.getLhs());
        Object rhsValue = evalExpValue(se, streamDefinition, expression.getRhs());

        if(!(lhsValue instanceof Double) || !(rhsValue instanceof Double)){
            throw new ExpressionEvaluationException("At lease one operand is not a number.");
        }

        if(operator == OperatorType.PLUS){
            return (Double)lhsValue + (Double)rhsValue;
        } else if (operator == OperatorType.MINUS) {
            return (Double)lhsValue - (Double)rhsValue;
        } else if (operator == OperatorType.MULTIPLY){
            return (Double)lhsValue * (Double)rhsValue;
        }else if (operator == OperatorType.DIVIDE){
            return (Double)lhsValue / (Double)rhsValue;
        } else {
            throw  new ExpressionEvaluationException("Unsupported operator: " + operator);
        }
    }

    public Object evalExpValue(StreamElement se, StreamDefinition streamDefinition, Expression expression) {
        if(expression == null){
            throw new ExpressionEvaluationException("Empty expression.");
        }

        if (expression.isField()) {
            String fieldName = expression.getField();

            if (fieldName == null || !streamDefinition.isValidField(fieldName)){
                throw new ExpressionEvaluationException("Unknown field: " + fieldName);
            }

            StreamDefinition.FieldType fieldType = streamDefinition.getType(fieldName);

            if (fieldType == StreamDefinition.FieldType.STRING) {
                return se.getStringField(fieldName);
            } else if (fieldType == StreamDefinition.FieldType.INTEGER) {
                return se.getIntegerField(fieldName).doubleValue();
            } else if (fieldType == StreamDefinition.FieldType.LONG) {
                return se.getLongField(fieldName).doubleValue();
            } else if (fieldType == StreamDefinition.FieldType.BOOL) {
                return se.getBoolField(fieldName);
            } else if (fieldType == StreamDefinition.FieldType.FLOAT) {
                return se.getFloatField(fieldName).doubleValue();
            } else {
                throw new ExpressionEvaluationException("Unsupported field type " + fieldType + "!");
            }
        } else if (expression.isValue()) {
            return expression.getValue();
        } else if(expression.isNumerical()) {
            return evalNumericalExpression(se, streamDefinition, expression);
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
