package com.lehvolk.xodus.repo;

import jetbrains.exodus.entitystore.EntityStore;
import jetbrains.exodus.entitystore.PersistentEntityStoreImpl;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

/**
 * @author Alexey Volkov
 * @since 31.10.15
 */
@Slf4j
@Singleton
public class ConfigurationService {

	public PersistentEntityStoreImpl configStore;
	public PersistentEntityStoreImpl currentStore;

	@PostConstruct
	public void construct() {
		currentStore = PersistentEntityStores.newInstance("/home/lehvolk/work/xodus/data");
	}

	public String getLabelFormat(long entityTypeId) {
		return "{{id}}";
	}

	public String getDetailsFormat(long entityTypeId) {
		return "some information";
	}


	@PreDestroy
	public void destroy() {
		close(configStore);
		close(currentStore);
	}

	private void close(EntityStore env) {
		if (env != null) {
			try {
				env.close();
			} catch (Exception e) {
				log.error("error closing entity store: " + env, e);
			}
		}
	}
}
