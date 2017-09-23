package org.openstreetmap.josm.plugins.ods.storage;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.openstreetmap.josm.plugins.ods.storage.query.ResultSet;
import org.openstreetmap.josm.plugins.ods.storage.query.ResultSetImpl;
import org.openstreetmap.josm.plugins.ods.util.ChainedIterator;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class ObjectStore<T> {
    private final Repository repository;
    private final Class<T> type;
    private final UniqueIndex<T> primaryIndex;
    private final Map<IndexKey<? super T>, Index<T>> indexes = new HashMap<>();
    private final Set<ObjectStore<? super T>> superStores = new HashSet<>();
    private final Set<ObjectStore<? extends T>> childStores = new HashSet<>();
    private Geometry boundary;
    private boolean empty = true;

    /**
     * Create a new write-only entity store.
     *
     * @param indexKey
     */
    public ObjectStore(Repository repository, IndexKey<T> indexKey) {
        this.repository = repository;
        this.type = indexKey.getBaseClass();
        this.primaryIndex = new UniqueIndexImpl<>(type, indexKey);
        createSuperStores();
    }

    public ObjectStore(Repository repository, UniqueIndex<T> primaryIndex) {
        this.repository = repository;
        this.type = primaryIndex.getType();
        this.primaryIndex = primaryIndex;
        createSuperStores();
    }

    public ObjectStore(Repository repository, Class<T> type, String... properties) {
        this(repository, createIndex(repository, type, properties));
    }

    public boolean isEmpty() {
        return empty;
    }

    private static <T2> UniqueIndex<T2> createIndex(Repository pRepository, Class<T2> pType, String[] properties) {
        if (properties.length == 0) {
            return createPrimaryIndex(pRepository, pType);
        }
        return new UniqueIndexImpl<>(pType, IndexKeyFactory.createPropertyIndexKey(pType, properties));
    }

    public Index<T> getIndex(IndexKey<T> indexFunction) {
        Index<T> index = indexes.get(indexFunction);
        if (index == null) {
            // Create a new temporary index
            index = new IndexImpl<>(indexFunction);
            stream().forEach(index::insert);
        }
        return index;
    }

    public boolean isInterface() {
        return type.isInterface();
    }

    public boolean isAbstract() {
        return Modifier.isAbstract(type.getModifiers());
    }

    public Iterator<? extends T> iterator() {
        return iterator(true);
    }

    public Iterator<? extends T> iterator(boolean withChildClasses) {
        if (!withChildClasses || childStores.isEmpty()) {
            return primaryIndex.iterator();
        }
        List<Iterator<? extends T>> iterators = new LinkedList<>();
        iterators.add(primaryIndex.iterator());
        for (ObjectStore<? extends T> store : childStores) {
            iterators.add(store.iterator(withChildClasses));
        }
        return new ChainedIterator<>(iterators);
    }

    public Stream<? extends T> stream() {
        return getAll(true);
    }

    public Stream<? extends T> getAll(boolean withChildClasses) {
        if (!withChildClasses || childStores.isEmpty()) {
            return isEmpty() ? Stream.empty() : primaryIndex.stream();
        }
        List<Stream<? extends T>> streams = new ArrayList<>(childStores.size() + 1);
        if (!isEmpty()) {
            streams.add(primaryIndex.stream());
        }
        for (ObjectStore<? extends T> store : childStores) {
            if (!store.isEmpty()) {
                streams.add(store.getAll(withChildClasses));
            }
        }
        @SuppressWarnings("unchecked")
        Stream<? extends T>[] arr = (Stream<? extends T>[])streams.toArray();
        return Stream.of(arr).flatMap(s->s);
    }

    public void addSuperStore(ObjectStore<? super T> store) {
        superStores.add(store);
        store.addChildStore(this);
    }

    public void addChildStore(ObjectStore<? extends T> store) {
        childStores.add(store);
    }


    public Class<T> getType() {
        return type;
    }

    protected void addIndex(Index<T> index) {
        indexes.put(index.getIndexFunction(), index);
    }

    protected void addIndex(IndexKey<T> indexFunction) {
        if (!indexes.containsKey(indexFunction)) {
            Index<T> index = new IndexImpl<>(indexFunction);
            indexes.put(indexFunction, index);
        }
    }

    public final UniqueIndex<T> createUniqueIndex(String ... properties) {
        UniqueIndex<T> index = new UniqueIndexImpl<>(type, properties);
        addIndex(index);
        return index;
    }

    @SafeVarargs
    public final Index<T> createIndex(String ... properties) {
        Index<T> index = new IndexImpl<>(type, properties);
        addIndex(index);
        return index;
    }

    public void add(T object) {
        primaryIndex.insert(object);
        this.empty = false;
        addToIndex(object);
    }

    private void addToIndex(T object) {
        for (Index<T> index : indexes.values()) {
            index.insert(object);
        }
    }

    public Geometry getBoundary() {
        if (boundary == null) {
            boundary = new GeometryFactory().buildGeometry(Collections.emptyList());
        }
        return boundary;
    }

    public void extendBoundary(Geometry bounds) {
        if (this.boundary == null) {
            this.boundary = bounds;
        } else {
            this.boundary = this.boundary.union(bounds);
        }
    }

    public UniqueIndex<T> getPrimaryIndex() {
        return primaryIndex;
    }

    public boolean contains(T entity) {
        return getPrimaryIndex().contains(entity);
    }

    public T getByPrimary(Object id) {
        return getPrimaryIndex().get(id);
    }

    public void remove(T entity) {
        for (Index<T> index : indexes.values()) {
            index.remove(entity);
        }
    }

    public void reIndex() {
        indexes.values().forEach(index -> index.clear());
        stream().forEach(object->{
            for (Index<T> index : indexes.values()) {
                index.insert(object);
            }
        });
    }

    /**
     * Clear the entity store. Remove all entities
     */
    public void clear() {
        primaryIndex.clear();
        for (Index<?> index : indexes.values()) {
            index.clear();
        }
        empty = true;
        boundary = null;
    }

    private void createSuperStores() {
        for (Class<? super T> superClass : Repository.getSuperClasses(type)) {
            ObjectStore<? super T> superStore = repository.getStore(superClass);
            if (superStore == null) {
                repository.register(superClass);
                superStore = repository.getStore(superClass);
            }
            addSuperStore(repository.getStore(superClass));
        }
    }

    private static <T2> UniqueIndex<T2> createPrimaryIndex(Repository repo, Class<T2> type2) {
        UniqueIndex<T2> primary = null;
        for (Class<? super T2> superClass : Repository.getSuperClasses(type2)) {
            ObjectStore<? super T2> superStore = repo.getStore(superClass);
            if (superStore != null) {
                UniqueIndex<? super T2> superIndex = superStore.getPrimaryIndex();
                if (!(superIndex instanceof IdentityIndex)) {
                    if (primary == null) {
                        primary = superIndex.forSubClass(type2);
                    }
                    else {
                        throw new UndeterminedPrimaryException();
                    }
                }
            }
        }
        return (primary != null ? primary : new IdentityIndex<>(type2));
    }

    public ResultSet<T> getAllResults(boolean withChildClasses) {
        List<UniqueIndex<? extends T>> indexes1 = new ArrayList<>(childStores.size() + 1);
        indexes1.add(primaryIndex);
        if (withChildClasses) {
            childStores.forEach(childStore -> {
                indexes1.add(childStore.getPrimaryIndex());
            });
        }
        return new ResultSetImpl<>(indexes1);
    }

    @Override
    public String toString() {
        return "Object store for " + type.getSimpleName();
    }
}