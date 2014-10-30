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

public class Expression {
    private ExpressionType type;

    // Not null if expression is a predicate
    private PredicateType predicate;

    // Not null if expression is a field
    private String field;

    // Not null if expression is a value
    private Object value;

    // Not null if if binary or unary predicate
    private Expression lhs;

    // Not null if binary predicate
    private Expression rhs;

    public Expression(){
        this.type = null;
        this.predicate = null;
        this.field = null;
        this.value = null;
        this.lhs = null;
        this.rhs = null;
    }
    public Expression(ExpressionType type){
        this.type = type;
        this.predicate = null;
        this.field = null;
        this.value = null;
        this.lhs = null;
        this.rhs = null;
    }

    public ExpressionType getType() {
        return type;
    }

    public void setType(ExpressionType type) {
        this.type = type;
    }

    public boolean isPredicate(){
        return type == ExpressionType.PREDICATE;
    }

    public boolean isField(){
        return type == ExpressionType.FIELD;
    }

    public boolean isValue(){
        return type == ExpressionType.VALUE;
    }

    public PredicateType getPredicate() {
        return predicate;
    }

    public void setPredicate(PredicateType predicate) {
        this.predicate = predicate;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Expression getLhs() {
        return lhs;
    }

    public void setLhs(Expression lhs) {
        this.lhs = lhs;
    }

    public Expression getRhs() {
        return rhs;
    }

    public void setRhs(Expression rhs) {
        this.rhs = rhs;
    }
}
