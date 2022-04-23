package com.libre.video.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Libre
 * @date 2021/7/13 16:33
 */
@Component
@Slf4j
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    private static final String SYSTEM = "System";

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("mybatis plus start insert fill ....");
		this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
		this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("mybatis plus start update fill ....");
		this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
    }

	@Override
	public MetaObjectHandler strictFillStrategy(MetaObject metaObject, String fieldName, Supplier<?> fieldVal) {
		if (metaObject.getValue(fieldName) == null) {
			Object obj = fieldVal.get();
			if (Objects.nonNull(obj)) {
				metaObject.setValue(fieldName, obj);
			}
		}
		return this;
	}

}
