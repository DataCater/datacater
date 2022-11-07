package io.datacater.core.stream;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

public class TenantListener {

  @PreUpdate
  @PreRemove
  @PrePersist
  public void setTenant(TenantAware entity) {
    final String tenantId = "DataCaterNew";
    entity.setTenantId(tenantId);
  }
}
