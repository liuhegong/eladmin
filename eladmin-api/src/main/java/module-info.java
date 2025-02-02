@SuppressWarnings({ "requires-automatic", "requires-transitive-automatic" })
module lwohvye.eladmin.api {
    requires transitive lwohvye.eladmin.common;

    exports com.lwohvye.api.annotation;
    exports com.lwohvye.api.utils;
    exports com.lwohvye.modules.mnt.domain;
    exports com.lwohvye.modules.mnt.service.dto;
    exports com.lwohvye.modules.quartz.domain;
    exports com.lwohvye.modules.quartz.service.dto;
    exports com.lwohvye.modules.system.api;
    exports com.lwohvye.modules.system.domain;
    exports com.lwohvye.modules.system.domain.vo;
    exports com.lwohvye.modules.system.domain.projection;
    exports com.lwohvye.modules.system.service.dto;

    opens com.lwohvye.modules.mnt.domain;
    opens com.lwohvye.modules.mnt.service.dto;
    opens com.lwohvye.modules.quartz.domain;
    opens com.lwohvye.modules.quartz.service.dto;
    opens com.lwohvye.modules.system.domain;
    opens com.lwohvye.modules.system.domain.vo;
    opens com.lwohvye.modules.system.domain.projection;
    opens com.lwohvye.modules.system.service.dto;

}
