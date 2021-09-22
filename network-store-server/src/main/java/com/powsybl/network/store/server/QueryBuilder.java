package com.powsybl.network.store.server;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class QueryBuilder {
    private QueryBuilder() {

    }

    public static <T> T literal(T t) {
        return t;
    }

    public static Select selectFrom(String table) {
        return new Select().from(table);
    }

    public static Insert insertInto(String s) {
        return new Insert(s);
    }

    public static Update update(String s) {
        return new Update(s);
    }

    public static Delete deleteFrom(String table) {
        return new Delete().from(table);
    }

    public static <T extends OngoingStatement<T>> Clause<T> eq(String s, BindMarker marker) {
        return new Clause<>(s, marker);
    }

    public static <T extends OngoingStatement<T>> Clause<T> eq(String s, Object o) {
        return new Clause<>(s, o);
    }

    public static BindMarker bindMarker() {
        return new BindMarker();
    }

    public static Assignment set(String s, BindMarker marker) {
        return new Assignment(s, marker);
    }

    public static interface OngoingStatement<T extends OngoingStatement<T>> {
        public SimpleStatement build();

        OngoingStatement<T> addClause(Clause<T> clause);
    }

    public static interface SimpleStatement {
        public String getQuery();

        List<Object> values();
    }

    public static interface BoundStatement extends SimpleStatement {
    }

    public static class Select implements OngoingStatement<Select>, SimpleStatement {
        String[] columns;
        String from;
        List<Clause<Select>> clauses = new ArrayList<>();

        public Select() {
        }

        public Select columns(String... columns) {
            this.columns = columns;
            return this;
        }

        @Override
        public String getQuery() {
            StringBuilder sb = new StringBuilder("SELECT " + String.join(",", columns) + " from " + from);
            if (clauses.size() > 0) {
                sb.append(" where "
                        + String.join(" and ", clauses.stream().map(s -> s.s + "=?").collect(Collectors.toList())));
            }
            return sb.toString();
        }

        public Select from(String from) {
            // HACK: in cassandra we have materialized views name xxxByZZZ,
            // they are the exact same tables, except that the pk order is switched
            // So removing the suffix ByZZZ allows in relational databases that have index
            // to do the same query in the original table
            int index = from.indexOf("By");
            if (index != -1) {
                this.from = from.substring(0, index);
            } else {
                this.from = from;
            }
            return this;
        }

        public Select where(Clause<Select> eq) {
            this.clauses.add(eq);
            return this;
        }

        @Override
        public List<Object> values() {
            return clauses.stream().map(o -> o.o).collect(Collectors.toList());
        }

        @Override
        public SimpleStatement build() {
            return this;
        }

        public Clause<Select> whereColumn(String s) {
            return new Clause<>(s, null, this);
        }

        public Select addClause(Clause<Select> clause) {
            this.clauses.add(clause);
            return this;
        }

        public Select allowFiltering() {
            return this;
        }
        
    }

    public static class Insert implements OngoingStatement<Insert>, SimpleStatement {
        StringBuilder sb = new StringBuilder("INSERT INTO ");
        List<String> columns = new ArrayList<>();

        public Insert(String s) {
            sb.append(s + "(");
        }

        public Insert value(String s, BindMarker marker) {
            columns.add(s);
            return this;
        }

        @Override
        public String getQuery() {
            return sb.append(String.join(",", columns)).append(") values (")
                    .append(String.join(",", columns.stream().map(s -> "?").collect(Collectors.toList()))).append(")")
                    .toString();
        }

        @Override
        public SimpleStatement build() {
            return this;
        }

        @Override
        public List<Object> values() {
            throw new RuntimeException("For now we use insert only with prepared statements");
        }

        @Override
        public OngoingStatement<Insert> addClause(Clause<Insert> clause) {
            throw new RuntimeException("we should have no clause for insert");
        }
    }

    public static class Update implements OngoingStatement<Update>, SimpleStatement {
        StringBuilder sb = new StringBuilder();
        List<String> columns = new ArrayList<>();
        List<Clause<Update>> clauses = new ArrayList<>();

        public Update(String s) {
            sb.append("UPDATE " + s);
        }

        public Update set(Assignment assignment) {
            columns.add(assignment.s);
            return this;
        }

        public Clause<Update> whereColumn(String s) {
            return new Clause<>(s, null, this);
        }

        @Override
        public String getQuery() {
            sb.append(" set ")
                    .append(String.join(", ", columns.stream().map(s -> s + "=?").collect(Collectors.toList())));
            if (clauses.size() > 0) {
                sb.append(" where ")
                        .append(String.join(" and ",
                                clauses.stream().map(s -> s.s + "=?").collect(Collectors.toList())));
            }
            return sb.toString();
        }

        @Override
        public SimpleStatement build() {
            return this;
        }

        public Update addClause(Clause<Update> clause) {
            this.clauses.add(clause);
            return this;
        }

        @Override
        public List<Object> values() {
            return clauses.stream().map(o -> o.o).collect(Collectors.toList());
        }

    }

    public static class Delete implements OngoingStatement<Delete>, SimpleStatement {
        StringBuilder sb = new StringBuilder();
        List<Clause<Delete>> clauses = new ArrayList<>();

        public Delete() {
            sb.append("DELETE");
        }

        public Delete from(String s) {
            sb.append(" FROM " + s);
            return this;
        }

        @Override
        public String getQuery() {
            if (clauses.size() > 0) {
                sb.append(" where ").append(
                        String.join(" and ", clauses.stream().map(s -> s.s + "=?").collect(Collectors.toList())));
            }
            return sb.toString();
        }

        @Override
        public SimpleStatement build() {
            return this;
        }

        public Clause<Delete> whereColumn(String column) {
            return new Clause<>(column, null, this);
        }

        public Delete addClause(Clause<Delete> clause) {
            this.clauses.add(clause);
            return this;
        }

        @Override
        public List<Object> values() {
            return clauses.stream().map(o -> o.o).collect(Collectors.toList());
        }
    }

    public static class BindMarker {

    }

    public static class Assignment {
        String s;

        public Assignment(String s, BindMarker marker) {
            this.s = s;
        }

        public static Assignment setColumn(String string, BindMarker bindMarker) {
            return new Assignment(string, bindMarker);
        }
    }

    public static class Clause<T extends OngoingStatement<T>> {
        T context;
        String s;
        Object o = null;

        public Clause(String s, BindMarker marker) {
            this.s = s;
        }

        public Clause(String s, Object o) {
            this.s = s;
            this.o = o;
        }

        public Clause(String s, Object o, T context) {
            this.s = s;
            this.o = o;
            this.context = context;
        }

        public T isEqualTo(BindMarker marker) {
            this.context.addClause(this);
            return context;
        }

        public T isEqualTo(Object o) {
            this.o = o;
            this.context.addClause(this);
            return context;
        }
    }

    public enum BatchType {
        UNLOGGED,
    }
}
