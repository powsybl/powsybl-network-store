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

    public static Clause eq(String s, BindMarker marker) {
        return new Clause(s, marker);
    }

    public static Clause eq(String s, Object o) {
        return new Clause(s, o);
    }

    public static BindMarker bindMarker() {
        return new BindMarker();
    }

    public static Assignment set(String s, BindMarker marker) {
        return new Assignment(s, marker);
    }

    public static interface OngoingStatement {
        public SimpleStatement build();

        default OngoingStatement addClause(Clause clause) {
            throw new RuntimeException("ingore where clause");
//            return this;
        };
    }

    public static interface SimpleStatement {
        public String getQuery();

        // used by session.execute without prepare, the values are in the statement
        // used only for select for now
        default List<Object> values() {
            // TODO: for now, unused (only for execute without prepare)
            throw new RuntimeException("not implemented");
        }
    }

    public static interface BoundStatement extends SimpleStatement {
    }

    public static class Select implements OngoingStatement, SimpleStatement {
        String[] columns;
        String from;
        List<Clause> clauses = new ArrayList<>();

        public Select() {
            this.columns = columns;
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

        public Select where(Clause eq) {
            this.clauses.add(eq);
            return this;
        }

        public Select and(Clause eq) {
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

        public Select addClause(Clause clause) {
            this.clauses.add(clause);
            return this;
        };
    }

    public static class Insert implements OngoingStatement, SimpleStatement {
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
    }

    public static class Update implements OngoingStatement, SimpleStatement {
        StringBuilder sb = new StringBuilder();
        List<String> columns = new ArrayList<>();
        List<Clause> clauses = new ArrayList<>();

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

        public Update and(Clause clause) {
            this.clauses.add(clause);
            return this;
        }

        public Update value(String s, BindMarker marker) {
            columns.add(s);
            return this;
        }

        public Update with(Assignment assignment) {
            columns.add(assignment.s);
            return this;
        }

        public Update and(Assignment assignment) {
            columns.add(assignment.s);
            return this;
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

        public Update addClause(Clause clause) {
            this.clauses.add(clause);
            return this;
        };

    }

    public static class Delete implements OngoingStatement, SimpleStatement {
        StringBuilder sb = new StringBuilder();
        List<Clause> clauses = new ArrayList<>();

        public Delete() {
            sb.append("DELETE");
        }

        public Delete from(String s) {
            sb.append(" FROM " + s);
            return this;
        }

        public Delete where(Clause clause) {
            this.clauses.add(clause);
            return this;
        }

        public Delete and(Clause clause) {
            this.clauses.add(clause);
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

        public Delete addClause(Clause clause) {
            this.clauses.add(clause);
            return this;
        };

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

    public static class Clause<T extends OngoingStatement> {
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
