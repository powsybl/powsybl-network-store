package com.powsybl.network.store.server;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultSet implements Iterable<Row> {

    java.sql.ResultSet resultSet;

    public ResultSet(java.sql.ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    @Override
    public Iterator<Row> iterator() {
        return new Iterator<Row>() {
            boolean moved = false;
            boolean hasmore = false;

            private void ensureNext() {
                if (!moved) {
                    try {
                        moved = true;
                        hasmore = resultSet.next();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public boolean hasNext() {
                ensureNext();
                return hasmore;
            }

            @Override
            public Row next() {
                ensureNext();
                if (!hasmore) {
                    throw new NoSuchElementException();
                }
                moved = false;
                return new Row(ResultSet.this);
            }
        };
    }

    public Row one() {
        try {
            boolean b = resultSet.next();
            if (b) {
                return new Row(ResultSet.this);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

class Row {
    public static ObjectMapper mapper = new ObjectMapper();

    ResultSet resultSet;

    public Row(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public static boolean isCustomTypeJsonified(Class<?> class1) {
        return !(
                  Integer.class.equals(class1) || Long.class.equals(class1)
                  || Float.class.equals(class1) || Double.class.equals(class1)
                  || String.class.equals(class1) || Boolean.class.equals(class1)
                  || UUID.class.equals(class1)
                  || Date.class.isAssignableFrom(class1) // java.util.Date and java.sql.Date
            );
    }

    public <T> T get(int i, Class<T> class1) {
        try {
            Object o = resultSet.resultSet.getObject(i + 1);
            if (o != null && isCustomTypeJsonified(class1)) {
                try {
                    return new ObjectMapper().readValue((String) o, class1);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return (T) o;
            }
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    public boolean getBoolean(int i) {
        try {
            return resultSet.resultSet.getBoolean(i + 1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public float getFloat(int i) {
        try {
            return resultSet.resultSet.getFloat(i + 1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public double getDouble(int i) {
        try {
            return resultSet.resultSet.getDouble(i + 1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getInt(int i) {
        try {
            return resultSet.resultSet.getInt(i + 1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getString(int i) {
        try {
            return resultSet.resultSet.getString(i + 1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Date getTimestamp(int i) {
        try {
            return resultSet.resultSet.getDate(i + 1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public UUID getUuid(int i) {
        try {
            return (UUID) resultSet.resultSet.getObject(i + 1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> getList(int i, Class<T> class1) {
        try {
            Object o = resultSet.resultSet.getObject(i + 1);
            if (o != null) {
                try {
                    return (List<T>) mapper.readValue((String) o,
                            mapper.getTypeFactory().constructCollectionType(List.class, class1));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    public <T, U> Map<T, U> getMap(int i, Class<T> class1, Class<U> class2) {
        try {
            Object o = resultSet.resultSet.getObject(i + 1);
            if (o != null) {
                try {
                    return (Map<T, U>) mapper.readValue((String) o,
                            mapper.getTypeFactory().constructMapType(Map.class, class1, class2));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    public <T> Set<T> getSet(int i, Class<T> class1) {
        try {
            Object o = resultSet.resultSet.getObject(i + 1);
            if (o != null) {
                try {
                    return (Set<T>) mapper.readValue((String) o,
                            mapper.getTypeFactory().constructCollectionType(Set.class, class1));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    public boolean isNull(int i) {
        try {
            return resultSet.resultSet.getObject(i + 1) == null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Instant getInstant(int i) {
        try {
            return ((java.sql.Timestamp) resultSet.resultSet.getObject(i + 1)).toInstant();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
