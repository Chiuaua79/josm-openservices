package org.openstreetmap.josm.plugins.ods.storage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.quadtree.Quadtree;

public class GeoIndexImpl<T> implements GeoIndex<T> {
    private Quadtree quadTree = new Quadtree();
    private final Class<T> clazz;
    private final Method getGeometryMethod;
    private final String property;

    public GeoIndexImpl(Class<T> clazz, String property) {
        super();
        this.clazz = clazz;
        this.property = property;
        getGeometryMethod = createGetGeometryMethod();
    }

    @Override
    public Class<T> getType() {
        return clazz;
    }

    //    @Override
    //    public List<String> getProperties() {
    //        return Collections.singletonList(property);
    //    }


    @Override
    public Object getKey(T entity) {
        return null;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.josm.plugins.ods.entities.Index#add(T)
     */
    @Override
    public void insert(T entity) {
        Geometry geom = getGeometry(entity);
        if (geom != null) {
            quadTree.insert(geom.getEnvelopeInternal(), entity);
        }
    }

    private Method createGetGeometryMethod() {
        try {
            return clazz.getMethod(getGetterName());
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    public Stream<T> getAll(Object id) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Stream<T> getAllByTemplate(T t) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.josm.plugins.ods.entities.GeoIndex#intersection(com.vividsolutions.jts.geom.Geometry)
     */
    @Override
    public List<T> intersection(Geometry geometry) {
        List<T> entities = new LinkedList<>();
        List<?> candidates = quadTree.query(geometry.getEnvelopeInternal());
        for (Object object : candidates) {
            T entity = clazz.cast(object);
            if (getGeometry(entity).intersects(geometry)) {
                entities.add(entity);
            }
        }
        return entities;
    }

    @Override
    public void remove(T entity) {
        quadTree.remove(getGeometry(entity).getEnvelopeInternal(), entity);
    }

    private Geometry getGeometry(T entity) {
        try {
            return (Geometry)getGeometryMethod.invoke(entity);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void clear() {
        quadTree = new Quadtree();
    }

    private String getGetterName() {
        return "get" + property.substring(0, 1).toUpperCase() +
                property.substring(1);
    }

    @Override
    public IndexKey<T> getIndexFunction() {
        // TODO Auto-generated method stub
        return null;
    }
}
