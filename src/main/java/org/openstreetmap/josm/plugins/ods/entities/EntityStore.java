package org.openstreetmap.josm.plugins.ods.entities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The EntityStore stores entities of a single entity type.
 * 
 * @author gertjan
 *
 */
public class EntityStore<T extends Entity> implements Iterable<T> {
    private Map<Object, T> entities = new HashMap<>();
    private Map<String, T> namedEntities = new HashMap<>();
    private Map<Object, T> referencedEntities = new HashMap<>();
	
	public boolean add(T entity) {
		if (!entities.containsKey(entity.getId())) {
            entities.put(entity.getId(), entity);
            if (entity.hasName()) {
      		    namedEntities.put(entity.getName(), entity);
            }
            if (entity.hasReferenceId()) {
                referencedEntities.put(entity.getReferenceId(), entity);
            }
            return true;
		}
		return false;
	}
	
	public T get(Object id) {
		return entities.get(id);
	}
	
    public T getByReference(Object id) {
        return referencedEntities.get(id);
    }
    
	public T getByName(String name) {
	    return namedEntities.get(name);
	}
	
	public Iterator<T> iterator() {
	    return entities.values().iterator();
	}

    public boolean contains(T entity) {
        return get(entity.getId()) != null;
    }
    
    public void remove(T entity) {
        entities.remove(entity.getId());
    }
}
