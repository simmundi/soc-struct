package pl.edu.icm.em.common.detached;

import pl.edu.icm.trurl.ecs.DaoManager;
import pl.edu.icm.trurl.ecs.dao.Dao;

import java.util.Optional;

public class DetachedEntity {
    private final static Object NOTHING = new Object();
    private final int id;
    private final Object[] components;

    private final DaoManager daoManager;

    public DetachedEntity(int id, DaoManager daoManager) {
        this.id = id;
        this.daoManager = daoManager;
        components = new Object[daoManager.componentCount()];
    }

    public<T> T get(Class<T> componentClass) {
        int idx = daoManager.classToIndex(componentClass);
        if (components[idx] == null) {
            Dao<T> objectDao = daoManager.indexToDao(idx);
            if (objectDao.isPresent(idx)) {
                components[idx] = objectDao.createAndLoad(idx);
            } else {
                components[idx] = NOTHING;
            }
        }
        return components[idx] == NOTHING ? null : (T) components[idx];
    }

    public <T> Optional<T> optional(Class<T> componentClass) {
        return Optional.ofNullable(get(componentClass));
    }

    public int getId() {
        return id;
    }
}
