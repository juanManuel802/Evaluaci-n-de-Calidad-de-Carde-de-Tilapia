package co.unillanos.secct.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EntitiesTest {

    @Test
    @DisplayName("Role entity: getters, setters, constructores, equals, hashCode y toString")
    void testRoleEntity() {
        Role role = new Role((short) 1, "ADMINISTRADOR");
        assertThat(role.getId()).isEqualTo((short) 1);
        assertThat(role.getCode()).isEqualTo("ADMINISTRADOR");

        Role role2 = new Role("ADMINISTRADOR");
        role2.setId((short) 1);
        assertThat(role).isEqualTo(role2);
        assertThat(role.hashCode()).isEqualTo(role2.hashCode());
        assertThat(role.toString()).contains("ADMINISTRADOR");

        Role roleDefault = new Role();
        roleDefault.setCode("PISCICULTOR");
        roleDefault.setId((short) 2);
        assertThat(roleDefault.getCode()).isEqualTo("PISCICULTOR");
        assertThat(role).isNotEqualTo(roleDefault);
        assertThat(role.equals(null)).isFalse();
        assertThat(role.equals(new Object())).isFalse();
    }

    @Test
    @DisplayName("EventType entity: getters, setters, constructores, equals, hashCode y toString")
    void testEventTypeEntity() {
        EventType eventType = new EventType((short) 1, "USER_REGISTERED", "Registro de usuario");
        assertThat(eventType.getId()).isEqualTo((short) 1);
        assertThat(eventType.getCode()).isEqualTo("USER_REGISTERED");
        assertThat(eventType.getDescription()).isEqualTo("Registro de usuario");

        EventType eventType2 = new EventType("USER_REGISTERED", "Registro");
        assertThat(eventType).isEqualTo(eventType2);
        assertThat(eventType.hashCode()).isEqualTo(eventType2.hashCode());
        assertThat(eventType.toString()).contains("USER_REGISTERED");

        EventType eventDefault = new EventType();
        eventDefault.setId((short) 2);
        eventDefault.setCode("LOGIN_SUCCESS");
        eventDefault.setDescription("Login exitoso");
        assertThat(eventDefault.getId()).isEqualTo((short) 2);
        assertThat(eventType).isNotEqualTo(eventDefault);
        assertThat(eventType.equals(null)).isFalse();
        assertThat(eventType.equals(new Object())).isFalse();
    }

    @Test
    @DisplayName("Log entity: getters, setters, constructores y PrePersist")
    void testLogEntity() {
        EventType eventType = new EventType((short) 1, "USER_REGISTERED", "Registro de usuario");
        OffsetDateTime now = OffsetDateTime.now();

        Log log = new Log(eventType, 10, 20, 30, now, "Log description");
        log.setId(100L);

        assertThat(log.getId()).isEqualTo(100L);
        assertThat(log.getEventType()).isEqualTo(eventType);
        assertThat(log.getUserId()).isEqualTo(10);
        assertThat(log.getBatchId()).isEqualTo(20);
        assertThat(log.getEvaluationId()).isEqualTo(30);
        assertThat(log.getOccurredAt()).isEqualTo(now);
        assertThat(log.getDescription()).isEqualTo("Log description");

        Log defaultLog = new Log();
        defaultLog.setEventType(eventType);
        defaultLog.setUserId(5);
        defaultLog.setBatchId(6);
        defaultLog.setEvaluationId(7);
        defaultLog.setDescription("Desc");
        defaultLog.setOccurredAt(now);
        assertThat(defaultLog.getUserId()).isEqualTo(5);

        Log prePersistLog = new Log();
        prePersistLog.onPersist();
        assertThat(prePersistLog.getOccurredAt()).isNotNull();
    }

    @Test
    @DisplayName("User entity: getters, setters, constructores y PrePersist/PreUpdate")
    void testUserEntity() {
        Role role = new Role((short) 2, "PISCICULTOR");
        OffsetDateTime now = OffsetDateTime.now();

        User user = new User(1, role, "Juan", "Perez", "juan@correo.com", "3101234567",
                "hash", "salt", true, false, null, null, now, now);

        assertThat(user.getId()).isEqualTo(1);
        assertThat(user.getRole()).isEqualTo(role);
        assertThat(user.getFirstName()).isEqualTo("Juan");
        assertThat(user.getLastName()).isEqualTo("Perez");
        assertThat(user.getEmail()).isEqualTo("juan@correo.com");
        assertThat(user.getPhone()).isEqualTo("3101234567");
        assertThat(user.getPasswordHash()).isEqualTo("hash");
        assertThat(user.getSalt()).isEqualTo("salt");
        assertThat(user.isActive()).isTrue();
        assertThat(user.isDeleted()).isFalse();
        assertThat(user.getDeletedAt()).isNull();
        assertThat(user.getDeletedBy()).isNull();
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);

        User defaultUser = new User();
        defaultUser.setId(2);
        defaultUser.setRole(role);
        defaultUser.setFirstName("Maria");
        defaultUser.setLastName("Gomez");
        defaultUser.setEmail("maria@correo.com");
        defaultUser.setPhone("3201234567");
        defaultUser.setPasswordHash("h2");
        defaultUser.setSalt("s2");
        defaultUser.setActive(false);
        defaultUser.setDeleted(true);
        defaultUser.setDeletedAt(now);
        defaultUser.setDeletedBy(1);

        defaultUser.onCreate();
        assertThat(defaultUser.getCreatedAt()).isNotNull();
        assertThat(defaultUser.getUpdatedAt()).isNotNull();

        defaultUser.onUpdate();
        assertThat(defaultUser.getUpdatedAt()).isNotNull();
    }
}
